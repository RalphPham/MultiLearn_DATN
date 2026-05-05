package org.example.multileanproject.dto;

import lombok.Data;

@Data
public class LessonDTO {
    private Long id;
    private String title;
    private String type;
    private Integer orderIndex;
    private Long quizId;
    private String videoUrl;
    private Boolean isCompleted;
    private Integer quizPassingScore;
}
