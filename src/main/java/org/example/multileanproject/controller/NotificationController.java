package org.example.multileanproject.controller;

import lombok.RequiredArgsConstructor;
import org.example.multileanproject.entity.Notification;
import org.example.multileanproject.entity.Student;
import org.example.multileanproject.repository.AdminRepository;
import org.example.multileanproject.repository.NotificationRepository;
import org.example.multileanproject.repository.StudentRepository;
import org.example.multileanproject.service.NotificationService;
import org.example.multileanproject.util.TextEncodingUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final NotificationRepository notificationRepository;
    private final StudentRepository studentRepository;
    private final AdminRepository adminRepository;

    private Long getCurrentUserId() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        // Tìm trong bảng students trước (STUDENT + INSTRUCTOR)
        var student = studentRepository.findByEmail(email);
        if (student.isPresent()) return student.get().getId();

        // Fallback: tìm trong bảng admins
        return adminRepository.findByUsername(email)
                .map(admin -> admin.getId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng hiện tại"));
    }

    // ====================================================================
    // 🔥 PHẦN 1: CÁC API MỚI DÀNH RIÊNG CHO CHUÔNG HỌC VIÊN (Trang chủ)
    // ====================================================================

    @GetMapping("/student/unread-count")
    public ResponseEntity<Long> getStudentUnreadCount() {
        return ResponseEntity.ok(notificationRepository.countUnreadStudentNotifications(getCurrentUserId()));
    }

    @GetMapping("/student/recent")
    public ResponseEntity<List<Notification>> getStudentRecentNotifications() {
        // Lấy 10 thông báo học viên gần nhất
        List<Notification> notifications = notificationRepository.findRecentStudentNotifications(
                getCurrentUserId(),
                PageRequest.of(0, 10)
        );
        return ResponseEntity.ok(normalizeNotifications(notifications));
    }

    @PutMapping("/student/mark-all-read")
    @Transactional
    public ResponseEntity<?> markAllStudentRead() {
        notificationRepository.markAllStudentAsRead(getCurrentUserId());
        return ResponseEntity.ok(Map.of("message", "Đã đánh dấu tất cả thông báo học viên là đã đọc"));
    }

    // ====================================================================
    // 🔥 PHẦN 2: CÁC API MỚI DÀNH RIÊNG CHO CHUÔNG GIẢNG VIÊN (Studio)
    // ====================================================================

    @GetMapping("/instructor/unread-count")
    public ResponseEntity<Long> getInstructorUnreadCount() {
        return ResponseEntity.ok(notificationRepository.countUnreadInstructorNotifications(getCurrentUserId()));
    }

    @GetMapping("/instructor/recent")
    public ResponseEntity<List<Notification>> getInstructorRecentNotifications() {
        // Lấy 10 thông báo giảng viên gần nhất
        List<Notification> notifications = notificationRepository.findRecentInstructorNotifications(
                getCurrentUserId(),
                PageRequest.of(0, 10)
        );
        return ResponseEntity.ok(normalizeNotifications(notifications));
    }

    @PutMapping("/instructor/mark-all-read")
    @Transactional
    public ResponseEntity<?> markAllInstructorRead() {
        notificationRepository.markAllInstructorAsRead(getCurrentUserId());
        return ResponseEntity.ok(Map.of("message", "Đã đánh dấu tất cả thông báo giảng viên là đã đọc"));
    }

    // ====================================================================
    // 🛡️ PHẦN 3: CÁC API CŨ CỦA BẠN (Dùng cho trang Search / Xem tất cả)
    // ====================================================================

    @GetMapping("/my-recent")
    public ResponseEntity<List<Notification>> getMyRecent() {
        return ResponseEntity.ok(normalizeNotifications(notificationService.getRecentNotifications(getCurrentUserId())));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<Long> getUnreadCount() {
        return ResponseEntity.ok(notificationService.countUnread(getCurrentUserId()));
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<?> markAsRead(@PathVariable Long id) {
        notificationService.markAsRead(id, getCurrentUserId());
        return ResponseEntity.ok(Map.of("message", "Đã đánh dấu đã đọc"));
    }

    @PutMapping("/mark-all-read")
    public ResponseEntity<?> markAllAsRead() {
        notificationService.markAllAsRead(getCurrentUserId());
        return ResponseEntity.ok(Map.of("message", "Đã đánh dấu tất cả là đã đọc"));
    }

    @GetMapping({"", "/search"})
    public ResponseEntity<Page<Notification>> searchNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Boolean isImportant,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String courseName,
            @RequestParam(required = false) String studentName,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate
    ) {
        Long userId = getCurrentUserId();

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Notification.NotificationCategory categoryEnum = null;
        if (category != null && !category.isBlank() && !"ALL".equalsIgnoreCase(category)) {
            categoryEnum = Notification.NotificationCategory.valueOf(category.toUpperCase());
        }

        LocalDateTime start = null;
        LocalDateTime end = null;

        if (startDate != null && !startDate.isBlank()) {
            start = LocalDate.parse(startDate).atStartOfDay();
        }
        if (endDate != null && !endDate.isBlank()) {
            end = LocalDate.parse(endDate).atTime(LocalTime.MAX);
        }

        Page<Notification> result = notificationService.searchNotifications(
                userId,
                isImportant,
                categoryEnum,
                blankToNull(courseName),
                blankToNull(studentName),
                start,
                end,
                pageable
        );

        return ResponseEntity.ok(result.map(this::normalizeNotification));
    }

    @GetMapping("/my-all")
    public ResponseEntity<Page<Notification>> getMyAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Notification> pageResult = notificationRepository.findByUserIdOrderByCreatedAtDesc(getCurrentUserId(), pageable);
        return ResponseEntity.ok(pageResult.map(this::normalizeNotification));
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private List<Notification> normalizeNotifications(List<Notification> notifications) {
        return notifications.stream().map(this::normalizeNotification).toList();
    }

    private Notification normalizeNotification(Notification source) {
        if (source == null) {
            return null;
        }
        return Notification.builder()
                .id(source.getId())
                .title(TextEncodingUtil.normalize(source.getTitle()))
                .message(TextEncodingUtil.normalize(source.getMessage()))
                .targetUrl(source.getTargetUrl())
                .type(source.getType())
                .category(source.getCategory())
                .isRead(source.getIsRead())
                .isImportant(source.getIsImportant())
                .createdAt(source.getCreatedAt())
                .userId(source.getUserId())
                .courseName(TextEncodingUtil.normalize(source.getCourseName()))
                .studentName(TextEncodingUtil.normalize(source.getStudentName()))
                .build();
    }
}
