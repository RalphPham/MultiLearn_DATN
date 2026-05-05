package org.example.multileanproject.controller;

import lombok.RequiredArgsConstructor;
import org.example.multileanproject.dto.StudentProfileDTO;
import org.example.multileanproject.entity.Student;
import org.example.multileanproject.service.StudentService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/students")
@RequiredArgsConstructor
public class StudentController {

    private final StudentService studentService;

    // 1. Xem hồ sơ — chỉ chính chủ mới xem được
    @GetMapping("/{id}")
    public ResponseEntity<?> getStudent(@PathVariable Long id) {
        try {
            String currentEmail = SecurityContextHolder.getContext().getAuthentication().getName();
            Student student = studentService.getStudentById(id);

            if (!currentEmail.equalsIgnoreCase(student.getEmail())) {
                return ResponseEntity.status(403).body("Bạn không có quyền xem thông tin tài khoản này.");
            }

            StudentProfileDTO dto = StudentProfileDTO.builder()
                    .id(student.getId())
                    .fullName(student.getFullName())
                    .email(student.getEmail())
                    .phone(student.getPhone())
                    .avatar(student.getAvatar())
                    .balance(student.getBalance())
                    .role(student.getRole().name())
                    .build();

            return ResponseEntity.ok(dto);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Không tìm thấy sinh viên ID: " + id);
        }
    }

    // 2. Cập nhật hồ sơ — chỉ chính chủ mới sửa được
    @PutMapping("/{id}")
    public ResponseEntity<?> updateStudent(@PathVariable Long id, @RequestBody Map<String, Object> updates) {
        try {
            String currentEmail = SecurityContextHolder.getContext().getAuthentication().getName();
            Student existing = studentService.getStudentById(id);

            if (!currentEmail.equalsIgnoreCase(existing.getEmail())) {
                return ResponseEntity.status(403).body("Bạn không có quyền chỉnh sửa tài khoản này.");
            }

            Student updatedStudent = studentService.updateStudent(id, updates);

            StudentProfileDTO dto = StudentProfileDTO.builder()
                    .id(updatedStudent.getId())
                    .fullName(updatedStudent.getFullName())
                    .email(updatedStudent.getEmail())
                    .phone(updatedStudent.getPhone())
                    .avatar(updatedStudent.getAvatar())
                    .balance(updatedStudent.getBalance())
                    .role(updatedStudent.getRole().name())
                    .build();

            return ResponseEntity.ok(dto);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Lỗi cập nhật: " + e.getMessage());
        }
    }

    // 3. Nâng cấp lên Giảng viên
    @PostMapping("/upgrade-instructor")
    public ResponseEntity<String> upgradeToInstructor(@RequestBody(required = false) Map<String, Object> surveyData) {
        try {
            String currentEmail = SecurityContextHolder.getContext().getAuthentication().getName();
            studentService.upgradeToInstructor(currentEmail);
            return ResponseEntity.ok("Chúc mừng! Bạn đã nâng cấp lên Giảng viên thành công.");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Lỗi nâng cấp: " + e.getMessage());
        }
    }
}