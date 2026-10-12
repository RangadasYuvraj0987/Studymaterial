package com.example.Studymaterial.service;

import com.example.Studymaterial.entity.Syllabus;
import com.example.Studymaterial.entity.Unit;
import com.example.Studymaterial.repository.UnitRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UnitService {

    private final UnitRepository repo;
    private final SyllabusService syllabusService;

    public List<Unit> getBySyllabus(Long syllabusId) {
        return repo.findBySyllabusIdOrderByIdAsc(syllabusId);
    }

    public Unit getById(Long id) {
        return repo.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Unit not found"));
    }

    public Unit create(Long syllabusId, Unit unit) {
        Syllabus syllabus = syllabusService.getById(syllabusId);
        unit.setId(null);
        unit.setSyllabus(syllabus);
        unit.getTopics().clear();
        return repo.save(unit);
    }

    public Unit update(Long id, Unit data) {
        Unit existing = getById(id);
        existing.setName(data.getName());
        return repo.save(existing);
    }

    public void delete(Long id) {
        repo.delete(getById(id));
    }
}
