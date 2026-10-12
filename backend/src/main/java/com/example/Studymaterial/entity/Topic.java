package com.example.Studymaterial.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Topic {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Topic name is required")
    private String name;

    @Column(columnDefinition = "TEXT")
    private String content;

    private String videoUrl;

    // AI-generated micro-video: a JSON array of scenes {title, bullets[], narration}
    @Column(columnDefinition = "TEXT")
    private String videoScript;

    // NOT_STARTED / DEVELOPING / MASTERED
    private String status = "NOT_STARTED";

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unit_id")
    @JsonIgnore
    private Unit unit;

    @OneToMany(mappedBy = "topic", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC")
    private List<Subtopic> subtopics = new ArrayList<>();

    @OneToMany(mappedBy = "topic", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Flashcard> flashcards = new ArrayList<>();

    @OneToMany(mappedBy = "topic", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<QuizQuestion> quizQuestions = new ArrayList<>();

    // Lets the frontend build a "Back to syllabus" link
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public Long getSyllabusId() {
        if (unit == null || unit.getSyllabus() == null) return null;
        return unit.getSyllabus().getId();
    }
}
