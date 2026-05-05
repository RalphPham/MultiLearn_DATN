package org.example.multileanproject.service;

import lombok.RequiredArgsConstructor;
import org.example.multileanproject.dto.CouponDTO;
import org.example.multileanproject.entity.Coupon;
import org.example.multileanproject.entity.Instructor;
import org.example.multileanproject.repository.CouponRepository;
import org.example.multileanproject.repository.InstructorRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InstructorCouponService {

    private final CouponRepository couponRepository;
    private final InstructorRepository instructorRepository;

    // Ã¢â€â‚¬Ã¢â€â‚¬ LÃ¡ÂºÂ¥y instructor hiÃ¡Â»â€¡n tÃ¡ÂºÂ¡i Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬
    private Instructor getCurrentInstructor() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return instructorRepository.findByUser_Email(email)
                .orElseThrow(() -> new RuntimeException("KhÃƒÂ´ng tÃƒÂ¬m thÃ¡ÂºÂ¥y thÃƒÂ´ng tin giÃ¡ÂºÂ£ng viÃƒÂªn."));
    }

    // Ã¢â€â‚¬Ã¢â€â‚¬ Danh sÃƒÂ¡ch coupon cÃ¡Â»Â§a instructor Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬
    public List<CouponDTO> getMyCoupons() {
        Long instructorUserId = getCurrentInstructor().getUser().getId();
        return couponRepository.findByInstructorId(instructorUserId)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    // Ã¢â€â‚¬Ã¢â€â‚¬ TÃ¡ÂºÂ¡o coupon mÃ¡Â»â€ºi Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬
    @Transactional
    public CouponDTO createCoupon(CouponDTO dto) {
        Instructor instructor = getCurrentInstructor();
        Long instructorUserId = instructor.getUser().getId();

        String code = dto.getCode() == null ? "" : dto.getCode().trim().toUpperCase();
        if (code.isEmpty()) {
            throw new RuntimeException("MÃƒÂ£ coupon khÃƒÂ´ng Ã„â€˜Ã†Â°Ã¡Â»Â£c Ã„â€˜Ã¡Â»Æ’ trÃ¡Â»â€˜ng.");
        }
        if (couponRepository.existsByCode(code)) {
            throw new RuntimeException("MÃƒÂ£ coupon \"" + code + "\" Ã„â€˜ÃƒÂ£ tÃ¡Â»â€œn tÃ¡ÂºÂ¡i.");
        }
        validate(dto);

        Coupon c = new Coupon();
        c.setCode(code);
        mapFields(c, dto);
        c.setInstructorId(instructorUserId);
        c.setUsedCount(0);
        c.setActive(true);

        return toDTO(couponRepository.save(c));
    }

    // Ã¢â€â‚¬Ã¢â€â‚¬ CÃ¡ÂºÂ­p nhÃ¡ÂºÂ­t coupon Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬
    @Transactional
    public CouponDTO updateCoupon(Long couponId, CouponDTO dto) {
        Coupon c = getOwnedCoupon(couponId);
        validate(dto);
        mapFields(c, dto);
        return toDTO(couponRepository.save(c));
    }

    // Ã¢â€â‚¬Ã¢â€â‚¬ BÃ¡ÂºÂ­t / tÃ¡ÂºÂ¯t coupon Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬
    @Transactional
    public void toggleActive(Long couponId) {
        Coupon c = getOwnedCoupon(couponId);
        c.setActive(!c.isActive());
        couponRepository.save(c);
    }

    // Ã¢â€â‚¬Ã¢â€â‚¬ XÃƒÂ³a coupon Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬
    @Transactional
    public void deleteCoupon(Long couponId) {
        Coupon c = getOwnedCoupon(couponId);
        couponRepository.delete(c);
    }

    // Ã¢â€â‚¬Ã¢â€â‚¬ Private helpers Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬

    private Coupon getOwnedCoupon(Long couponId) {
        Instructor instructor = getCurrentInstructor();
        Coupon c = couponRepository.findById(couponId)
                .orElseThrow(() -> new RuntimeException("KhÃƒÂ´ng tÃƒÂ¬m thÃ¡ÂºÂ¥y coupon."));
        if (!instructor.getUser().getId().equals(c.getInstructorId())) {
            throw new RuntimeException("BÃ¡ÂºÂ¡n khÃƒÂ´ng cÃƒÂ³ quyÃ¡Â»Ân thao tÃƒÂ¡c vÃ¡Â»â€ºi coupon nÃƒÂ y.");
        }
        return c;
    }

    private void validate(CouponDTO dto) {
        if (dto.getDiscountValue() == null || dto.getDiscountValue().signum() <= 0) {
            throw new RuntimeException("GiÃƒÂ¡ trÃ¡Â»â€¹ giÃ¡ÂºÂ£m giÃƒÂ¡ phÃ¡ÂºÂ£i lÃ¡Â»â€ºn hÃ†Â¡n 0.");
        }
        if ("PERCENT".equalsIgnoreCase(dto.getDiscountType())
                && dto.getDiscountValue().doubleValue() >= 100) {
            throw new RuntimeException("PhÃ¡ÂºÂ§n trÃ„Æ’m giÃ¡ÂºÂ£m giÃƒÂ¡ phÃ¡ÂºÂ£i nhÃ¡Â»Â hÃ†Â¡n 100%.");
        }
        if (dto.getStartDate() != null && dto.getEndDate() != null
                && !dto.getEndDate().isAfter(dto.getStartDate())) {
            throw new RuntimeException("NgÃƒÂ y kÃ¡ÂºÂ¿t thÃƒÂºc phÃ¡ÂºÂ£i sau ngÃƒÂ y bÃ¡ÂºÂ¯t Ã„â€˜Ã¡ÂºÂ§u.");
        }
    }

    private void mapFields(Coupon c, CouponDTO dto) {
        c.setDiscountType(dto.getDiscountType());
        c.setDiscountValue(dto.getDiscountValue());
        c.setMaxDiscountAmount(dto.getMaxDiscountAmount());
        c.setUsageLimit(dto.getUsageLimit());
        c.setStartDate(dto.getStartDate());
        c.setEndDate(dto.getEndDate());
        c.setMinOrderValue(dto.getMinOrderValue());
    }

    // Public: lay coupon dang hoat dong theo khoa hoc (Course Detail)
    public List<CouponDTO> getActiveCouponsByCourseId(Long courseId) {
        // Platform-first: endpoint public giu nguyen de khong vo frontend,
        // nhung chi tra coupon cua nen tang (instructor_id = null).
        LocalDateTime now = LocalDateTime.now();
        return couponRepository.findByInstructorIdIsNull()
                .stream()
                .filter(c -> c.isActive()
                        && (c.getStartDate() == null || !c.getStartDate().isAfter(now))
                        && (c.getEndDate() == null || c.getEndDate().isAfter(now))
                        && (c.getUsageLimit() == null || c.getUsedCount() == null
                        || c.getUsedCount() < c.getUsageLimit()))
                .map(this::toPublicDTO)
                .collect(Collectors.toList());
    }

    private CouponDTO toPublicDTO(Coupon c) {
        CouponDTO dto = new CouponDTO();
        dto.setId(c.getId());
        dto.setCode(c.getCode());
        dto.setDiscountType(c.getDiscountType());
        dto.setDiscountValue(c.getDiscountValue());
        dto.setMaxDiscountAmount(c.getMaxDiscountAmount());
        dto.setEndDate(c.getEndDate());
        dto.setMinOrderValue(c.getMinOrderValue());
        dto.setUsageLimit(c.getUsageLimit());
        dto.setUsedCount(c.getUsedCount());
        dto.setRemainingCount(c.getRemainingCount());
        return dto;
    }

    private CouponDTO toDTO(Coupon c) {
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
        dto.setInstructorId(c.getInstructorId());
        return dto;
    }
}
