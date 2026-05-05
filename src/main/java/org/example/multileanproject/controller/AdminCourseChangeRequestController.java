package org.example.multileanproject.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.multileanproject.dto.CourseChangeRequestDTO;
import org.example.multileanproject.dto.ReviewCourseChangeRequestDTO;
import org.example.multileanproject.service.CourseChangeRequestService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/course-change-requests")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ADMIN')")
public class AdminCourseChangeRequestController {

    private final CourseChangeRequestService changeRequestService;

    /**
     * GET /api/admin/course-change-requests?status=PENDING&type=BASIC_INFO&page=0&size=10
     */
    @GetMapping
    public ResponseEntity<Page<CourseChangeRequestDTO>> listRequests(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(changeRequestService.listPendingRequests(page, size));
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<CourseChangeRequestDTO> approve(
            @PathVariable Long id,
            @RequestBody(required = false) ReviewCourseChangeRequestDTO dto,
            Authentication auth) {
        return ResponseEntity.ok(changeRequestService.approveRequest(id, dto, auth.getName()));
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<CourseChangeRequestDTO> reject(
            @PathVariable Long id,
            @Valid @RequestBody ReviewCourseChangeRequestDTO dto,
            Authentication auth) {
        return ResponseEntity.ok(changeRequestService.rejectRequest(id, dto, auth.getName()));
    }
}
