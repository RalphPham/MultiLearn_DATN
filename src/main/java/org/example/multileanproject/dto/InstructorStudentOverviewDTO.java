package org.example.multileanproject.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class InstructorStudentOverviewDTO {
    private String instructorEmail;
    private Long totalCourses;
    private Long totalUniqueStudents;
    private Long totalActiveEnrollments;
    private List<InstructorCourseEnrollmentDTO> courses;
}