package org.example.multileanproject.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "course_approval_history")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseApprovalHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "course_id")
    private Long courseId;

    @Column(name = "admin_id")
    private Long adminId;

    private String statusBefore;
    private String statusAfter;

    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String reason;

    private LocalDateTime createdAt;
}