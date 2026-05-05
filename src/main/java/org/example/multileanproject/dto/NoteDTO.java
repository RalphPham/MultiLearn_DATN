package org.example.multileanproject.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NoteDTO {
    private Long id;
    private Long lessonId;
    private String content;
    private Integer timestampSeconds;
    private LocalDateTime createdAt;
}
