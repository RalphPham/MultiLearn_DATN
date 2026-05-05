package org.example.multileanproject.service;

import org.example.multileanproject.dto.CourseContentRequest;

public interface CourseBuilderService {
    void updateFullContent(Long courseId, CourseContentRequest request);
}

