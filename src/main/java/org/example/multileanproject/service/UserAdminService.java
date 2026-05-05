package org.example.multileanproject.service;

import lombok.RequiredArgsConstructor;
import org.example.multileanproject.dto.StudentStatusRequestDTO;
import org.example.multileanproject.dto.UserCourseDTO;
import org.example.multileanproject.dto.UserDTO;
import org.example.multileanproject.entity.Instructor;
import org.example.multileanproject.entity.Role;
import org.example.multileanproject.entity.Student;
import org.example.multileanproject.repository.CourseRepository;
import org.example.multileanproject.repository.InstructorRepository;
import org.example.multileanproject.repository.OrderRepository;
import org.example.multileanproject.repository.StudentRepository;
import org.springframework.data.domain.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserAdminService {

    private final StudentRepository studentRepository;
    private final InstructorRepository instructorRepository;
    private final OrderRepository orderRepository;
    private final CourseRepository courseRepository;
    private final AdminActionLogService adminActionLogService;
    private final StudentService studentService;

    // â”€â”€ Danh sÃ¡ch user â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
    public Page<UserDTO> getAllUsers(String keyword, String roleStr, String statusStr, Pageable pageable) {
        String kw   = (keyword != null && !keyword.isBlank()) ? keyword.trim() : null;
        String role = (roleStr != null && !roleStr.isBlank()) ? roleStr.trim().toUpperCase() : null;
        Boolean activeFilter = null;
        if ("ACTIVE".equalsIgnoreCase(statusStr)) activeFilter = true;
        else if ("LOCKED".equalsIgnoreCase(statusStr)) activeFilter = false;
        return studentRepository.searchUsers(kw, role, activeFilter, pageable).map(this::convertToDTO);
    }

    // â”€â”€ KhÃ³a / Má»Ÿ khÃ³a â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
    @Transactional
    public void updateUserStatus(Long userId, String status) {
        boolean active = "ACTIVE".equalsIgnoreCase(status);
        StudentStatusRequestDTO request = new StudentStatusRequestDTO();
        request.setActive(active);
        request.setReason(active ? "Mo khoa tai khoan boi admin" : "Khoa tai khoan boi admin");
        request.setSource("admin_users_endpoint");
        studentService.updateStudentStatus(userId, request, getCurrentAdminName());
    }

    private String getCurrentAdminName() {
        try {
            return SecurityContextHolder.getContext().getAuthentication().getName();
        } catch (Exception ignored) {
            return "System Admin";
        }
    }

    // â”€â”€ Cáº­p nháº­t thÃ´ng tin â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
    @Transactional
    public UserDTO updateUser(Long userId, UserDTO dto) {
        Student s = studentRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User khÃ´ng tá»“n táº¡i: " + userId));
        if (dto.getFullName() != null && !dto.getFullName().isBlank())
            s.setFullName(dto.getFullName());
        if (dto.getRole() != null && !dto.getRole().isBlank()) {
            try { s.setRole(Role.valueOf(dto.getRole().toUpperCase())); }
            catch (IllegalArgumentException e) { throw new RuntimeException("Role khÃ´ng há»£p lá»‡: " + dto.getRole()); }
        }
        UserDTO result = convertToDTO(studentRepository.save(s));
        adminActionLogService.log("UPDATE_USER",
                "Cáº­p nháº­t thÃ´ng tin user: " + s.getEmail(), userId, "USER");
        return result;
    }

    // â”€â”€ Danh sÃ¡ch khÃ³a há»c chi tiáº¿t (Drawer tab) â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
    public List<UserCourseDTO> getUserCourses(Long userId, String mode) {
        Student s = studentRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User khong ton tai: " + userId));

        String normalizedMode = mode != null ? mode.trim().toUpperCase() : "";
        if ("LEARNING".equals(normalizedMode)) {
            return studentRepository.findEnrolledCoursesByStudentId(userId)
                    .stream().map(this::mapStudentRow).collect(Collectors.toList());
        }
        if ("TEACHING".equals(normalizedMode)) {
            return studentRepository.findCoursesByInstructorStudentId(userId)
                    .stream().map(this::mapInstructorRow).collect(Collectors.toList());
        }

        // Backward compatibility: nếu FE cũ không truyền mode thì giữ logic theo role hiện tại.
        if (s.getRole() == Role.STUDENT) {
            return studentRepository.findEnrolledCoursesByStudentId(userId)
                    .stream().map(this::mapStudentRow).collect(Collectors.toList());
        }
        if (s.getRole() == Role.INSTRUCTOR) {
            return studentRepository.findCoursesByInstructorStudentId(userId)
                    .stream().map(this::mapInstructorRow).collect(Collectors.toList());
        }
        return List.of();
    }

    public List<UserCourseDTO> getUserCourses(Long userId) {
        return getUserCourses(userId, null);
    }

    // ── Danh sách giảng viên (admin list) ─────────────────────────────────────
    public Page<Map<String, Object>> getAllInstructors(String keyword, Pageable pageable) {
        String q = (keyword != null && !keyword.isBlank()) ? keyword.trim() : null;
        return instructorRepository.searchInstructors(q, pageable).map(i -> {
            Map<String, Object> item = new HashMap<>();
            item.put("id", i.getId());
            item.put("userId", i.getUser().getId());
            item.put("fullName", i.getFullName() != null ? i.getFullName() : i.getUser().getFullName());
            item.put("email", i.getUser().getEmail());
            item.put("phone", i.getPhone() != null ? i.getPhone() : i.getUser().getPhone());
            item.put("avatarUrl", i.getAvatarUrl() != null ? i.getAvatarUrl() : i.getUser().getAvatar());
            item.put("bankAccount", i.getBankAccount());
            item.put("bankName", i.getBankName());
            item.put("walletBalance", i.getWalletBalance());
            item.put("isActive", i.getIsActive());
            item.put("createdAt", i.getCreatedAt());
            item.put("totalCourses", courseRepository.countByInstructorUserId(i.getUser().getId()));
            return item;
        });
    }

    // â”€â”€ Instructor profile (trang xem chi tiáº¿t giáº£ng viÃªn) â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
    public Map<String, Object> getInstructorProfile(Long userId) {
        Student student = studentRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("KhÃ´ng tÃ¬m tháº¥y user: " + userId));

        Instructor instructor = instructorRepository.findByUser_Id(userId)
                .orElseThrow(() -> new RuntimeException("User nÃ y khÃ´ng pháº£i giáº£ng viÃªn: " + userId));

        List<org.example.multileanproject.entity.Course> courses = courseRepository.findByInstructorUserId(userId);
        long totalStudents = courses.stream()
                .mapToLong(c -> c.getStudentCount() != null ? c.getStudentCount() : 0L)
                .sum();

        BigDecimal totalRevenue = orderRepository.sumRevenueByInstructorUserId(userId);

        Map<String, Object> profile = new HashMap<>();
        profile.put("id", instructor.getId());
        profile.put("userId", student.getId());
        profile.put("fullName", instructor.getFullName() != null ? instructor.getFullName() : student.getFullName());
        profile.put("email", student.getEmail());
        profile.put("phone", instructor.getPhone() != null ? instructor.getPhone() : student.getPhone());
        profile.put("avatarUrl", instructor.getAvatarUrl() != null ? instructor.getAvatarUrl() : student.getAvatar());
        profile.put("bio", instructor.getBio());
        profile.put("bankAccount", instructor.getBankAccount());
        profile.put("bankName", instructor.getBankName());
        profile.put("walletBalance", instructor.getWalletBalance());
        profile.put("isActive", instructor.getIsActive());
        profile.put("createdAt", instructor.getCreatedAt());
        profile.put("totalCourses", (long) courses.size());
        profile.put("totalStudents", totalStudents);
        profile.put("totalRevenue", totalRevenue != null ? totalRevenue : BigDecimal.ZERO);
        return profile;
    }

    // â”€â”€ Private helpers â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    /**
     * courseCount phÃ¢n biá»‡t theo role:
     *   STUDENT    â†’ countEnrollments()   (sá»‘ khÃ³a Ä‘ang há»c)
     *   INSTRUCTOR â†’ countOwnedCourses()  (sá»‘ khÃ³a Ä‘ang dáº¡y)
     *   ADMIN      â†’ 0
     */
    private UserDTO convertToDTO(Student s) {
        int learningCourseCount = studentRepository.countEnrollments(s.getId());
        int teachingCourseCount = studentRepository.countOwnedCourses(s.getId());
        int courseCount = learningCourseCount + teachingCourseCount;

        return UserDTO.builder()
                .id(s.getId())
                .fullName(s.getFullName())
                .email(s.getEmail())
                .role(s.getRole() != null ? s.getRole().name() : null)
                .status(s.isActive() ? "ACTIVE" : "LOCKED")
                .avatar(s.getAvatar())
                .createdAt(s.getCreatedAt())
                .courseCount(courseCount)
                .learningCourseCount(learningCourseCount)
                .teachingCourseCount(teachingCourseCount)
                .build();
    }

    /**
     * Map 10 cá»™t tá»« findEnrolledCoursesByStudentId.
     * [9] progressPercent: JPQL tráº£ vá» Long tá»« phÃ©p chia nguyÃªn,
     *     dÃ¹ng ((Number) r[9]).intValue() Ä‘á»ƒ an toÃ n vá»›i cÃ¡c kiá»ƒu sá»‘ khÃ¡c nhau.
     */
    private UserCourseDTO mapStudentRow(Object[] r) {
        return UserCourseDTO.builder()
                .id(toLong(r[0]))
                .title((String) r[1])
                .slug((String) r[2])
                .thumbnail((String) r[3])
                .status(r[4] != null ? r[4].toString() : null)
                .price(r[5] != null ? (BigDecimal) r[5] : null)
                .salePrice(r[6] != null ? (BigDecimal) r[6] : null)
                .enrolledAt((LocalDateTime) r[7])
                .categoryName((String) r[8])
                .progressPercent(r[9] != null ? ((Number) r[9]).intValue() : 0)
                .build();
    }

    /** Map 10 cá»™t tá»« findCoursesByInstructorStudentId. */
    private UserCourseDTO mapInstructorRow(Object[] r) {
        return UserCourseDTO.builder()
                .id(toLong(r[0]))
                .title((String) r[1])
                .slug((String) r[2])
                .thumbnail((String) r[3])
                .status(r[4] != null ? r[4].toString() : null)
                .price(r[5] != null ? (BigDecimal) r[5] : null)
                .salePrice(r[6] != null ? (BigDecimal) r[6] : null)
                .createdAt((LocalDateTime) r[7])
                .categoryName((String) r[8])
                .studentCount(r[9] != null ? ((Number) r[9]).longValue() : 0L)
                .build();
    }

    private Long toLong(Object o) {
        return o != null ? ((Number) o).longValue() : null;
    }
}

