package org.example.multileanproject.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CartItemResponse {
    private Long id;           // ID của mục trong giỏ
    private Long courseId;     // ID khóa học
    private String courseTitle;
    private String courseSlug; // Để bấm vào ra trang chi tiết
    private String courseThumbnail;
    private BigDecimal coursePrice;
    private String instructorName;
    private Integer totalDuration;
    private Integer totalLessons;
}