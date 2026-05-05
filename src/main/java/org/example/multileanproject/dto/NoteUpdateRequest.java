package org.example.multileanproject.dto;

import lombok.Data;

@Data
public class NoteUpdateRequest {
    private String content;
    private Integer timestampSeconds;
}
