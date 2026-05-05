package org.example.multileanproject.controller;

import lombok.RequiredArgsConstructor;
import org.example.multileanproject.service.RefundService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/refunds")
@RequiredArgsConstructor
public class AdminRefundController {

    private final RefundService refundService;

    @GetMapping
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN','REFUND_VIEW')")
    public ResponseEntity<?> getAllRefunds() {
        return ResponseEntity.ok(refundService.getAllRefundRequests());
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN','REFUND_PROCESS')")
    public ResponseEntity<?> approveRefund(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, String> payload
    ) {
        String adminNote = payload != null ? payload.get("adminNote") : null;
        refundService.approveRefund(id, adminNote);
        return ResponseEntity.ok(Map.of("message", "Da duyet yeu cau hoan tien."));
    }

    @PostMapping("/{id}/reject")
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN','REFUND_PROCESS')")
    public ResponseEntity<?> rejectRefund(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, String> payload
    ) {
        String adminNote = payload != null ? payload.get("adminNote") : null;
        refundService.rejectRefund(id, adminNote);
        return ResponseEntity.ok(Map.of("message", "Da tu choi yeu cau hoan tien."));
    }
}