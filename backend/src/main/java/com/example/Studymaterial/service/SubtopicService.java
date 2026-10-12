package com.example.Studymaterial.service;

import com.example.Studymaterial.entity.Subtopic;
import com.example.Studymaterial.entity.Topic;
import com.example.Studymaterial.repository.SubtopicRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SubtopicService {

    private final SubtopicRepository repo;
    private final TopicService topicService;

    public List<Subtopic> getByTopic(Long topicId) {
        return repo.findByTopicIdOrderByIdAsc(topicId);
    }

    public Subtopic getById(Long id) {
        return repo.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Subtopic not found"));
    }

    public Subtopic create(Long topicId, Subtopic s) {
        Topic topic = topicService.getById(topicId);
        s.setId(null);
        s.setTopic(topic);
        return repo.save(s);
    }

    public Subtopic update(Long id, Subtopic data) {
        Subtopic existing = getById(id);
        existing.setName(data.getName());
        existing.setContent(data.getContent());
        return repo.save(existing);
    }

    public void delete(Long id) {
        repo.delete(getById(id));
    }
}
