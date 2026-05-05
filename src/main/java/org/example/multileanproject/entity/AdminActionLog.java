package org.example.multileanproject.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "admin_action_logs")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
@EntityListeners(AuditingEntityListener.class)
public class AdminActionLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Tên đăng nhập của admin thực hiện hành động */
    @Column(name = "admin_username", nullable = false, length = 100)
    private String adminUsername;

    /**
     * Loại hành động: APPROVE_COURSE, REJECT_COURSE, LOCK_USER,
     * UNLOCK_USER, DELETE_CATEGORY, UPDATE_ORDER_STATUS, DELETE_COUPON, ...
     */
    @Column(name = "action", nullable = false, length = 100)
    private String action;

    /** Mô tả ngắn: "Duyệt khóa học 'Java Spring Boot' (ID: 42)" */
    @Column(name = "description", columnDefinition = "NVARCHAR(500)")
    private String description;

    /** ID của object bị tác động (course_id, user_id, order_id...) */
    @Column(name = "target_id")
    private Long targetId;

    /** Loại object: COURSE, USER, ORDER, CATEGORY, COUPON */
    @Column(name = "target_type", length = 50)
    private String targetType;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
