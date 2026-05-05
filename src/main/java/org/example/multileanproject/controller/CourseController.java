package org.example.multileanproject.controller;

import lombok.RequiredArgsConstructor;
import org.example.multileanproject.dto.CourseDetailDTO;
import org.example.multileanproject.dto.CourseListDTO;
import org.example.multileanproject.dto.CourseRequestDTO;
import org.example.multileanproject.dto.InstructorCourseListItemDTO;
import org.example.multileanproject.entity.Course;
import org.example.multileanproject.service.CourseService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;

    @GetMapping(value = {"", "/search"})
    public ResponseEntity<Page<CourseListDTO>> searchCourses(
            @RequestParam Map<String, String> params,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Page<CourseListDTO> result = courseService.searchCoursesPublic(params, page, size);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/all")
    @PreAuthorize("hasAuthority('ADMIN')")
    public List<Course> getAllCourses() {
        return courseService.getAllCourses();
    }

    @GetMapping("/my-courses")
    public ResponseEntity<List<InstructorCourseListItemDTO>> getMyCourses() {
        return ResponseEntity.ok(courseService.getMyCourses(getRequiredEmail()));
    }

    @GetMapping("/slug/{slug}")
    public ResponseEntity<CourseDetailDTO> getCourseDetailBySlug(@PathVariable String slug) {
        return ResponseEntity.ok(courseService.getCourseDetailBySlug(slug, getCurrentEmailOrNull()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CourseDetailDTO> getCourseDetail(@PathVariable Long id) {
        return ResponseEntity.ok(courseService.getCourseDetail(id, getCurrentEmailOrNull()));
    }

    @PostMapping
    public ResponseEntity<Course> createCourse(@RequestBody Course course) {
        return ResponseEntity.ok(courseService.createCourseWithInstructor(course, getRequiredEmail()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Course> updateCourse(@PathVariable Long id, @RequestBody CourseRequestDTO request) {
        return ResponseEntity.ok(courseService.updateCourse(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('INSTRUCTOR')")
    public ResponseEntity<Void> deleteCourse(@PathVariable Long id) {
        courseService.deleteCourse(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/submit")
    public ResponseEntity<Void> submitForApproval(@PathVariable Long id) {
        courseService.submitForApproval(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/toggle-sale")
    @PreAuthorize("hasAuthority('INSTRUCTOR')")
    public ResponseEntity<?> toggleSaleStatus(@PathVariable Long id) {
        courseService.toggleSaleStatus(id, getRequiredEmail());
        return ResponseEntity.ok(Map.of("message", "Đã cập nhật trạng thái bán."));
    }

    @GetMapping("/{id:[0-9]+}/learn")
    public ResponseEntity<CourseDetailDTO> getLearningCourse(@PathVariable Long id) {
        String currentEmail = getRequiredEmail();
        return ResponseEntity.ok(courseService.getLearningCourseDetail(id, currentEmail));
    }

    private String getCurrentEmailOrNull() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null) return null;
        if (!authentication.isAuthenticated()) return null;
        if (authentication instanceof AnonymousAuthenticationToken) return null;

        String name = authentication.getName();
        if (name == null || "anonymousUser".equalsIgnoreCase(name)) {
            return null;
        }

        return name;
    }

    private String getRequiredEmail() {
        String email = getCurrentEmailOrNull();
        if (email == null || email.isBlank()) {
            throw new RuntimeException("Bạn chưa đăng nhập hoặc token không hợp lệ.");
        }
        return email;
    }
}
