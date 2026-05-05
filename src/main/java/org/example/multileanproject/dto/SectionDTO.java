package org.example.multileanproject.dto;

import lombok.Data;

import java.util.List;

@Data
public class SectionDTO {
    private Long id;
    private String title;
    private Integer orderIndex;
    private List<LessonDTO> lessons;
}
