package org.example.multileanproject.controller;

import org.example.multileanproject.dto.CourseApproveRequest;
import org.example.multileanproject.dto.CourseBulkApprovalRequest;
import org.example.multileanproject.dto.CourseDTO;
import org.example.multileanproject.service.CourseAdminService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/courses")
public class CourseAdminController {

    private final CourseAdminService courseAdminService;

    public CourseAdminController(CourseAdminService courseAdminService) {
        this.courseAdminService = courseAdminService;
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN','COURSE_VIEW')")
    public ResponseEntity<Page<CourseDTO>> getCourses(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("updatedAt").descending());
        return ResponseEntity.ok(courseAdminService.getAllCourses(q, categoryId, status, pageable));
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN','COURSE_APPROVE')")
    public ResponseEntity<Void> approveCourse(
            @PathVariable Long id,
            @RequestBody CourseApproveRequest request
    ) {
        courseAdminService.approveCourseWithPrice(id, request);
        return ResponseEntity.ok().build();
    }

    // 🔥 API MỚI: Từ chối khóa học (Gửi kèm lý do vào body)
    @PostMapping("/{id}/reject")
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN','COURSE_REJECT')")
    public ResponseEntity<?> rejectCourse(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        try {
            String reason = "";
            if (body.containsKey("reason") && body.get("reason") != null) {
                reason = body.get("reason").toString();
            }

            courseAdminService.updateCourseStatus(id, "REJECTED", reason);

            return ResponseEntity.ok().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Lỗi hệ thống: " + e.getMessage());
        }
    }

    // 🔥 API MỚI: Lấy lịch sử duyệt/từ chối khóa học
    @GetMapping("/{id}/approval-history")
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN','COURSE_VIEW')")
    public ResponseEntity<?> getApprovalHistory(@PathVariable Long id) {
        return ResponseEntity.ok(courseAdminService.getApprovalHistory(id));
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN','COURSE_EDIT')")
    public ResponseEntity<Void> updateStatus(
            @PathVariable Long id,
            @RequestParam String status
    ) {
        courseAdminService.updateCourseStatus(id, status, "");
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN','COURSE_EDIT')")
    public ResponseEntity<Void> updateCourse(
            @PathVariable Long id,
            @RequestBody CourseDTO courseDTO
    ) {
        courseAdminService.updateCourse(id, courseDTO);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN','COURSE_DELETE')")
    public ResponseEntity<Void> deleteCourse(@PathVariable Long id) {
        courseAdminService.deleteCourse(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/batch-sale")
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN','COURSE_BATCH_SALE')")
    public ResponseEntity<Void> applyBatchSale(@RequestBody org.example.multileanproject.dto.BatchSaleRequest request) {
        courseAdminService.applyBatchSale(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/bulk-approval")
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN','COURSE_BULK_APPROVAL')")
    public ResponseEntity<Void> bulkApproval(@RequestBody CourseBulkApprovalRequest request) {
        courseAdminService.bulkApproveOrReject(request);
        return ResponseEntity.ok().build();
    }
}
