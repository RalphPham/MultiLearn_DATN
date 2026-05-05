package org.example.multileanproject.controller;

import lombok.RequiredArgsConstructor;
import org.example.multileanproject.dto.BundleDTO;
import org.example.multileanproject.entity.Bundle;
import org.example.multileanproject.service.BundleService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bundles")
@RequiredArgsConstructor
public class BundleController {

    private final BundleService bundleService;

    // 1. Tạo hoặc Cập nhật Gói Combo
    @PostMapping
    public ResponseEntity<Bundle> createOrUpdateBundle(@RequestBody BundleDTO bundleDTO) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(bundleService.createBundle(bundleDTO, email));
    }

    // 2. Lấy chi tiết 1 Gói Combo (để edit)
    @GetMapping("/{id}")
    public ResponseEntity<BundleDTO> getBundleDetail(@PathVariable Long id) {
        return ResponseEntity.ok(bundleService.getBundleDetail(id));
    }

    // 3. Lấy danh sách Gói Combo của tôi
    @GetMapping("/my-bundles")
    public ResponseEntity<List<Bundle>> getMyBundles() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(bundleService.getMyBundles(email));
    }

    // 3. Xóa Gói Combo
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBundle(@PathVariable Long id) {
        bundleService.deleteBundle(id);
        return ResponseEntity.noContent().build();
    }
}