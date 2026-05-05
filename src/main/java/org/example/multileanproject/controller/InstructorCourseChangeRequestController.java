package org.example.multileanproject.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.multileanproject.dto.CourseChangeRequestDTO;
import org.example.multileanproject.dto.CreateBasicInfoChangeRequestDTO;
import org.example.multileanproject.service.CourseChangeRequestService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/instructor/courses/{courseId}/basic-info-requests")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('INSTRUCTOR')")
public class InstructorCourseChangeRequestController {

    private final CourseChangeRequestService changeRequestService;

    @PostMapping
    public ResponseEntity<CourseChangeRequestDTO> createRequest(
            @PathVariable Long courseId,
            @Valid @RequestBody CreateBasicInfoChangeRequestDTO dto,
            Authentication auth) {
        CourseChangeRequestDTO result = changeRequestService
                .createBasicInfoRequest(courseId, dto, auth.getName());
        return ResponseEntity.status(201).body(result);
    }

    @GetMapping("/latest")
    public ResponseEntity<CourseChangeRequestDTO> getLatest(
            @PathVariable Long courseId,
            Authentication auth) {
        return changeRequestService.getLatestRequest(courseId, auth.getName())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }

    @DeleteMapping("/{requestId}")
    public ResponseEntity<Void> cancelRequest(
            @PathVariable Long courseId,
            @PathVariable Long requestId,
            Authentication auth) {
        changeRequestService.cancelRequest(requestId, auth.getName());
        return ResponseEntity.ok().build();
    }
}
