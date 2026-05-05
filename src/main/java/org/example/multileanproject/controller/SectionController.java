package org.example.multileanproject.controller;



import lombok.RequiredArgsConstructor;
import org.example.multileanproject.entity.Section;
import org.example.multileanproject.service.SectionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sections")
@RequiredArgsConstructor
public class SectionController {

    private final SectionService sectionService;

    // ================= CREATE =================
    // POST /api/sections/course/{courseId}
    @PostMapping("/course/{courseId}")
    public ResponseEntity<Section> createSection(
            @PathVariable Long courseId,
            @RequestBody Section section
    ) {
        return ResponseEntity.ok(
                sectionService.createSection(courseId, section)
        );
    }

    // ================= UPDATE =================
    // PUT /api/sections/{sectionId}
    @PutMapping("/{sectionId}")
    public ResponseEntity<Section> updateSection(
            @PathVariable Long sectionId,
            @RequestBody Section section
    ) {
        return ResponseEntity.ok(
                sectionService.updateSection(sectionId, section)
        );
    }

    // ================= DELETE =================
    // DELETE /api/sections/{sectionId}
    @DeleteMapping("/{sectionId}")
    public ResponseEntity<Void> deleteSection(
            @PathVariable Long sectionId
    ) {
        sectionService.deleteSection(sectionId);
        return ResponseEntity.noContent().build();
    }

    // ================= GET =================
    // GET /api/sections/course/{courseId}
    @GetMapping("/course/{courseId}")
    public ResponseEntity<List<Section>> getSectionsByCourse(
            @PathVariable Long courseId
    ) {
        return ResponseEntity.ok(
                sectionService.getSectionsByCourse(courseId)
        );
    }

    // GET /api/sections/{sectionId}
    @GetMapping("/{sectionId}")
    public ResponseEntity<Section> getSectionDetail(
            @PathVariable Long sectionId
    ) {
        return ResponseEntity.ok(
                sectionService.getSectionDetail(sectionId)
        );
    }
}

