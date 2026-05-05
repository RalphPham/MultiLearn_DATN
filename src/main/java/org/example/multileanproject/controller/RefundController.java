package org.example.multileanproject.controller;

import lombok.RequiredArgsConstructor;
import org.example.multileanproject.dto.RefundRequestDTO;
import org.example.multileanproject.service.RefundService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/student/refunds")
@RequiredArgsConstructor
public class RefundController {

    private final RefundService refundService;

    @PostMapping("/request")
    public ResponseEntity<?> requestRefund(
            @RequestBody RefundRequestDTO requestDTO,
            Authentication authentication
    ) {
        if (authentication == null || authentication.getName() == null || "anonymousUser".equals(authentication.getName())) {
            return ResponseEntity.status(401).body(Map.of("message", "Ban chua dang nhap."));
        }

        refundService.createRefundRequest(authentication.getName(), requestDTO);
        return ResponseEntity.ok(Map.of("message", "Da gui yeu cau hoan tien."));
    }
}
