package com.example.Studymaterial.controller;

import com.example.Studymaterial.entity.Flashcard;
import com.example.Studymaterial.service.FlashcardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class FlashcardController {

    private final FlashcardService service;

    @GetMapping("/topics/{topicId}/flashcards")
    public List<Flashcard> getByTopic(@PathVariable Long topicId) {
        return service.getByTopic(topicId);
    }

    @PostMapping("/topics/{topicId}/flashcards")
    @ResponseStatus(HttpStatus.CREATED)
    public Flashcard create(@PathVariable Long topicId, @Valid @RequestBody Flashcard f) {
        return service.create(topicId, f);
    }

    @PutMapping("/flashcards/{id}")
    public Flashcard update(@PathVariable Long id, @Valid @RequestBody Flashcard f) {
        return service.update(id, f);
    }

    @DeleteMapping("/flashcards/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
