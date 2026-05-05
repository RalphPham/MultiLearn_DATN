package org.example.multileanproject.dto;

import lombok.Data;

@Data
public class NoteCreateRequest {
    private Long lessonId;
    private String content;
    private Integer timestampSeconds;
}
