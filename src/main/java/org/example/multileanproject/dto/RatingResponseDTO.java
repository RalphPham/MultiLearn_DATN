package org.example.multileanproject.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class RatingResponseDTO {

    private Long id;

    private Integer stars;
    private String comment;

    private Long studentId;
    private String studentName;
    private String studentAvatar;

    private Long courseId;
    private String courseTitle;
    private String courseSlug;
    private String courseThumbnail;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}