package org.example.multileanproject.controller;

import lombok.RequiredArgsConstructor;
import org.example.multileanproject.dto.LeaderboardEntryDTO;
import org.example.multileanproject.repository.CourseRepository;
import org.example.multileanproject.repository.EnrollmentRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
public class LeaderboardController {

    private final EnrollmentRepository enrollmentRepository;
    private final CourseRepository courseRepository;

    /**
     * GET /api/courses/{courseId}/leaderboard
     * - Student: phải đang enrolled trong course
     * - Instructor: phải sở hữu course
     * Trả về top 50, student thấy tên rút gọn của người khác, instructor thấy full name.
     */
    @GetMapping("/{courseId}/leaderboard")
    public ResponseEntity<?> getLeaderboard(
            @PathVariable Long courseId,
            Authentication authentication) {

        if (authentication == null || "anonymousUser".equals(authentication.getName())) {
            return ResponseEntity.status(401).body("Bạn chưa đăng nhập.");
        }

        String email = authentication.getName();

        // Kiểm tra course tồn tại
        if (!courseRepository.existsById(courseId)) {
            return ResponseEntity.notFound().build();
        }

        // Xác định role: instructor sở hữu course?
        boolean isInstructor = courseRepository.findByInstructorEmail(email)
                .stream().anyMatch(c -> c.getId().equals(courseId));

        // Student: phải enrolled
        if (!isInstructor) {
            boolean enrolled = enrollmentRepository.existsByStudent_EmailAndCourse_IdAndStatus(
                    email, courseId, "ACTIVE");
            if (!enrolled) {
                return ResponseEntity.status(403).body("Bạn chưa ghi danh khóa học này.");
            }
        }

        List<Object[]> rows = enrollmentRepository.findLeaderboardNative(courseId);

        List<LeaderboardEntryDTO> result = new ArrayList<>();
        int rank = 1;
        for (Object[] row : rows) {
            // row: [0]=enrollmentId, [1]=studentId, [2]=fullName, [3]=email,
            //       [4]=avatar, [5]=progress, [6]=isCourseCompleted, [7]=courseCompletedAt
            Long studentId = row[1] != null ? ((Number) row[1]).longValue() : null;
            String studentFullName = row[2] != null ? row[2].toString() : null;
            String studentEmail = row[3] != null ? row[3].toString() : null;
            String avatar = row[4] != null ? row[4].toString() : null;
            double progress = row[5] != null ? ((Number) row[5]).doubleValue() : 0.0;
            boolean completed = row[6] != null && (
                    row[6] instanceof Boolean b ? b : ((Number) row[6]).intValue() == 1);
            LocalDateTime completedAt = row[7] instanceof LocalDateTime ldt ? ldt : null;

            boolean isMe = email.equals(studentEmail);

            String displayName;
            if (isInstructor) {
                displayName = studentFullName;
            } else if (isMe) {
                displayName = "Bạn (" + studentFullName + ")";
            } else {
                displayName = studentFullName != null && studentFullName.length() > 1
                        ? studentFullName.charAt(0) + "***"
                        : "Ẩn danh";
            }

            result.add(LeaderboardEntryDTO.builder()
                    .rank(rank++)
                    .studentId(studentId)
                    .displayName(displayName)
                    .avatar(avatar)
                    .progress(progress)
                    .isCourseCompleted(completed)
                    .courseCompletedAt(completedAt)
                    .isMe(isMe)
                    .build());
        }

        return ResponseEntity.ok(result);
    }
}
