package com.example.Studymaterial.service;

import com.example.Studymaterial.entity.Subtopic;
import com.example.Studymaterial.entity.Syllabus;
import com.example.Studymaterial.entity.Topic;
import com.example.Studymaterial.entity.Unit;
import com.example.Studymaterial.repository.SyllabusRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SyllabusService {

    private final SyllabusRepository repo;

    public List<Syllabus> getAll() {
        return repo.findAll();
    }

    public Syllabus getById(Long id) {
        return repo.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Syllabus not found"));
    }

    public Syllabus create(Syllabus s) {
        s.setId(null);
        s.getUnits().clear();
        return repo.save(s);
    }

    public Syllabus update(Long id, Syllabus data) {
        Syllabus existing = getById(id);
        existing.setSubjectName(data.getSubjectName());
        existing.setDescription(data.getDescription());
        return repo.save(existing);
    }

    public void delete(Long id) {
        repo.delete(getById(id));
    }

    /** Saves a whole syllabus tree (units, topics, subtopics) in one request. */
    @Transactional
    public Syllabus importTree(Syllabus s) {
        s.setId(null);
        for (Unit u : s.getUnits()) {
            u.setId(null);
            u.setSyllabus(s);
            for (Topic t : u.getTopics()) {
                t.setId(null);
                t.setUnit(u);
                for (Subtopic st : t.getSubtopics()) {
                    st.setId(null);
                    st.setTopic(t);
                }
            }
        }
        return repo.save(s);
    }
}
