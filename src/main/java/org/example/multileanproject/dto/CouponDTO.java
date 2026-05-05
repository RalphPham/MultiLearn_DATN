package org.example.multileanproject.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CouponDTO {
    private Long id;
    private String code;
    private String discountType;      // PERCENT, FIXED
    private BigDecimal maxDiscountAmount;
    private BigDecimal discountValue; // Giá trị
    private Integer usageLimit;       // Tổng số lượng
    private Integer usedCount;        // Đã dùng
    private Integer remainingCount;   // Còn lại
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private BigDecimal minOrderValue; // Đơn tối thiểu
    private boolean isActive;
    private Long instructorId;        // NULL = admin coupon, non-null = instructor coupon
}
