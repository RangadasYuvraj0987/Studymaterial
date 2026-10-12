package com.example.Studymaterial.repository;

import com.example.Studymaterial.entity.Unit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UnitRepository extends JpaRepository<Unit, Long> {
    List<Unit> findBySyllabusIdOrderByIdAsc(Long syllabusId);
}
