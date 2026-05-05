package org.example.multileanproject.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.multileanproject.dto.WithdrawRequestDTO;
import org.example.multileanproject.dto.WithdrawResponseDTO;
import org.example.multileanproject.service.WithdrawService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/instructor/withdraw")
@RequiredArgsConstructor
public class WithdrawController {

    private final WithdrawService withdrawService;

    @GetMapping("/wallet-balance")
    public ResponseEntity<?> getWalletBalance(Authentication authentication) {
        try {
            BigDecimal balance = withdrawService.getWalletBalance(authentication.getName());
            return ResponseEntity.ok(Map.of("balance", balance));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping
    public ResponseEntity<?> createWithdrawRequest(
            @Valid @RequestBody WithdrawRequestDTO request,
            Authentication authentication
    ) {
        try {
            WithdrawResponseDTO response = withdrawService.createRequest(authentication.getName(), request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/history")
    public ResponseEntity<?> getWithdrawHistory(Authentication authentication) {
        try {
            List<WithdrawResponseDTO> history = withdrawService.getMyHistory(authentication.getName());
            return ResponseEntity.ok(history);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}

