package org.example.multileanproject.service;

import org.example.multileanproject.dto.CouponDTO;
import org.example.multileanproject.entity.Coupon;
import org.example.multileanproject.repository.CouponRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CouponAdminService {
    private final CouponRepository couponRepository;

    public CouponAdminService(CouponRepository couponRepository) {
        this.couponRepository = couponRepository;
    }

    public List<CouponDTO> getAllCoupons() {
        return couponRepository.findAll().stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    @Transactional
    public void createCoupon(CouponDTO dto) {
        if (couponRepository.existsByCode(dto.getCode())) {
            throw new RuntimeException("Mã giảm giá này đã tồn tại!");
        }
        validateCoupon(dto);
        Coupon c = new Coupon();
        mapToEntity(c, dto);
        c.setUsedCount(0);
        couponRepository.save(c);
    }

    @Transactional
    public void updateCoupon(Long id, CouponDTO dto) {
        Coupon c = couponRepository.findById(id).orElseThrow(() -> new RuntimeException("Không tìm thấy coupon"));
        validateCoupon(dto);
        mapToEntity(c, dto);
        couponRepository.save(c);
    }

    private void validateCoupon(CouponDTO dto) {
        if (dto.getDiscountValue() == null || dto.getDiscountValue().signum() <= 0) {
            throw new RuntimeException("Giá trị giảm giá phải lớn hơn 0!");
        }
        if ("PERCENT".equalsIgnoreCase(dto.getDiscountType())
                && dto.getDiscountValue().doubleValue() > 100) {
            throw new RuntimeException("Phần trăm giảm giá không được vượt quá 100%!");
        }
        if (dto.getStartDate() != null && dto.getEndDate() != null
                && !dto.getEndDate().isAfter(dto.getStartDate())) {
            throw new RuntimeException("Ngày kết thúc phải sau ngày bắt đầu!");
        }
    }

    @Transactional
    public void deleteCoupon(Long id) {
        couponRepository.deleteById(id);
    }

    // Helper: Map Entity <-> DTO
    private void mapToEntity(Coupon c, CouponDTO dto) {
        if(c.getId() == null) c.setCode(dto.getCode().toUpperCase());
        c.setDiscountType(dto.getDiscountType());
        c.setDiscountValue(dto.getDiscountValue());
        c.setMaxDiscountAmount(dto.getMaxDiscountAmount());
        c.setUsageLimit(dto.getUsageLimit());
        c.setStartDate(dto.getStartDate());
        c.setEndDate(dto.getEndDate());
        c.setMinOrderValue(dto.getMinOrderValue());
        c.setActive(dto.isActive());
    }

    private CouponDTO convertToDTO(Coupon c) {
        CouponDTO dto = new CouponDTO();
        dto.setId(c.getId());
        dto.setCode(c.getCode());
        dto.setDiscountType(c.getDiscountType());
        dto.setDiscountValue(c.getDiscountValue());
        dto.setMaxDiscountAmount(c.getMaxDiscountAmount());
        dto.setUsageLimit(c.getUsageLimit());
        dto.setUsedCount(c.getUsedCount());
        dto.setRemainingCount(c.getRemainingCount());
        dto.setStartDate(c.getStartDate());
        dto.setEndDate(c.getEndDate());
        dto.setMinOrderValue(c.getMinOrderValue());
        dto.setActive(c.isActive());
        return dto;
    }
}
