package org.example.multileanproject.controller;

import lombok.RequiredArgsConstructor;
import org.example.multileanproject.dto.CouponDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/public/coupons")
@RequiredArgsConstructor
public class PublicCouponController {

    /**
     * Platform-first policy:
     * instructor coupon flow is disabled. Keep this endpoint for compatibility.
     */
    @GetMapping("/course/{courseId}")
    public ResponseEntity<List<CouponDTO>> getCouponsByCourse(@PathVariable Long courseId) {
        return ResponseEntity.ok(Collections.emptyList());
    }
}
