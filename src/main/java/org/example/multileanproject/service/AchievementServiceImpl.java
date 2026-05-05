package org.example.multileanproject.service;

import lombok.RequiredArgsConstructor;
import org.example.multileanproject.dto.BadgeDTO;
import org.example.multileanproject.repository.EnrollmentRepository;
import org.example.multileanproject.repository.LearningProgressRepository;
import org.example.multileanproject.repository.StudentRepository;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AchievementServiceImpl implements AchievementService {

    private final EnrollmentRepository enrollmentRepository;
    private final LearningProgressRepository learningProgressRepository;
    private final StudentRepository studentRepository;

    @Override
    public List<BadgeDTO> getBadges(String studentEmail) {
        var studentOpt = studentRepository.findByEmail(studentEmail);
        if (studentOpt.isEmpty()) return List.of();

        Long studentId = studentOpt.get().getId();

        // ── Tất cả dùng count queries, không lazy load ────────────────────
        long completedCount  = enrollmentRepository.countCompletedByStudentId(studentId);
        long enrolledCount   = enrollmentRepository.countActiveByStudentId(studentId);
        long categoryCount   = enrollmentRepository.countDistinctCategoriesByStudentId(studentId);
        boolean isFastLearner = enrollmentRepository.isFastLearner(studentId) > 0;
        LocalDateTime firstCompletedAt = enrollmentRepository.findFirstCompletedAt(studentId);

        // ── Streak ───────────────────────────────────────────────────────
        List<Date> learnDates = learningProgressRepository.findDistinctLearnDates(studentEmail);
        int currentStreak = calcCurrentStreak(learnDates);

        // ── Build badges ──────────────────────────────────────────────────
        List<BadgeDTO> badges = new ArrayList<>();

        badges.add(BadgeDTO.builder()
                .key("FIRST_COURSE").name("Khởi đầu xuất sắc")
                .description("Hoàn thành khóa học đầu tiên")
                .icon("🎓").category("COURSE")
                .isEarned(completedCount >= 1)
                .earnedAt(completedCount >= 1 ? firstCompletedAt : null)
                .build());

        badges.add(BadgeDTO.builder()
                .key("FIVE_COURSES").name("Học viên chăm chỉ")
                .description("Hoàn thành 5 khóa học")
                .icon("🏆").category("COURSE")
                .isEarned(completedCount >= 5)
                .earnedAt(null)
                .build());

        badges.add(BadgeDTO.builder()
                .key("TEN_COURSES").name("Bậc thầy tri thức")
                .description("Hoàn thành 10 khóa học")
                .icon("💎").category("COURSE")
                .isEarned(completedCount >= 10)
                .earnedAt(null)
                .build());

        badges.add(BadgeDTO.builder()
                .key("BOOKWORM").name("Mọt sách")
                .description("Ghi danh 5 khóa học trở lên")
                .icon("📚").category("COURSE")
                .isEarned(enrolledCount >= 5)
                .earnedAt(null)
                .build());

        badges.add(BadgeDTO.builder()
                .key("STREAK_3").name("Lửa học tập")
                .description("Học liên tục 3 ngày")
                .icon("🔥").category("STREAK")
                .isEarned(currentStreak >= 3)
                .earnedAt(null)
                .build());

        badges.add(BadgeDTO.builder()
                .key("STREAK_7").name("Không thể dừng lại")
                .description("Học liên tục 7 ngày")
                .icon("🌟").category("STREAK")
                .isEarned(currentStreak >= 7)
                .earnedAt(null)
                .build());

        badges.add(BadgeDTO.builder()
                .key("EXPLORER").name("Người khám phá")
                .description("Học khóa thuộc 3 danh mục khác nhau")
                .icon("🌈").category("SPECIAL")
                .isEarned(categoryCount >= 3)
                .earnedAt(null)
                .build());

        badges.add(BadgeDTO.builder()
                .key("FAST_LEARNER").name("Học nhanh như gió")
                .description("Hoàn thành khóa học trong 7 ngày")
                .icon("⚡").category("SPECIAL")
                .isEarned(isFastLearner)
                .earnedAt(null)
                .build());

        return badges;
    }

    @Override
    public int getCurrentStreak(String studentEmail) {
        List<Date> learnDates = learningProgressRepository.findDistinctLearnDates(studentEmail);
        return calcCurrentStreak(learnDates);
    }

    private int calcCurrentStreak(List<Date> learnDates) {
        if (learnDates == null || learnDates.isEmpty()) return 0;

        java.time.LocalDate today = java.time.LocalDate.now();
        java.time.LocalDate first = learnDates.get(0).toLocalDate();

        if (ChronoUnit.DAYS.between(first, today) > 1) return 0;

        int streak = 1;
        for (int i = 1; i < learnDates.size(); i++) {
            java.time.LocalDate prev = learnDates.get(i - 1).toLocalDate();
            java.time.LocalDate curr = learnDates.get(i).toLocalDate();
            if (ChronoUnit.DAYS.between(curr, prev) == 1) {
                streak++;
            } else {
                break;
            }
        }
        return streak;
    }
}
