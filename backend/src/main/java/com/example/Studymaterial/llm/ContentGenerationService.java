package com.example.Studymaterial.llm;

import com.example.Studymaterial.dto.GeneratedContent;
import com.example.Studymaterial.dto.GeneratedContent.Card;
import com.example.Studymaterial.dto.GeneratedContent.Quiz;
import com.example.Studymaterial.dto.GeneratedContent.Scene;
import com.example.Studymaterial.dto.GenerationSummary;
import com.example.Studymaterial.entity.Flashcard;
import com.example.Studymaterial.entity.QuizQuestion;
import com.example.Studymaterial.entity.Subtopic;
import com.example.Studymaterial.entity.Topic;
import com.example.Studymaterial.repository.TopicRepository;
import com.example.Studymaterial.service.TopicService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Generates the micro-video script, quiz and flashcards for a topic with ONE LLM call
 * (cheaper and faster than three calls, and all three stay consistent with each other).
 */
@Service
@RequiredArgsConstructor
public class ContentGenerationService {

    private static final int MAX_SCENES = 8;
    private static final int MAX_QUIZ = 10;
    private static final int MAX_CARDS = 15;

    private final LlmClient llm;
    private final TopicService topicService;
    private final TopicRepository topicRepo;
    private final TransactionTemplate tx;
    private final ObjectMapper mapper;

