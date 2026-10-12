package com.example.Studymaterial.controller;

import com.example.Studymaterial.entity.Subtopic;
import com.example.Studymaterial.service.SubtopicService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class SubtopicController {

    private final SubtopicService service;

    @GetMapping("/topics/{topicId}/subtopics")
    public List<Subtopic> getByTopic(@PathVariable Long topicId) {
        return service.getByTopic(topicId);
    }

    @PostMapping("/topics/{topicId}/subtopics")
    @ResponseStatus(HttpStatus.CREATED)
    public Subtopic create(@PathVariable Long topicId, @Valid @RequestBody Subtopic s) {
        return service.create(topicId, s);
    }

    @GetMapping("/subtopics/{id}")
    public Subtopic getOne(@PathVariable Long id) {
        return service.getById(id);
    }

    @PutMapping("/subtopics/{id}")
    public Subtopic update(@PathVariable Long id, @Valid @RequestBody Subtopic s) {
        return service.update(id, s);
    }

    @DeleteMapping("/subtopics/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
