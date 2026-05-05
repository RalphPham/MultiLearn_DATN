package org.example.multileanproject.dto;

import lombok.Data;

@Data
public class BroadcastRequest {
    private Long courseId;
    private String title;
    private String message;
}
