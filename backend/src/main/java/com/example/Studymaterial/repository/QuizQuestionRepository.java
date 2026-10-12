package com.example.Studymaterial.repository;

import com.example.Studymaterial.entity.QuizQuestion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuizQuestionRepository extends JpaRepository<QuizQuestion, Long> {
    List<QuizQuestion> findByTopicIdOrderByIdAsc(Long topicId);
}
