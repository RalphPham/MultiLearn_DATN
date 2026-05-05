package org.example.multileanproject.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MyCourseResponse {
    private Long id;
    private String title;
    private String slug;
    private String thumbnail;
    private String instructorName;
    private BigDecimal progress;
    private Boolean isCourseCompleted;
    private String categoryName;
    private Double averageRating;
    private Integer totalLessons;
    private Integer totalDuration;
    private String level;
    private LocalDateTime rentalExpiresAt;
    private Long rentalDaysLeft;
    private Boolean hasPendingRefund;
    private Boolean hasRejectedRefund;
    private LocalDateTime enrolledAt;
    private BigDecimal originalPaidAmount;
    private BigDecimal finalPaidAmount;
    private BigDecimal discountAmount;
    private BigDecimal discountPercent;
    private String couponCode;
}
