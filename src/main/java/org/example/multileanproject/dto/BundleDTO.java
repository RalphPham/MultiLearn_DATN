package org.example.multileanproject.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class BundleDTO {
    private Long id;
    private String title;
    private String description;
    private String thumbnail;
    private BigDecimal price;
    private String status;
    private List<Long> selectedCourseIds; // Danh sách ID các khóa học được chọn
}