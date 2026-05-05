package org.example.multileanproject.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InstructorCourseEnrollmentDTO {
    private Long courseId;
    private String courseTitle;
    private String courseSlug;
    private Long activeEnrollments;
    private Long uniqueStudents;
}