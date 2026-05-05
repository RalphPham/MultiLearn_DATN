package org.example.multileanproject.dto;

import lombok.Data;
import java.util.List;

@Data
public class QuizCreateRequestDTO {
    private Long sectionId;
    private String title;
    private Integer passingScore;
    private Integer orderIndex;
    private List<QuestionCreateRequestDTO> questions;

    @Data
    public static class QuestionCreateRequestDTO {
        private String content;
        private Double score;
        private List<AnswerCreateRequestDTO> answers;
    }

    @Data
    public static class AnswerCreateRequestDTO {
        private String content;
        private Boolean isCorrect;
    }
}