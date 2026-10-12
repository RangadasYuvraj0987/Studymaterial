package com.example.Studymaterial.controller;

import com.example.Studymaterial.entity.Unit;
import com.example.Studymaterial.service.UnitService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class UnitController {

    private final UnitService service;

    @GetMapping("/syllabi/{syllabusId}/units")
    public List<Unit> getBySyllabus(@PathVariable Long syllabusId) {
        return service.getBySyllabus(syllabusId);
    }

    @PostMapping("/syllabi/{syllabusId}/units")
    @ResponseStatus(HttpStatus.CREATED)
    public Unit create(@PathVariable Long syllabusId, @Valid @RequestBody Unit unit) {
        return service.create(syllabusId, unit);
    }

    @GetMapping("/units/{id}")
    public Unit getOne(@PathVariable Long id) {
        return service.getById(id);
    }

    @PutMapping("/units/{id}")
    public Unit update(@PathVariable Long id, @Valid @RequestBody Unit unit) {
        return service.update(id, unit);
    }

    @DeleteMapping("/units/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
