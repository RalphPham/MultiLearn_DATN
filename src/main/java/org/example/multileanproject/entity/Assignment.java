package org.example.multileanproject.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "assignments")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Assignment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;          // Tiêu đề (VD: Bài tập thực hành React)

    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String description;    // Nội dung đề bài

    private Long sectionId;        // Thuộc chương nào
    private Long courseId;         // Thuộc khóa học nào

    private Integer duration;      // Thời gian làm bài (phút)

    private LocalDateTime createdAt = LocalDateTime.now();
}