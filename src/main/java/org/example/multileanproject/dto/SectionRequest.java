package org.example.multileanproject.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class SectionRequest {

    private String title;
    private String description;
    private Integer orderIndex;

    private List<LessonRequest> lessons;
}

