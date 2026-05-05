package org.example.multileanproject.controller;

import lombok.RequiredArgsConstructor;
import org.example.multileanproject.entity.AdminActionLog;
import org.example.multileanproject.service.AdminActionLogService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/audit-logs")
@RequiredArgsConstructor
public class AdminActionLogController {

    private final AdminActionLogService adminActionLogService;

    @GetMapping
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN','AUDIT_VIEW')")
    public ResponseEntity<Page<AdminActionLog>> getLogs(
            @RequestParam(required = false) String adminUsername,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String targetType,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(
                adminActionLogService.getLogs(adminUsername, action, targetType, pageable)
        );
    }
}
