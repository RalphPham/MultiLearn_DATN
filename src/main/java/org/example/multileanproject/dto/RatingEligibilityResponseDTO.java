package org.example.multileanproject.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class RatingEligibilityResponseDTO {

    private boolean loggedIn;
    private boolean enrolled;
    private boolean completed;
    private boolean alreadyRated;
    private boolean eligible;

    private Long courseId;
    private Long studentId;
    private Double progress;

    private String message;
}