package org.example.multileanproject.controller;



import lombok.RequiredArgsConstructor;
import org.example.multileanproject.dto.LessonRequest;
import org.example.multileanproject.entity.Lesson;
import org.example.multileanproject.service.LessonService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/lessons")
@RequiredArgsConstructor
public class LessonController {

    private final LessonService lessonService;

    // ================= CREATE =================
    // POST /api/lessons/section/{sectionId}
    @PostMapping("/section/{sectionId}")
    public ResponseEntity<Lesson> createLesson(
            @PathVariable Long sectionId,
            @RequestBody LessonRequest request
    ) {
        Lesson lesson = lessonService.createLesson(sectionId, request);
        return ResponseEntity.ok(lesson);
    }

    // ================= UPDATE =================
    // PUT /api/lessons/{lessonId}
    @PutMapping("/{lessonId}")
    public ResponseEntity<Lesson> updateLesson(
            @PathVariable Long lessonId,
            @RequestBody LessonRequest request
    ) {
        Lesson lesson = lessonService.updateLesson(lessonId, request);
        return ResponseEntity.ok(lesson);
    }

    // ================= DELETE =================
    // DELETE /api/lessons/{lessonId}
    @DeleteMapping("/{lessonId}")
    public ResponseEntity<Void> deleteLesson(@PathVariable Long lessonId) {
        lessonService.deleteLesson(lessonId);
        return ResponseEntity.noContent().build();
    }

    // ================= GET =================
    // GET /api/lessons/section/{sectionId}
    @GetMapping("/section/{sectionId}")
    public ResponseEntity<List<Lesson>> getLessonsBySection(
            @PathVariable Long sectionId
    ) {
        return ResponseEntity.ok(
                lessonService.getLessonsBySection(sectionId)
        );
    }

    // GET /api/lessons/preview/course/{courseId}
    @GetMapping("/preview/course/{courseId}")
    public ResponseEntity<List<Lesson>> getPreviewLessonsByCourse(
            @PathVariable Long courseId
    ) {
        return ResponseEntity.ok(
                lessonService.getPreviewLessonsByCourse(courseId)
        );
    }

    // ================= DURATION =================
    // GET /api/lessons/duration/section/{sectionId}
    @GetMapping("/duration/section/{sectionId}")
    public ResponseEntity<Integer> getTotalDurationBySection(
            @PathVariable Long sectionId
    ) {
        return ResponseEntity.ok(
                lessonService.getTotalDurationBySection(sectionId)
        );
    }

    // GET /api/lessons/duration/course/{courseId}
    @GetMapping("/duration/course/{courseId}")
    public ResponseEntity<Integer> getTotalDurationByCourse(
            @PathVariable Long courseId
    ) {
        return ResponseEntity.ok(
                lessonService.getTotalDurationByCourse(courseId)
        );
    }
}

