package com.example.Studymaterial.service;

import com.example.Studymaterial.entity.QuizQuestion;
import com.example.Studymaterial.entity.Topic;
import com.example.Studymaterial.repository.QuizQuestionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class QuizQuestionService {

    private final QuizQuestionRepository repo;
    private final TopicService topicService;

    public List<QuizQuestion> getByTopic(Long topicId) {
        return repo.findByTopicIdOrderByIdAsc(topicId);
    }

    public QuizQuestion getById(Long id) {
        return repo.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Quiz question not found"));
    }

    public QuizQuestion create(Long topicId, QuizQuestion q) {
        Topic topic = topicService.getById(topicId);
        q.setId(null);
        q.setTopic(topic);
        return repo.save(q);
    }

    public QuizQuestion update(Long id, QuizQuestion data) {
        QuizQuestion existing = getById(id);
        existing.setQuestion(data.getQuestion());
        existing.setOptionA(data.getOptionA());
        existing.setOptionB(data.getOptionB());
        existing.setOptionC(data.getOptionC());
        existing.setOptionD(data.getOptionD());
        existing.setCorrectOption(data.getCorrectOption());
        return repo.save(existing);
    }

    public void delete(Long id) {
        repo.delete(getById(id));
    }
}
