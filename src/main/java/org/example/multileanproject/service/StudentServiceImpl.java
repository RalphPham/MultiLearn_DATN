package org.example.multileanproject.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.multileanproject.dto.StudentStatusRequestDTO;
import org.example.multileanproject.entity.Course;
import org.example.multileanproject.entity.CourseApprovalHistory;
import org.example.multileanproject.entity.CourseStatus;
import org.example.multileanproject.entity.Instructor;
import org.example.multileanproject.entity.Notification;
import org.example.multileanproject.entity.Role;
import org.example.multileanproject.entity.Student;
import org.example.multileanproject.entity.StudentStatusHistory;
import org.example.multileanproject.repository.AdminRepository;
import org.example.multileanproject.repository.CourseApprovalHistoryRepository;
import org.example.multileanproject.repository.CourseRepository;
import org.example.multileanproject.repository.InstructorRepository;
import org.example.multileanproject.repository.StudentRepository;
import org.example.multileanproject.repository.StudentStatusHistoryRepository;
import org.example.multileanproject.util.TextEncodingUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class StudentServiceImpl implements StudentService {

    private static final String STATUS_ACTIVE = "ACTIVE";
    private static final String STATUS_LOCKED = "LOCKED";
    private static final String AUTO_BLOCK_REASON = "AUTO_BLOCKED_BY_ACCOUNT_LOCK";
    private static final String AUTO_REVIEW_REASON = "AUTO_MOVE_TO_PENDING_AFTER_ACCOUNT_UNLOCK";

    private final StudentRepository studentRepository;
    private final InstructorRepository instructorRepository;
    private final CourseRepository courseRepository;
    private final CourseApprovalHistoryRepository courseApprovalHistoryRepository;
    private final StudentStatusHistoryRepository studentStatusHistoryRepository;
    private final NotificationService notificationService;
    private final EmailService emailService;
    private final AdminRepository adminRepository;
    private final AdminActionLogService adminActionLogService;

    @Override
    public Student getStudentById(Long id) {
        return studentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Khong tim thay tai khoan: " + id));
    }

    @Override
    public Student getStudentByEmail(String email) {
        return studentRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new RuntimeException("Khong tim thay tai khoan voi email: " + email));
    }

    @Override
    @Transactional
    public Student updateStudent(Long id, Map<String, Object> updates) {
        Student student = getStudentById(id);

        if (updates.containsKey("fullName")) {
            student.setFullName((String) updates.get("fullName"));
        }
        if (updates.containsKey("phone")) {
            student.setPhone((String) updates.get("phone"));
        }
        if (updates.containsKey("avatar")) {
            String avatarUrl = (String) updates.get("avatar");
            if (avatarUrl != null && !avatarUrl.startsWith("blob:")) {
                student.setAvatar(avatarUrl);
            }
        }

        return studentRepository.saveAndFlush(student);
    }

    @Override
    @Transactional
    public void upgradeToInstructor(String email) {
        Student student = getStudentByEmail(email);

        if (student.getRole() != Role.INSTRUCTOR) {
            student.setRole(Role.INSTRUCTOR);
            studentRepository.save(student);
        }

        if (instructorRepository.existsByUser(student)) {
            return;
        }

        Instructor instructor = Instructor.builder()
                .user(student)
                .fullName(student.getFullName())
                .phone(student.getPhone())
                .avatarUrl(student.getAvatar())
                .walletBalance(BigDecimal.ZERO)
                .isActive(student.isActive())
                .build();
        instructorRepository.save(instructor);
    }

    @Override
    @Transactional
    public void updateStudentStatus(Long studentId, StudentStatusRequestDTO request, String adminEmail) {
        Student student = getStudentById(studentId);
        boolean oldActive = student.isActive();
        boolean newActive = request.isActive();

        if (request.getAdminNote() != null) {
            student.setAdminNote(TextEncodingUtil.normalize(request.getAdminNote().trim()));
        }
        student.setActive(newActive);
        studentRepository.save(student);

        String reason = resolveReason(request, newActive);
        saveStatusHistory(student, oldActive, newActive, reason, adminEmail);

        int autoChangedCourses = 0;
        Optional<Instructor> instructorOpt = instructorRepository.findByUser(student);
        if (instructorOpt.isPresent()) {
            Instructor instructor = instructorOpt.get();
            instructor.setIsActive(newActive);
            instructorRepository.save(instructor);

            if (!newActive) {
                autoChangedCourses = autoBlockPublishedCourses(instructor, reason);
            } else {
                autoChangedCourses = restoreAutoBlockedCoursesToPending(instructor, reason);
            }
        }

        sendStatusNotification(student, newActive, reason, autoChangedCourses);
        sendStatusEmail(student, newActive, reason, request.isSendWarningEmail());
        writeAdminLog(student, newActive, reason, autoChangedCourses);
    }

    private String resolveReason(StudentStatusRequestDTO request, boolean newActive) {
        String rawReason = request != null ? request.getReason() : null;
        if (rawReason != null && !rawReason.isBlank()) {
            return TextEncodingUtil.normalize(rawReason.trim());
        }
        return newActive
                ? "Tai khoan duoc mo khoa boi admin."
                : "Tai khoan bi khoa boi admin.";
    }

    private void saveStatusHistory(Student student, boolean oldActive, boolean newActive, String reason, String adminEmail) {
        StudentStatusHistory history = StudentStatusHistory.builder()
                .student(student)
                .oldStatus(oldActive ? STATUS_ACTIVE : STATUS_LOCKED)
                .newStatus(newActive ? STATUS_ACTIVE : STATUS_LOCKED)
                .reason(reason)
                .actionBy(adminEmail != null && !adminEmail.isBlank() ? adminEmail : "System Admin")
                .build();
        studentStatusHistoryRepository.save(history);
    }

    private int autoBlockPublishedCourses(Instructor instructor, String statusReason) {
        List<Course> courses = courseRepository.findByInstructorUserId(instructor.getUser().getId());
        int affected = 0;

        for (Course course : courses) {
            if (course.getStatus() != CourseStatus.PUBLISHED) {
                continue;
            }

            CourseStatus before = course.getStatus();
            course.setStatus(CourseStatus.BLOCKED);
            courseRepository.save(course);

            saveCourseHistory(course.getId(), before.name(), CourseStatus.BLOCKED.name(), buildAutoBlockReason(statusReason));
            affected++;
        }

        return affected;
    }

    private int restoreAutoBlockedCoursesToPending(Instructor instructor, String statusReason) {
        List<Course> courses = courseRepository.findByInstructorUserId(instructor.getUser().getId());
        int affected = 0;

        for (Course course : courses) {
            if (course.getStatus() != CourseStatus.BLOCKED) {
                continue;
            }
            if (!isLatestHistoryAutoBlock(course.getId())) {
                continue;
            }

            CourseStatus before = course.getStatus();
            course.setStatus(CourseStatus.PENDING_APPROVAL);
            courseRepository.save(course);

            String reviewReason = AUTO_REVIEW_REASON;
            if (statusReason != null && !statusReason.isBlank()) {
                reviewReason = reviewReason + " | " + statusReason;
            }
            saveCourseHistory(course.getId(), before.name(), CourseStatus.PENDING_APPROVAL.name(), reviewReason);
            affected++;
        }

        return affected;
    }

    private boolean isLatestHistoryAutoBlock(Long courseId) {
        Optional<CourseApprovalHistory> latest = courseApprovalHistoryRepository
                .findTopByCourseIdOrderByCreatedAtDescIdDesc(courseId);
        if (latest.isEmpty()) {
            return false;
        }
        String reason = latest.get().getReason();
        return reason != null && reason.startsWith(AUTO_BLOCK_REASON);
    }

    private void saveCourseHistory(Long courseId, String statusBefore, String statusAfter, String reason) {
        Long adminId = resolveCurrentAdminId();
        CourseApprovalHistory history = CourseApprovalHistory.builder()
                .courseId(courseId)
                .adminId(adminId)
                .statusBefore(statusBefore)
                .statusAfter(statusAfter)
                .reason(reason)
                .createdAt(LocalDateTime.now())
                .build();
        courseApprovalHistoryRepository.save(history);
    }

    private Long resolveCurrentAdminId() {
        try {
            return adminRepository.findByUsername(getCurrentAdminName())
                    .map(a -> a.getId())
                    .orElse(1L);
        } catch (Exception ignored) {
            return 1L;
        }
    }

    private String getCurrentAdminName() {
        try {
            return org.springframework.security.core.context.SecurityContextHolder
                    .getContext()
                    .getAuthentication()
                    .getName();
        } catch (Exception ignored) {
            return "System Admin";
        }
    }

    private String buildAutoBlockReason(String statusReason) {
        if (statusReason == null || statusReason.isBlank()) {
            return AUTO_BLOCK_REASON;
        }
        return AUTO_BLOCK_REASON + " | " + statusReason;
    }

    private void sendStatusNotification(Student student, boolean active, String reason, int autoChangedCourses) {
        String title = active ? "Tai khoan da duoc mo khoa" : "Tai khoan da bi khoa";
        String actionText = active ? "mo khoa" : "khoa";
        StringBuilder message = new StringBuilder("Tai khoan cua ban da duoc ")
                .append(actionText)
                .append(".");
        if (reason != null && !reason.isBlank()) {
            message.append(" Ly do: ").append(reason);
        }
        if (autoChangedCourses > 0) {
            if (active) {
                message.append(" ").append(autoChangedCourses)
                        .append(" khoa hoc bi chan tu dong da duoc chuyen ve trang thai cho duyet lai.");
            } else {
                message.append(" ").append(autoChangedCourses)
                        .append(" khoa hoc dang ban da bi chan tu dong.");
            }
        }

        notificationService.createNotification(
                student.getId(),
                TextEncodingUtil.normalize(title),
                TextEncodingUtil.normalize(message.toString()),
                "/profile",
                "ACCOUNT_STATUS_CHANGED",
                Notification.NotificationCategory.SYSTEM_NOTIFICATION,
                true,
                null,
                student.getFullName()
        );
    }

    private void sendStatusEmail(Student student, boolean active, String reason, boolean sendWarningEmail) {
        try {
            if (active) {
                emailService.sendAccountUnlockedEmail(student.getEmail(), student.getFullName(), reason);
                if (sendWarningEmail) {
                    emailService.sendWarningEmail(student.getEmail(), student.getFullName(), reason);
                }
            } else {
                emailService.sendAccountLockedEmail(student.getEmail(), student.getFullName(), reason);
            }
        } catch (Exception ex) {
            log.warn("Failed to send account status email for user {}: {}", student.getId(), ex.getMessage());
        }
    }

    private void writeAdminLog(Student student, boolean active, String reason, int autoChangedCourses) {
        String action = active ? "UNLOCK_USER" : "LOCK_USER";
        String description = "Tai khoan " + student.getEmail() + " -> " + (active ? "ACTIVE" : "LOCKED")
                + (reason != null && !reason.isBlank() ? " | Ly do: " + reason : "")
                + " | Auto-course-changed: " + autoChangedCourses;
        adminActionLogService.log(action, description, student.getId(), "USER");
    }
}

