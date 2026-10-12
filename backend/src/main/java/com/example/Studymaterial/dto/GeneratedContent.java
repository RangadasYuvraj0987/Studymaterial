package com.example.Studymaterial.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/** The JSON shape we ask the LLM to return. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record GeneratedContent(List<Scene> scenes, List<Quiz> quiz, List<Card> flashcards) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Scene(String title, List<String> bullets, String narration) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Quiz(String question, List<String> options, String correct) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Card(String question, String answer) {}
}
