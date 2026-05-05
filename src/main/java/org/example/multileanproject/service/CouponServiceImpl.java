package org.example.multileanproject.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.multileanproject.entity.Coupon;
import org.example.multileanproject.repository.CouponRepository;
import org.example.multileanproject.service.CouponService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CouponServiceImpl implements CouponService {

    private final CouponRepository couponRepository;

    @Override
    public BigDecimal calculateDiscount(String code, BigDecimal totalOrderValue, List<Long> instructorUserIds) {
        if (code == null || code.isBlank()) {
            return BigDecimal.ZERO;
        }

        Coupon coupon = couponRepository.findByCode(code.trim().toUpperCase())
                .orElseThrow(() -> new RuntimeException("MÃƒÂ£ giÃ¡ÂºÂ£m giÃƒÂ¡ khÃƒÂ´ng tÃ¡Â»â€œn tÃ¡ÂºÂ¡i!"));

        // Platform-first: chi chap nhan coupon do nen tang phat hanh.
        // Coupon instructor legacy duoc giu de doi soat lich su, nhung khong cho ap dung don moi.
        if (coupon.getInstructorId() != null) {
            throw new RuntimeException("Ma coupon giang vien da ngung ap dung. Vui long dung ma do nen tang phat hanh.");
        }

        if (!coupon.isActive()) {
            throw new RuntimeException("MÃ£ nÃ y Ä‘ang bá»‹ khÃ³a hoáº·c ngÆ°ng hoáº¡t Ä‘á»™ng!");
        }

        LocalDateTime now = LocalDateTime.now();
        if (coupon.getStartDate() != null && now.isBefore(coupon.getStartDate())) {
            throw new RuntimeException("MÃƒÂ£ nÃƒÂ y chÃ†Â°a Ã„â€˜Ã¡ÂºÂ¿n thÃ¡Â»Âi gian hiÃ¡Â»â€¡u lÃ¡Â»Â±c!");
        }

        if (coupon.getEndDate() != null && now.isAfter(coupon.getEndDate())) {
            throw new RuntimeException("MÃƒÂ£ nÃƒÂ y Ã„â€˜ÃƒÂ£ hÃ¡ÂºÂ¿t hÃ¡ÂºÂ¡n!");
        }

        if (coupon.getUsageLimit() != null && coupon.getUsageLimit() > 0) {
            int used = coupon.getUsedCount() == null ? 0 : coupon.getUsedCount();
            if (used >= coupon.getUsageLimit()) {
                throw new RuntimeException("MÃƒÂ£ nÃƒÂ y Ã„â€˜ÃƒÂ£ hÃ¡ÂºÂ¿t lÃ†Â°Ã¡Â»Â£t sÃ¡Â»Â­ dÃ¡Â»Â¥ng!");
            }
        }

        if (coupon.getMinOrderValue() != null
                && totalOrderValue.compareTo(coupon.getMinOrderValue()) < 0) {
            throw new RuntimeException("Ã„ÂÃ†Â¡n hÃƒÂ ng phÃ¡ÂºÂ£i tÃ¡Â»Â« "
                    + coupon.getMinOrderValue() + " Ã„â€˜ mÃ¡Â»â€ºi Ã„â€˜Ã†Â°Ã¡Â»Â£c dÃƒÂ¹ng mÃƒÂ£ nÃƒÂ y!");
        }

        BigDecimal discountAmount = BigDecimal.ZERO;
        String discountType = coupon.getDiscountType() != null
                ? coupon.getDiscountType().trim().toUpperCase()
                : "";

        if ("PERCENT".equals(discountType)) {
            BigDecimal percent = coupon.getDiscountValue()
                    .divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
            discountAmount = totalOrderValue.multiply(percent);
        } else if ("FIXED".equals(discountType) || "AMOUNT".equals(discountType)) {
            discountAmount = coupon.getDiscountValue() != null
                    ? coupon.getDiscountValue()
                    : BigDecimal.ZERO;
        }

        if (discountAmount.compareTo(totalOrderValue) > 0) {
            discountAmount = totalOrderValue;
        }

        if (coupon.getMaxDiscountAmount() != null
                && coupon.getMaxDiscountAmount().compareTo(BigDecimal.ZERO) > 0
                && discountAmount.compareTo(coupon.getMaxDiscountAmount()) > 0) {
            discountAmount = coupon.getMaxDiscountAmount();
        }

        return discountAmount.setScale(0, RoundingMode.HALF_UP);
    }

    @Override
    @Transactional
    public void incrementUsageCount(String code) {
        if (code == null || code.isBlank()) {
            return;
        }

        couponRepository.findByCode(code.trim().toUpperCase()).ifPresent(coupon -> {
            int currentCount = coupon.getUsedCount() == null ? 0 : coupon.getUsedCount();
            coupon.setUsedCount(currentCount + 1);
            couponRepository.save(coupon);
        });
    }
}
