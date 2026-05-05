package org.example.multileanproject.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "coupons")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Coupon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Column(name = "discount_type", length = 20)
    private String discountType;

    @Column(name = "discount_value")
    private BigDecimal discountValue; // Tên đúng là discountValue

    @Column(name = "max_discount_amount")
    private BigDecimal maxDiscountAmount;

    @Column(name = "usage_limit")
    private Integer usageLimit;

    // 🔥 THÊM @Builder.Default VÀO ĐÂY
    @Builder.Default
    @Column(name = "used_count")
    private Integer usedCount = 0;

    // 🔥 THÊM @Builder.Default VÀO ĐÂY
    @Builder.Default
    @Column(name = "is_active")
    private boolean isActive = true;

    @Column(name = "start_date")
    private LocalDateTime startDate;

    @Column(name = "end_date")
    private LocalDateTime endDate;

    @Column(name = "min_order_value")
    private BigDecimal minOrderValue;

    /** NULL = coupon toàn hệ thống (admin). Non-null = coupon riêng của giảng viên */
    @Column(name = "instructor_id")
    private Long instructorId;

    public int getRemainingCount() {
        if (usageLimit == null) return 9999;
        return Math.max(0, usageLimit - (usedCount != null ? usedCount : 0));
    }
}
