package com.example.Studymaterial.repository;

import com.example.Studymaterial.entity.Flashcard;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FlashcardRepository extends JpaRepository<Flashcard, Long> {
    List<Flashcard> findByTopicIdOrderByIdAsc(Long topicId);
}
