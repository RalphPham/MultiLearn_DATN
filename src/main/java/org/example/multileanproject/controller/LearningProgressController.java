package org.example.multileanproject.controller;

import lombok.RequiredArgsConstructor;
import org.example.multileanproject.entity.Lesson;
import org.example.multileanproject.repository.LessonRepository;
import org.example.multileanproject.service.LearningProgressService;
import org.example.multileanproject.service.MinioUploadService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/learning")
@RequiredArgsConstructor
public class LearningProgressController {

    private final LearningProgressService learningProgressService;
    private final MinioUploadService minioUploadService;
    private final LessonRepository lessonRepository;

    @PostMapping("/lessons/{lessonId}/complete")
    public ResponseEntity<?> markLessonCompleted(@PathVariable Long lessonId) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        double progress = learningProgressService.markLessonCompleted(email, lessonId);
        return ResponseEntity.ok(Map.of("message", "Đã lưu tiến trình học tập", "progress", progress));
    }

    @PostMapping("/courses/{courseId}/start")
    public ResponseEntity<?> markCourseStarted(@PathVariable Long courseId) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        learningProgressService.markCourseStarted(email, courseId);
        return ResponseEntity.ok(Map.of("message", "ok"));
    }

    @GetMapping("/presign")
    public ResponseEntity<?> getPresignedUrlForUpload(@RequestParam String objectKey) {
        String url = minioUploadService.getPresignedUrl(objectKey, 120);
        return ResponseEntity.ok(Map.of("url", url));
    }

    @GetMapping("/lessons/{lessonId}/video-url")
    public ResponseEntity<?> getLessonVideoUrl(@PathVariable Long lessonId) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        String url = learningProgressService.getLessonVideoUrl(email, lessonId);
        return ResponseEntity.ok(Map.of("url", url));
    }

    @GetMapping("/lessons/{lessonId}/document-url")
    public ResponseEntity<?> getLessonDocumentUrl(@PathVariable Long lessonId) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        String url = learningProgressService.getLessonDocumentUrl(email, lessonId);
        return ResponseEntity.ok(Map.of("url", url));
    }

    // Public endpoint — không cần login, chỉ hoạt động với bài isPreview=true
    @GetMapping("/lessons/{lessonId}/preview-url")
    public ResponseEntity<?> getPreviewUrl(@PathVariable Long lessonId) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bài học"));

        if (!Boolean.TRUE.equals(lesson.getIsPreview())) {
            return ResponseEntity.status(403).body(Map.of("error", "Bài học này không phải bài xem thử."));
        }

        if (lesson.getVideoUrl() == null || lesson.getVideoUrl().isBlank()) {
            return ResponseEntity.status(404).body(Map.of("error", "Bài học này chưa có video."));
        }

        String objectKey = minioUploadService.extractObjectKey(lesson.getVideoUrl());
        String url = minioUploadService.getPresignedUrl(objectKey, 30);
        return ResponseEntity.ok(Map.of("url", url));
    }
}