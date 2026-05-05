package org.example.multileanproject.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class QuizResulResponse {
    private double score;
    private int correctCount;
    private int totalquestions;
    private boolean isPassed;
    private String message;
}
