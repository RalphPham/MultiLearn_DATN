package org.example.multileanproject.controller;

import lombok.RequiredArgsConstructor;
import org.example.multileanproject.dto.MarketInsightRequest;
import org.example.multileanproject.service.MarketInsightService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/instructor")
@RequiredArgsConstructor
public class MarketInsightController {

    private final MarketInsightService marketInsightService;

    @PostMapping("/market-insight")
    public ResponseEntity<?> analyze(
            @RequestBody MarketInsightRequest request,
            Authentication authentication) {
        if (authentication == null || "anonymousUser".equals(authentication.getName())) {
            return ResponseEntity.status(401).body("Bạn chưa đăng nhập.");
        }
        try {
            return ResponseEntity.ok(marketInsightService.analyze(request));
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Lỗi phân tích: " + e.getMessage());
        }
    }
}
