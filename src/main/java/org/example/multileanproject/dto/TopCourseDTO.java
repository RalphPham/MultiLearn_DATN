package org.example.multileanproject.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class TopCourseDTO {
    private Long id;
    private String title;
    private String thumbnail;
    private Long totalEnrollments;
    private BigDecimal totalRevenue;
}
