package org.example.multileanproject.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VerifyCourseRequest {
    @NotBlank
    private String status;
    private String message;
}
