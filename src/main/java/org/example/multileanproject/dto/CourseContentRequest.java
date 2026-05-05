package org.example.multileanproject.dto;

import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
public class CourseContentRequest {

    private List<SectionRequest> sections;
}
