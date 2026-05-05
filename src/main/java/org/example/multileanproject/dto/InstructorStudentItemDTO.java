package org.example.multileanproject.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InstructorStudentItemDTO {
    private Long enrollmentId;

    private Long studentId;
    private String studentName;
    private String studentEmail;
    private String studentAvatar;

    private Long courseId;
    private String courseTitle;
    private String courseSlug;
    private String courseThumbnail;

    private String enrollmentStatus;
    private Double progress;
    private java.time.LocalDateTime enrolledAt;
}