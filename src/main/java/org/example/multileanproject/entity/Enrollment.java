package org.example.multileanproject.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "enrollments", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"student_id", "course_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class Enrollment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @CreatedDate
    @Column(name = "enrolled_at", updatable = false)
    private LocalDateTime enrolledAt;

    @Builder.Default
    @Column(name = "progress")
    private BigDecimal progress = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "status")
    private String status = "ACTIVE";

    @Builder.Default
    @Column(name = "is_course_completed", nullable = false)
    private Boolean isCourseCompleted = false;

    @Column(name = "course_completed_at")
    private LocalDateTime courseCompletedAt;

    @Builder.Default
    @Column(name = "completion_email_sent", nullable = false)
    private Boolean completionEmailSent = false;

    @Builder.Default
    @Column(name = "certificate_issued", nullable = false)
    private Boolean certificateIssued = false;

    /** Null = mua vĩnh viễn. Có giá trị = thuê, hết hạn sau thời điểm này */
    @Column(name = "rental_expires_at")
    private LocalDateTime rentalExpiresAt;

    /** Null = chưa học bài nào. Set khi học viên hoàn thành bài đầu tiên → khoá quyền hoàn tiền */
    @Column(name = "refund_locked_at")
    private LocalDateTime refundLockedAt;
}
