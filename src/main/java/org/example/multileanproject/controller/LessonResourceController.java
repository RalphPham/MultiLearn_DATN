package org.example.multileanproject.controller;

import lombok.RequiredArgsConstructor;
import org.example.multileanproject.dto.LessonResourceDTO;
import org.example.multileanproject.service.LessonResourceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/instructor/resources")
@RequiredArgsConstructor
public class LessonResourceController {

    private final LessonResourceService lessonResourceService;

    /** Danh sách tài nguyên của tôi */
    @GetMapping
    public ResponseEntity<List<LessonResourceDTO>> getMyResources() {
        return ResponseEntity.ok(lessonResourceService.getMyResources());
    }

    /** Tài nguyên theo bài học (instructor view) */
    @GetMapping("/lesson/{lessonId}")
    public ResponseEntity<List<LessonResourceDTO>> getResourcesByLesson(@PathVariable Long lessonId) {
        return ResponseEntity.ok(lessonResourceService.getResourcesByLessonId(lessonId));
    }

    /** Thống kê nhanh */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        return ResponseEntity.ok(lessonResourceService.getStats());
    }

    /** Danh sách bài học để chọn khi tải lên */
    @GetMapping("/lessons")
    public ResponseEntity<List<Map<String, Object>>> getLessonsForPicker() {
        return ResponseEntity.ok(lessonResourceService.getMyLessonsForPicker());
    }

    /** Lưu bản ghi tài nguyên sau khi upload file */
    @PostMapping
    public ResponseEntity<LessonResourceDTO> saveResource(@RequestBody LessonResourceDTO dto) {
        return ResponseEntity.ok(lessonResourceService.saveResource(dto));
    }

    /** Xóa tài nguyên */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteResource(@PathVariable Long id) {
        lessonResourceService.deleteResource(id);
        return ResponseEntity.ok().build();
    }
}
