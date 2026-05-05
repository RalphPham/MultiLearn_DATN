package org.example.multileanproject.controller;

import lombok.RequiredArgsConstructor;
import org.example.multileanproject.dto.BroadcastRequest;
import org.example.multileanproject.entity.Notification;
import org.example.multileanproject.entity.Student;
import org.example.multileanproject.repository.CourseRepository;
import org.example.multileanproject.repository.EnrollmentRepository;
import org.example.multileanproject.repository.NotificationRepository;
import org.example.multileanproject.repository.StudentRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/instructor")
@RequiredArgsConstructor
public class BroadcastController {

    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final NotificationRepository notificationRepository;
    private final StudentRepository studentRepository;

    /**
     * POST /api/instructor/broadcast
     * Gửi thông báo tới toàn bộ học viên đang enrolled trong khóa học.
     */
    @PostMapping("/broadcast")
    public ResponseEntity<?> broadcast(
            @RequestBody BroadcastRequest request,
            Authentication authentication) {

        if (authentication == null) return ResponseEntity.status(401).build();
        String email = authentication.getName();

        // Kiểm tra instructor sở hữu course
        boolean owns = courseRepository.findByInstructorEmail(email)
                .stream().anyMatch(c -> c.getId().equals(request.getCourseId()));
        if (!owns) {
            return ResponseEntity.status(403).body(Map.of("message", "Bạn không có quyền gửi thông báo cho khóa học này."));
        }

        var courseOpt = courseRepository.findById(request.getCourseId());
        if (courseOpt.isEmpty()) return ResponseEntity.notFound().build();

        String courseName = courseOpt.get().getTitle();

        // Lấy giảng viên (sender name)
        String instructorName = studentRepository.findByEmail(email)
                .map(Student::getFullName).orElse("Giảng viên");

        // Lấy danh sách học viên đang enrolled
        List<Student> students = enrollmentRepository.findActiveStudentsByCourseId(request.getCourseId());

        if (students.isEmpty()) {
            return ResponseEntity.ok(Map.of("sentCount", 0, "message", "Khóa học chưa có học viên nào."));
        }

        // Tạo notification cho từng học viên
        List<Notification> notifications = new ArrayList<>();
        for (Student student : students) {
            notifications.add(Notification.builder()
                    .userId(student.getId())
                    .title(request.getTitle())
                    .message(request.getMessage())
                    .type("BROADCAST")
                    .category(Notification.NotificationCategory.INSTRUCTOR_ANNOUNCEMENT)
                    .isImportant(true)
                    .isRead(false)
                    .courseName(courseName)
                    .studentName(instructorName)
                    .targetUrl("/learning/course/" + request.getCourseId())
                    .createdAt(LocalDateTime.now())
                    .build());
        }

        notificationRepository.saveAll(notifications);

        return ResponseEntity.ok(Map.of(
                "sentCount", notifications.size(),
                "message", "Đã gửi thông báo tới " + notifications.size() + " học viên."
        ));
    }
}
