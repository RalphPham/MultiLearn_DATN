package org.example.multileanproject.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class TopInstructorDTO {
    private Long id;
    private String fullName;
    private String avatar;
    private Long totalCourses;
    private BigDecimal totalRevenue;
}
