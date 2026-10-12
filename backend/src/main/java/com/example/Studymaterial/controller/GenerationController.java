package com.example.Studymaterial.controller;

import com.example.Studymaterial.dto.GenerationSummary;
import com.example.Studymaterial.llm.ContentGenerationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class GenerationController {

    private final ContentGenerationService service;

    /**
     * Generate the micro-video script, quiz and flashcards for a topic.
     * Optional: ?parts=video,quiz,flashcards (default: all three). Existing items of those parts are replaced.
     */
    @PostMapping("/topics/{id}/generate")
    public GenerationSummary generate(@PathVariable Long id,
                                      @RequestParam(defaultValue = "video,quiz,flashcards") Set<String> parts) {
        return service.generate(id, parts);
    }
}
