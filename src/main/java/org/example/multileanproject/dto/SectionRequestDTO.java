package org.example.multileanproject.dto;

import lombok.Data;
import java.util.List;

@Data
public class SectionRequestDTO {
    private Long id;
    private String title;
    private Integer orderIndex;

    // Chứa danh sách bài học dạng DTO
    private List<LessonRequestDTO> lessons;
}