package org.example.multileanproject.controller;

import org.example.multileanproject.dto.CouponDTO;
import org.example.multileanproject.service.CouponAdminService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/coupons")
public class CouponAdminController {
    private final CouponAdminService couponAdminService;

    public CouponAdminController(CouponAdminService couponAdminService) {
        this.couponAdminService = couponAdminService;
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN','VOUCHER_VIEW')")
    public ResponseEntity<List<CouponDTO>> getAll() {
        return ResponseEntity.ok(couponAdminService.getAllCoupons());
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN','VOUCHER_MANAGE')")
    public ResponseEntity<Void> create(@RequestBody CouponDTO dto) {
        couponAdminService.createCoupon(dto);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN','VOUCHER_MANAGE')")
    public ResponseEntity<Void> update(@PathVariable Long id, @RequestBody CouponDTO dto) {
        couponAdminService.updateCoupon(id, dto);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN','VOUCHER_MANAGE')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        couponAdminService.deleteCoupon(id);
        return ResponseEntity.ok().build();
    }
}