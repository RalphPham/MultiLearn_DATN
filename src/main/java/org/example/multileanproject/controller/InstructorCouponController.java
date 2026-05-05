package org.example.multileanproject.controller;

import lombok.RequiredArgsConstructor;
import org.example.multileanproject.dto.CouponDTO;
import org.example.multileanproject.service.AdminActionLogService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/instructor/coupons")
@RequiredArgsConstructor
public class InstructorCouponController {

    private static final String PLATFORM_FIRST_MESSAGE =
            "Coupon do nen tang quan ly tap trung. Vui long lien he Admin/SuperAdmin.";

    private final AdminActionLogService adminActionLogService;

    @GetMapping
    public ResponseEntity<List<CouponDTO>> getMyCoupons() {
        denyCouponOperation("LIST_COUPONS");
        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    @PostMapping
    public ResponseEntity<CouponDTO> create(@RequestBody CouponDTO dto) {
        denyCouponOperation("CREATE_COUPON");
        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<CouponDTO> update(@PathVariable Long id, @RequestBody CouponDTO dto) {
        denyCouponOperation("UPDATE_COUPON#" + id);
        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    @PatchMapping("/{id}/toggle")
    public ResponseEntity<Void> toggle(@PathVariable Long id) {
        denyCouponOperation("TOGGLE_COUPON#" + id);
        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        denyCouponOperation("DELETE_COUPON#" + id);
        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    private void denyCouponOperation(String operation) {
        try {
            adminActionLogService.log(
                    "INSTRUCTOR_COUPON_DENIED",
                    "Chan thao tac coupon cua giang vien: " + operation,
                    null,
                    "COUPON"
            );
        } catch (Exception ignored) {
        }
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, PLATFORM_FIRST_MESSAGE);
    }
}
