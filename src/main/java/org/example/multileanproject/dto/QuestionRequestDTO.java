package org.example.multileanproject.dto;
import lombok.Data;

@Data
public class QuestionRequestDTO {
    private Long lessonId;
    private Long courseId;
    private String content;
}