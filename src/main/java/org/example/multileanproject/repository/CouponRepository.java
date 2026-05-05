package org.example.multileanproject.repository;

import org.example.multileanproject.entity.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface CouponRepository extends JpaRepository<Coupon, Long> {
    Optional<Coupon> findByCode(String code);
    boolean existsByCode(String code);
    Optional<Coupon> findByCodeAndIsActiveTrue(String code);
    List<Coupon> findByInstructorId(Long instructorId);
    List<Coupon> findByInstructorIdIsNull();
}
