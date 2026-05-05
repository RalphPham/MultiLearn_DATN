package org.example.multileanproject.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "lesson_questions")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class LessonQuestion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "lesson_id")
    private Long lessonId;

    // Map thẳng vào bảng Course để lấy Tên khóa học
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id")
    private Course course;

    // Map thẳng vào bảng Student để lấy Tên học viên
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id")
    private Student student;

    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String content;

    @Column(name = "answer_count")
    private int answerCount;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LessonAnswer> answers = new ArrayList<>();
}