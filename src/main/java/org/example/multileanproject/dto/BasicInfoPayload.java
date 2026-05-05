package org.example.multileanproject.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Dùng để serialize/deserialize JSON trong payload_before và payload_after
 * của bảng course_change_requests.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BasicInfoPayload {

    private String title;
    private Long categoryId;
    private String categoryName;
    private String shortDescription;
    private String description;
    private String learningOutcomes;
    private String language;
    private String level;
    private String thumbnail;
}
