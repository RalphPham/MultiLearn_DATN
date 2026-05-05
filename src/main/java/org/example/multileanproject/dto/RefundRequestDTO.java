package org.example.multileanproject.dto;

import lombok.Data;

@Data
public class RefundRequestDTO {
    private Long courseId;
    private String reason;
}
