package org.example.multileanproject.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "certificates")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Certificate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "certificate_code", nullable = false, unique = true)
    private String certificateCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "enrollment_id")
    private Enrollment enrollment;

    @Column(name = "issued_at")
    private LocalDateTime issuedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "verify_url")
    private String verifyUrl;

    @Column(name = "pdf_url")
    private String pdfUrl;

    @Builder.Default
    @Column(name = "is_active")
    private Boolean isActive = true;
}