package org.example.multileanproject.controller;

import lombok.RequiredArgsConstructor;
import org.example.multileanproject.entity.Assignment;
import org.example.multileanproject.service.AssignmentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/assignments")
@RequiredArgsConstructor
public class AssignmentController {

    private final AssignmentService assignmentService;

    @PostMapping("/create")
    public ResponseEntity<?> create(@RequestBody Assignment assignment) {
        return ResponseEntity.ok(assignmentService.createAssignment(assignment));
    }

    @GetMapping("/section/{sectionId}")
    public ResponseEntity<?> getBySection(@PathVariable Long sectionId) {
        return ResponseEntity.ok(assignmentService.getAssignmentsBySection(sectionId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        assignmentService.deleteAssignment(id);
        return ResponseEntity.ok("Đã xóa bài tập");
    }
}