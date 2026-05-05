package org.example.multileanproject.controller;

import org.example.multileanproject.dto.UserCourseDTO;
import org.example.multileanproject.dto.UserDTO;
import org.example.multileanproject.service.UserAdminService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class UserAdminController {

    private final UserAdminService userAdminService;

    public UserAdminController(UserAdminService userAdminService) {
        this.userAdminService = userAdminService;
    }

    @GetMapping("/users")
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN','USER_VIEW')")
    public ResponseEntity<Page<UserDTO>> getUsers(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        return ResponseEntity.ok(userAdminService.getAllUsers(q, role, status, pageable));
    }

    @PutMapping("/users/{id}/status")
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN','USER_LOCK')")
    public ResponseEntity<Void> updateStatus(
            @PathVariable Long id,
            @RequestParam String status
    ) {
        userAdminService.updateUserStatus(id, status);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/users/{id}")
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN','USER_EDIT')")
    public ResponseEntity<UserDTO> updateUser(
            @PathVariable Long id,
            @RequestBody UserDTO dto
    ) {
        return ResponseEntity.ok(userAdminService.updateUser(id, dto));
    }

    @GetMapping("/users/{id}/courses")
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN','USER_VIEW')")
    public ResponseEntity<List<UserCourseDTO>> getUserCourses(
            @PathVariable Long id,
            @RequestParam(required = false) String mode
    ) {
        return ResponseEntity.ok(userAdminService.getUserCourses(id, mode));
    }

    @GetMapping("/instructors")
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN','INSTRUCTOR_PROFILE_VIEW')")
    public ResponseEntity<Page<Map<String, Object>>> getAllInstructors(
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(userAdminService.getAllInstructors(q, pageable));
    }

    @GetMapping("/instructors/{userId}/profile")
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN','INSTRUCTOR_PROFILE_VIEW')")
    public ResponseEntity<Map<String, Object>> getInstructorProfile(@PathVariable Long userId) {
        return ResponseEntity.ok(userAdminService.getInstructorProfile(userId));
    }
}
