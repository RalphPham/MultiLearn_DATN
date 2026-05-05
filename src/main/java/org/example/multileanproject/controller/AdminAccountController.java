package org.example.multileanproject.controller;

import lombok.RequiredArgsConstructor;
import org.example.multileanproject.dto.AdminAccountDTO;
import org.example.multileanproject.service.AdminAccountService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/accounts")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('SUPER_ADMIN')")
public class AdminAccountController {

    private final AdminAccountService adminAccountService;

    /** Danh sách tất cả admin — chỉ SUPER_ADMIN mới xem được */
    @GetMapping
    public ResponseEntity<List<AdminAccountDTO>> getAll() {
        return ResponseEntity.ok(adminAccountService.getAll());
    }

    /** Tạo admin mới */
    @PostMapping
    public ResponseEntity<AdminAccountDTO> create(
            @RequestBody AdminAccountDTO dto,
            Authentication auth) {
        return ResponseEntity.ok(adminAccountService.create(dto, auth.getName()));
    }

    /** Cập nhật fullName, role, isActive, password */
    @PutMapping("/{id}")
    public ResponseEntity<AdminAccountDTO> update(
            @PathVariable Long id,
            @RequestBody AdminAccountDTO dto,
            Authentication auth) {
        return ResponseEntity.ok(adminAccountService.update(id, dto, auth.getName()));
    }

    /** Xóa admin */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            Authentication auth) {
        adminAccountService.delete(id, auth.getName());
        return ResponseEntity.ok().build();
    }
}
