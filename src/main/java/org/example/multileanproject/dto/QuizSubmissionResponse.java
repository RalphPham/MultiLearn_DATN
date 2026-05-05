package org.example.multileanproject.dto;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Builder
public class QuizSubmissionResponse {
    private BigDecimal score;
    private Boolean isPassed;
    private Integer correctCount;
    private Integer totalQuestions;
    private String message;
}
