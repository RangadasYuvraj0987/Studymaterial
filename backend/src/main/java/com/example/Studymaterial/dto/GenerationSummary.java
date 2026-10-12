package com.example.Studymaterial.dto;

/** What the generate endpoint returns: how many items were created. */
public record GenerationSummary(int scenes, int quizQuestions, int flashcards) {}
