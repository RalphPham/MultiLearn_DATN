package org.example.multileanproject.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuizDetailResponseDTO {
    private Long id;
    private String title;
    private Integer passingScore;
    private Integer orderIndex;
    private Long sectionId;

    @Builder.Default
    private List<QuestionDTO> questions = new ArrayList<>();

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class QuestionDTO {
        private Long id;
        private String content;
        private Double score;

        @Builder.Default
        private List<AnswerDTO> answers = new ArrayList<>();
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AnswerDTO {
        private Long id;
        private String content;
        private Boolean isCorrect;
    }
}