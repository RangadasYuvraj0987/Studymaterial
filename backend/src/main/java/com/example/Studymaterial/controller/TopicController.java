package com.example.Studymaterial.controller;

import com.example.Studymaterial.entity.Topic;
import com.example.Studymaterial.service.TopicService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class TopicController {

    private final TopicService service;

    @GetMapping("/units/{unitId}/topics")
    public List<Topic> getByUnit(@PathVariable Long unitId) {
        return service.getByUnit(unitId);
    }

    @PostMapping("/units/{unitId}/topics")
    @ResponseStatus(HttpStatus.CREATED)
    public Topic create(@PathVariable Long unitId, @Valid @RequestBody Topic topic) {
        return service.create(unitId, topic);
    }

    @GetMapping("/topics/{id}")
    public Topic getOne(@PathVariable Long id) {
        return service.getById(id);
    }

    @PutMapping("/topics/{id}")
    public Topic update(@PathVariable Long id, @Valid @RequestBody Topic topic) {
        return service.update(id, topic);
    }

    /** Student progress: body is {"status":"DEVELOPING"} */
    @PatchMapping("/topics/{id}/status")
    public Topic updateStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        return service.updateStatus(id, body.get("status"));
    }

    @DeleteMapping("/topics/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
