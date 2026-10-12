package com.example.Studymaterial.controller;

import com.example.Studymaterial.entity.Syllabus;
import com.example.Studymaterial.service.SyllabusService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/syllabi")
@RequiredArgsConstructor
public class SyllabusController {

    private final SyllabusService service;

    @GetMapping
    public List<Syllabus> getAll() {
        return service.getAll();
    }

    @GetMapping("/{id}")
    public Syllabus getOne(@PathVariable Long id) {
        return service.getById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Syllabus create(@Valid @RequestBody Syllabus s) {
        return service.create(s);
    }

    @PostMapping("/import")
    @ResponseStatus(HttpStatus.CREATED)
    public Syllabus importTree(@Valid @RequestBody Syllabus s) {
        return service.importTree(s);
    }

    @PutMapping("/{id}")
    public Syllabus update(@PathVariable Long id, @Valid @RequestBody Syllabus s) {
        return service.update(id, s);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
