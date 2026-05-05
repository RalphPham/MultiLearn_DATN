package org.example.multileanproject.controller;

import lombok.RequiredArgsConstructor;
import org.example.multileanproject.dto.MyCourseResponse;
import org.example.multileanproject.service.EnrollmentService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/enrollments") // Khớp với đường dẫn Frontend gọi
@RequiredArgsConstructor
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    // API: /api/enrollments/my-courses
    @GetMapping("/my-courses")
    public ResponseEntity<?> getMyCourses() {
        try {
            // 1. Lấy email từ Token đăng nhập hiện tại
            String email = SecurityContextHolder.getContext().getAuthentication().getName();

            // Check nhanh nếu chưa đăng nhập
            if (email == null || email.equals("anonymousUser")) {
                return ResponseEntity.status(401).body("Bạn chưa đăng nhập!");
            }

            // 2. Gọi Service xử lý
            List<MyCourseResponse> courses = enrollmentService.getMyCourses(email);

            // 3. Trả về kết quả
            return ResponseEntity.ok(courses);

        } catch (Exception e) {
            e.printStackTrace(); // In lỗi ra console để debug
            return ResponseEntity.internalServerError().body("Lỗi Server: " + e.getMessage());
        }
    }
}