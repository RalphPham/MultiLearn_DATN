package org.example.multileanproject.controller;

import lombok.RequiredArgsConstructor;
import org.example.multileanproject.dto.WithdrawActionDTO;
import org.example.multileanproject.dto.WithdrawResponseDTO;
import org.example.multileanproject.service.WithdrawService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/withdraw")
@RequiredArgsConstructor
public class WithdrawAdminController {

    private final WithdrawService withdrawService;

    @GetMapping
    public ResponseEntity<?> getAllRequests(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<WithdrawResponseDTO> result = withdrawService.getAllRequests(status, pageable);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> processRequest(@PathVariable Long id, @RequestBody WithdrawActionDTO action) {
        try {
            WithdrawResponseDTO result = withdrawService.processRequest(id, action);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/pending-count")
    public ResponseEntity<?> getPendingCount() {
        return ResponseEntity.ok(Map.of("count", withdrawService.countPendingRequests()));
    }
}

