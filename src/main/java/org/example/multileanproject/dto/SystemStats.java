package org.example.multileanproject.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SystemStats {
    private long totalStudents;
    private long totalCourses;
    private long totalInstructors;
    private double averageRating;
}