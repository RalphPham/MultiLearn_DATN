package org.example.multileanproject.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "course_change_requests")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class CourseChangeRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "instructor_id", nullable = false)
    private Instructor instructor;

    @Builder.Default
    @Column(name = "request_type", length = 50, nullable = false)
    private String requestType = "BASIC_INFO";

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private CourseChangeRequestStatus status = CourseChangeRequestStatus.PENDING;

    @Column(name = "payload_before", columnDefinition = "NVARCHAR(MAX)", nullable = false)
    private String payloadBefore;

    @Column(name = "payload_after", columnDefinition = "NVARCHAR(MAX)", nullable = false)
    private String payloadAfter;

    @Column(name = "request_note", columnDefinition = "NVARCHAR(500)")
    private String requestNote;

    @Column(name = "admin_note", columnDefinition = "NVARCHAR(500)")
    private String adminNote;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by")
    private Admin reviewedBy;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
