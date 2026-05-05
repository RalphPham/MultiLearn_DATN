package org.example.multileanproject.dto;

import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LessonResourceDTO {
    private Long id;
    private Long lessonId;
    private Long instructorId;
    private String fileName;
    private String fileUrl;
    private String fileType;
    private Long fileSize;
    private LocalDateTime uploadedAt;

    // Enriched from Lesson/Course join
    private String lessonTitle;
    private String courseTitle;
}