    /** parts: any of "video", "quiz", "flashcards". Existing generated items of those parts are replaced. */
    public GenerationSummary generate(Long topicId, Set<String> parts) {
        boolean wantVideo = parts.contains("video");
        boolean wantQuiz = parts.contains("quiz");
        boolean wantCards = parts.contains("flashcards");
        if (!wantVideo && !wantQuiz && !wantCards) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "parts must contain video, quiz and/or flashcards");
        }

        // 1) read (no transaction held while we wait for the LLM)
        Topic topic = topicService.getById(topicId);
        String prompt = buildPrompt(topic, wantVideo, wantQuiz, wantCards);

        // 2) ONE LLM call
        String raw = llm.chatJson(SYSTEM_PROMPT, prompt);
        GeneratedContent result = parse(raw);

        // 3) validate
        List<Scene> scenes = wantVideo ? cleanScenes(result.scenes()) : List.of();
        List<Quiz> quiz = wantQuiz ? cleanQuiz(result.quiz()) : List.of();
        List<Card> cards = wantCards ? cleanCards(result.flashcards()) : List.of();

        if ((wantVideo && scenes.isEmpty()) || (wantQuiz && quiz.isEmpty()) || (wantCards && cards.isEmpty())) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "The LLM answer was incomplete. Please try again.");
        }

        String scriptJson = wantVideo ? toJson(scenes) : null;

        // 4) save everything atomically
        tx.executeWithoutResult(status -> {
            Topic t = topicRepo.findById(topicId).orElseThrow(() ->
                    new ResponseStatusException(HttpStatus.NOT_FOUND, "Topic not found"));

            if (wantVideo) t.setVideoScript(scriptJson);

            if (wantQuiz) {
                t.getQuizQuestions().clear();
                for (Quiz q : quiz) {
                    QuizQuestion e = new QuizQuestion();
                    e.setQuestion(q.question().trim());
                    e.setOptionA(q.options().get(0).trim());
                    e.setOptionB(q.options().get(1).trim());
                    e.setOptionC(q.options().get(2).trim());
                    e.setOptionD(q.options().get(3).trim());
                    e.setCorrectOption(q.correct().trim().toUpperCase());
                    e.setTopic(t);
                    t.getQuizQuestions().add(e);
                }
            }

            if (wantCards) {
                t.getFlashcards().clear();
                for (Card c : cards) {
                    Flashcard e = new Flashcard();
                    e.setQuestion(c.question().trim());
                    e.setAnswer(c.answer().trim());
                    e.setTopic(t);
                    t.getFlashcards().add(e);
                }
            }
            topicRepo.save(t);
        });

        return new GenerationSummary(scenes.size(), quiz.size(), cards.size());
    }

    // ---------------------------------------------------------------- prompt

    private static final String SYSTEM_PROMPT = """
            You are an expert teacher who creates accurate, concise study material for students.
            Reply with ONE valid JSON object only. No markdown, no code fences, no extra text.
            Never invent facts. Keep every item self-contained and correct.""";

    private String buildPrompt(Topic topic, boolean video, boolean quiz, boolean cards) {
        StringBuilder sb = new StringBuilder();

        String unit = topic.getUnit() != null ? topic.getUnit().getName() : null;
        String subject = (topic.getUnit() != null && topic.getUnit().getSyllabus() != null)
                ? topic.getUnit().getSyllabus().getSubjectName() : null;

        sb.append("Subject: ").append(subject == null ? "(not given)" : subject).append('\n');
        sb.append("Unit: ").append(unit == null ? "(not given)" : unit).append('\n');
        sb.append("Topic: ").append(topic.getName()).append('\n');

        if (!topic.getSubtopics().isEmpty()) {
            sb.append("Subtopics to cover:\n");
            for (Subtopic s : topic.getSubtopics()) {
                sb.append("- ").append(s.getName());
                if (s.getContent() != null && !s.getContent().isBlank()) {
                    sb.append(": ").append(truncate(s.getContent(), 600));
                }
                sb.append('\n');
            }
        }
        if (topic.getContent() != null && !topic.getContent().isBlank()) {
            sb.append("Existing notes (use as the main source, stay consistent with them):\n")
                    .append(truncate(topic.getContent(), 4000)).append('\n');
        }

        sb.append("\nWrite in the same language as the topic name. Target level: college student.\n");
        sb.append("Return a JSON object with exactly these keys:\n");

        if (video) {
            sb.append("""
                    "scenes": 5 to 6 scenes for a 1-2 minute explainer video, in teaching order
                      (intro, core ideas, an example, a short recap). Each scene is
                      {"title": "max 6 words", "bullets": ["2-3 short on-screen points, max 12 words each"],
                       "narration": "25-45 spoken words, natural, explains the scene"}.
                    """);
        }
        if (quiz) {
            sb.append("""
                    "quiz": 5 multiple-choice questions of mixed difficulty. Each is
                      {"question": "...", "options": ["option A text", "option B text", "option C text", "option D text"],
                       "correct": "A" | "B" | "C" | "D"}.
                      Exactly 4 options without letter prefixes, exactly one correct, plausible wrong options,
                      and spread the correct letter across A-D.
                    """);
        }
        if (cards) {
            sb.append("""
                    "flashcards": 8 cards for quick revision, short recall-style Q&A (not full explanations). Each is
                      {"question": "...", "answer": "1-2 sentences"}.
                    """);
        }
        return sb.toString();
    }

    // ---------------------------------------------------------------- parsing / validation

    private GeneratedContent parse(String raw) {
        String json = raw.trim();
        // some models wrap the JSON in ``` fences even when told not to
        if (json.startsWith("```")) {
            int firstBrace = json.indexOf('{');
            int lastBrace = json.lastIndexOf('}');
            if (firstBrace >= 0 && lastBrace > firstBrace) json = json.substring(firstBrace, lastBrace + 1);
        }
        try {
            GeneratedContent g = mapper.readValue(json, GeneratedContent.class);
            if (g == null) throw new IllegalStateException("empty");
            return g;
        } catch (RuntimeException e) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "The LLM answer could not be read. Please try again.");
        }
    }

    private List<Scene> cleanScenes(List<Scene> in) {
        List<Scene> out = new ArrayList<>();
        if (in == null) return out;
        for (Scene s : in) {
            if (s == null || blank(s.title()) || blank(s.narration())) continue;
            List<String> bullets = new ArrayList<>();
            if (s.bullets() != null) {
                for (String b : s.bullets()) if (!blank(b)) bullets.add(b.trim());
            }
            out.add(new Scene(s.title().trim(), bullets, s.narration().trim()));
            if (out.size() >= MAX_SCENES) break;
        }
        return out;
    }

    private List<Quiz> cleanQuiz(List<Quiz> in) {
        List<Quiz> out = new ArrayList<>();
        if (in == null) return out;
        for (Quiz q : in) {
            if (q == null || blank(q.question()) || q.options() == null || q.options().size() != 4) continue;
            if (q.options().stream().anyMatch(this::blank)) continue;
            if (blank(q.correct()) || !q.correct().trim().toUpperCase().matches("[ABCD]")) continue;
            out.add(q);
            if (out.size() >= MAX_QUIZ) break;
        }
        return out;
    }

    private List<Card> cleanCards(List<Card> in) {
        List<Card> out = new ArrayList<>();
        if (in == null) return out;
        for (Card c : in) {
            if (c == null || blank(c.question()) || blank(c.answer())) continue;
            out.add(c);
            if (out.size() >= MAX_CARDS) break;
        }
        return out;
    }

    private String toJson(Object o) {
        try {
            return mapper.writeValueAsString(o);
        } catch (RuntimeException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Could not store the video script");
        }
    }

    private boolean blank(String s) { return s == null || s.isBlank(); }

    private String truncate(String s, int max) { return s.length() <= max ? s : s.substring(0, max) + "…"; }
}
