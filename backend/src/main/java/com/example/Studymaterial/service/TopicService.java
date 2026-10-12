package com.example.Studymaterial.service;

import com.example.Studymaterial.entity.Topic;
import com.example.Studymaterial.entity.Unit;
import com.example.Studymaterial.repository.TopicRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TopicService {

    private static final List<String> STATUSES = List.of("NOT_STARTED", "DEVELOPING", "MASTERED");

    private final TopicRepository repo;
    private final UnitService unitService;

    public List<Topic> getByUnit(Long unitId) {
        return repo.findByUnitIdOrderByIdAsc(unitId);
    }

    public Topic getById(Long id) {
        return repo.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Topic not found"));
    }

    public Topic create(Long unitId, Topic topic) {
        Unit unit = unitService.getById(unitId);
        topic.setId(null);
        topic.setUnit(unit);
        topic.getSubtopics().clear();
        return repo.save(topic);
    }

    public Topic update(Long id, Topic data) {
        Topic existing = getById(id);
        existing.setName(data.getName());
        existing.setContent(data.getContent());
        existing.setVideoUrl(data.getVideoUrl());
        if (data.getStatus() != null) {
            checkStatus(data.getStatus());
            existing.setStatus(data.getStatus());
        }
        return repo.save(existing);
    }

    public Topic updateStatus(Long id, String status) {
        checkStatus(status);
        Topic existing = getById(id);
        existing.setStatus(status);
        return repo.save(existing);
    }

    public void delete(Long id) {
        repo.delete(getById(id));
    }

    private void checkStatus(String status) {
        if (status == null || !STATUSES.contains(status)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Status must be NOT_STARTED, DEVELOPING or MASTERED");
        }
    }
}
