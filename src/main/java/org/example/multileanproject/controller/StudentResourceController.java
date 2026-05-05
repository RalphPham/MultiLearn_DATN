package org.example.multileanproject.controller;

import lombok.RequiredArgsConstructor;
import org.example.multileanproject.dto.LessonResourceDTO;
import org.example.multileanproject.service.LessonResourceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/resources")
@RequiredArgsConstructor
public class StudentResourceController {

    private final LessonResourceService lessonResourceService;

    /** Học viên xem tài nguyên đính kèm của bài học */
    @GetMapping("/lesson/{lessonId}")
    public ResponseEntity<List<LessonResourceDTO>> getResourcesByLesson(@PathVariable Long lessonId) {
        return ResponseEntity.ok(lessonResourceService.getResourcesByLessonIdForStudent(lessonId));
    }
}
