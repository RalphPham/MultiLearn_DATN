package org.example.multileanproject.service;

import java.math.BigDecimal;
import java.util.List;

public interface CouponService {

    /**
     * @param instructorUserIds danh sách instructor.user.id của các khóa trong đơn hàng.
     *                          Dùng để kiểm tra phạm vi coupon của instructor.
     */
    BigDecimal calculateDiscount(String code, BigDecimal totalOrderValue, List<Long> instructorUserIds);

    void incrementUsageCount(String code);
}