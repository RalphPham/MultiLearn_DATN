package org.example.multileanproject.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseChangeRequestDTO {

    private Long id;
    private Long courseId;
    private String courseTitle;
    private String courseSlug;

    private Long instructorId;
    private String instructorName;

    private String requestType;
    private String status;

    private BasicInfoPayload payloadBefore;
    private BasicInfoPayload payloadAfter;

    private String requestNote;
    private String adminNote;

    private Long reviewedById;
    private String reviewedByName;
    private LocalDateTime reviewedAt;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
