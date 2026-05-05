package org.example.multileanproject.dto;

import lombok.Data;

@Data
public class LessonRequestDTO {
    private Long id;
    private String title;
    private String type;
    private Integer duration;
    private String videoUrl;
    private String contentText;
    private String documentUrl;
    private Boolean isPreview;
    private Integer orderIndex;
    private Long quizId;
}