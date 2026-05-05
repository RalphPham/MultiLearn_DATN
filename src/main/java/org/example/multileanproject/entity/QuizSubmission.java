package org.example.multileanproject.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "quiz_submissions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuizSubmission {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "enrollment_id")
    private Long enrollmentId; // Lưu ID ghi danh

    @Column(name = "quiz_id")
    private Long quizId;

    private BigDecimal score; // Điểm số đạt được

    @Column(name = "is_passed")
    private Boolean isPassed;

    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;
}
