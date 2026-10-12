package com.example.Studymaterial.controller;

import com.example.Studymaterial.entity.QuizQuestion;
import com.example.Studymaterial.service.QuizQuestionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class QuizQuestionController {

    private final QuizQuestionService service;

    @GetMapping("/topics/{topicId}/quiz")
    public List<QuizQuestion> getByTopic(@PathVariable Long topicId) {
        return service.getByTopic(topicId);
    }

    @PostMapping("/topics/{topicId}/quiz")
    @ResponseStatus(HttpStatus.CREATED)
    public QuizQuestion create(@PathVariable Long topicId, @Valid @RequestBody QuizQuestion q) {
        return service.create(topicId, q);
    }

    @PutMapping("/quiz/{id}")
    public QuizQuestion update(@PathVariable Long id, @Valid @RequestBody QuizQuestion q) {
        return service.update(id, q);
    }

    @DeleteMapping("/quiz/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
