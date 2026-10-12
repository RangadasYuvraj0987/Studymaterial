package com.example.Studymaterial.service;

import com.example.Studymaterial.entity.Flashcard;
import com.example.Studymaterial.entity.Topic;
import com.example.Studymaterial.repository.FlashcardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FlashcardService {

    private final FlashcardRepository repo;
    private final TopicService topicService;

    public List<Flashcard> getByTopic(Long topicId) {
        return repo.findByTopicIdOrderByIdAsc(topicId);
    }

    public Flashcard getById(Long id) {
        return repo.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Flashcard not found"));
    }

    public Flashcard create(Long topicId, Flashcard f) {
        Topic topic = topicService.getById(topicId);
        f.setId(null);
        f.setTopic(topic);
        return repo.save(f);
    }

    public Flashcard update(Long id, Flashcard data) {
        Flashcard existing = getById(id);
        existing.setQuestion(data.getQuestion());
        existing.setAnswer(data.getAnswer());
        return repo.save(existing);
    }

    public void delete(Long id) {
        repo.delete(getById(id));
    }
}
