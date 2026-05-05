package org.example.multileanproject.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {

    public enum NotificationCategory {
        SYSTEM_NOTIFICATION,
        INSTRUCTOR_ANNOUNCEMENT
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(columnDefinition = "NVARCHAR(MAX)", nullable = false)
    private String message;

    @Column(name = "target_url", length = 500)
    private String targetUrl;

    @Column(nullable = false, length = 100)
    private String type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private NotificationCategory category;

    @Column(name = "is_read", nullable = false)
    @Builder.Default
    private Boolean isRead = false;

    @Column(name = "is_important", nullable = false)
    @Builder.Default
    private Boolean isImportant = false;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    // userId ở đây là ID của tài khoản trong bảng students
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "course_name", length = 255)
    private String courseName;

    @Column(name = "student_name", length = 255)
    private String studentName;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (isRead == null) {
            isRead = false;
        }
        if (isImportant == null) {
            isImportant = false;
        }
        if (type == null || type.isBlank()) {
            type = "SYSTEM";
        }
        if (category == null) {
            category = NotificationCategory.SYSTEM_NOTIFICATION;
        }
    }
}