package org.example.multileanproject.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.multileanproject.dto.MyCourseResponse;
import org.example.multileanproject.entity.Course;
import org.example.multileanproject.entity.Enrollment;
import org.example.multileanproject.entity.Student;
import org.example.multileanproject.repository.CourseRepository;
import org.example.multileanproject.repository.EnrollmentRepository;
import org.example.multileanproject.repository.LearningProgressRepository;
import org.example.multileanproject.repository.LessonRepository;
import org.example.multileanproject.repository.OrderRepository;
import org.example.multileanproject.repository.RefundRequestRepository;
import org.example.multileanproject.repository.StudentRepository;
import org.example.multileanproject.service.EnrollmentService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EnrollmentServiceImpl implements EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;
    private final LearningProgressRepository learningProgressRepository;
    private final LessonRepository lessonRepository;
    private final OrderRepository orderRepository;
    private final RefundRequestRepository refundRequestRepository;

    @Override
    @Transactional
    public void activateCourse(Long studentId, Long courseId) {
        if (enrollmentRepository.existsByStudent_IdAndCourse_IdAndStatus(studentId, courseId, "ACTIVE")) {
            return;
        }

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));

        Enrollment enrollment = enrollmentRepository.findByStudent_IdAndCourse_Id(studentId, courseId)
                .orElseGet(Enrollment::new);

        boolean isNew = enrollment.getId() == null;

        enrollment.setStudent(student);
        enrollment.setCourse(course);
        enrollment.setEnrolledAt(LocalDateTime.now());
        enrollment.setProgress(enrollment.getProgress() != null ? enrollment.getProgress() : BigDecimal.ZERO);
        enrollment.setStatus("ACTIVE");

        enrollmentRepository.save(enrollment);

        // Chỉ tăng studentCount khi đây là enrollment mới, không tăng khi reactivate
        if (isNew) {
            Integer currentStudentCount = course.getStudentCount() != null ? course.getStudentCount() : 0;
            course.setStudentCount(currentStudentCount + 1);
            courseRepository.save(course);
        }
    }

    @Override
    @Transactional
    public List<MyCourseResponse> getMyCourses(String email) {
        Student student = studentRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sinh viên: " + email));

        List<Enrollment> enrollments = enrollmentRepository.findByStudent_IdAndStatus(student.getId(), "ACTIVE");

        List<Long> courseIds = enrollments.stream()
                .map(enrollment -> enrollment.getCourse().getId())
                .distinct()
                .toList();

        Map<Long, String> latestRefundStatusByCourseId = new HashMap<>();
        Map<Long, Object[]> latestCompletedOrderSummaryByCourseId = new HashMap<>();
        if (!courseIds.isEmpty()) {
            List<Object[]> latestStatusRows = refundRequestRepository
                    .findLatestStatusByStudentAndCourseIds(student.getId(), courseIds);
            for (Object[] row : latestStatusRows) {
                Long courseId = row[0] != null ? ((Number) row[0]).longValue() : null;
                String status = row[1] != null ? row[1].toString() : null;
                if (courseId != null && status != null) {
                    latestRefundStatusByCourseId.put(courseId, status);
                }
            }

            List<Object[]> latestPaidRows = orderRepository
                    .findLatestCompletedOrderSummaryByStudentAndCourseIds(student.getId(), courseIds);
            for (Object[] row : latestPaidRows) {
                Long courseId = row[0] != null ? ((Number) row[0]).longValue() : null;
                if (courseId != null) {
                    latestCompletedOrderSummaryByCourseId.put(courseId, row);
                }
            }
        }

        return enrollments.stream().map(enrollment -> {
            Course course = enrollment.getCourse();

            String instructorName = "Giảng viên EduStar";
            if (course.getInstructor() != null && course.getInstructor().getUser() != null) {
                instructorName = course.getInstructor().getUser().getFullName();
            }

            String categoryName = course.getCategory() != null ? course.getCategory().getName() : null;

            MyCourseResponse res = new MyCourseResponse();
            res.setId(course.getId());
            res.setTitle(course.getTitle());
            res.setSlug(course.getSlug());
            res.setThumbnail(course.getThumbnail());
            res.setInstructorName(instructorName);
            // Tính tiến độ live từ LearningProgress để tránh dữ liệu cũ không đồng bộ
            long totalLessons = lessonRepository.countBySection_Course_Id(course.getId());
            Integer totalDurationSeconds = lessonRepository.sumDurationByCourseId(course.getId());
            int safeTotalDuration = totalDurationSeconds != null ? Math.max(totalDurationSeconds, 0) : 0;
            BigDecimal liveProgress;
            if (Boolean.TRUE.equals(enrollment.getIsCourseCompleted())) {
                // Đã hoàn thành -> khoá ở 100%, không tính lại để tránh tụt khi giảng viên thêm bài mới sau khi học viên đã xong.
                liveProgress = new BigDecimal("100.00");
            } else if (totalLessons == 0) {
                liveProgress = BigDecimal.ZERO;
            } else {
                long completedLessons = learningProgressRepository.countCompletedLessons(student.getId(), course.getId());
                double pct = ((double) completedLessons / totalLessons) * 100;
                liveProgress = BigDecimal.valueOf(pct).setScale(2, RoundingMode.HALF_UP);
            }
            // Đồng bộ lại enrollment nếu lệch
            boolean needsSave = false;
            if (liveProgress.compareTo(enrollment.getProgress() != null ? enrollment.getProgress() : BigDecimal.ZERO) != 0) {
                enrollment.setProgress(liveProgress);
                needsSave = true;
            }
            if (liveProgress.compareTo(new BigDecimal("100.00")) >= 0 && !Boolean.TRUE.equals(enrollment.getIsCourseCompleted())) {
                enrollment.setIsCourseCompleted(true);
                needsSave = true;
            }
            if (needsSave) {
                enrollmentRepository.save(enrollment);
            }
            res.setProgress(liveProgress);
            res.setIsCourseCompleted(Boolean.TRUE.equals(enrollment.getIsCourseCompleted()));
            res.setCategoryName(categoryName);
            res.setAverageRating(course.getAverageRating());
            res.setTotalLessons((int) totalLessons);
            res.setTotalDuration(safeTotalDuration);
            res.setLevel(course.getLevel());
            res.setEnrolledAt(enrollment.getEnrolledAt());

            String refundStatus = latestRefundStatusByCourseId.get(course.getId());
            res.setHasPendingRefund("PENDING".equalsIgnoreCase(refundStatus));
            res.setHasRejectedRefund("REJECTED".equalsIgnoreCase(refundStatus));

            Object[] latestPaid = latestCompletedOrderSummaryByCourseId.get(course.getId());
            if (latestPaid != null) {
                BigDecimal originalPaid = toBigDecimal(latestPaid[1]);
                BigDecimal finalPaid = toBigDecimal(latestPaid[2]);
                BigDecimal discountAmount = originalPaid.subtract(finalPaid);
                if (discountAmount.compareTo(BigDecimal.ZERO) < 0) {
                    discountAmount = BigDecimal.ZERO;
                }

                BigDecimal discountPercent = BigDecimal.ZERO;
                if (originalPaid.compareTo(BigDecimal.ZERO) > 0) {
                    discountPercent = discountAmount
                            .multiply(BigDecimal.valueOf(100))
                            .divide(originalPaid, 2, RoundingMode.HALF_UP);
                }

                res.setOriginalPaidAmount(originalPaid);
                res.setFinalPaidAmount(finalPaid);
                res.setDiscountAmount(discountAmount);
                res.setDiscountPercent(discountPercent);
                res.setCouponCode(latestPaid[3] != null ? latestPaid[3].toString() : null);
            }

            LocalDateTime expiresAt = enrollment.getRentalExpiresAt();
            if (expiresAt != null) {
                res.setRentalExpiresAt(expiresAt);
                long daysLeft = ChronoUnit.DAYS.between(LocalDateTime.now(), expiresAt);
                res.setRentalDaysLeft(Math.max(0, daysLeft));
            }
            return res;
        }).collect(Collectors.toList());
    }

    @Override
    public boolean isStudentEnrolled(Long studentId, Long courseId) {
        return enrollmentRepository.existsByStudent_IdAndCourse_IdAndStatus(studentId, courseId, "ACTIVE");
    }

    private BigDecimal toBigDecimal(Object value) {
        if (value == null) {
            return BigDecimal.ZERO;
        }
        if (value instanceof BigDecimal bigDecimal) {
            return bigDecimal;
        }
        try {
            return new BigDecimal(value.toString());
        } catch (Exception ex) {
            return BigDecimal.ZERO;
        }
    }

    @Override
    @Transactional
    public void activateRental(Long studentId, Long courseId, int durationDays) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));

        LocalDateTime expiresAt = LocalDateTime.now().plusDays(durationDays);

        Enrollment enrollment = enrollmentRepository.findByStudent_IdAndCourse_Id(studentId, courseId)
                .orElse(null);

        if (enrollment == null) {
            enrollment = Enrollment.builder()
                    .student(student).course(course)
                    .enrolledAt(LocalDateTime.now())
                    .progress(BigDecimal.ZERO)
                    .status("ACTIVE")
                    .isCourseCompleted(false)
                    .completionEmailSent(false)
                    .certificateIssued(false)
                    .rentalExpiresAt(expiresAt)
                    .build();
            enrollmentRepository.save(enrollment);
            course.setStudentCount((course.getStudentCount() != null ? course.getStudentCount() : 0) + 1);
            courseRepository.save(course);
        } else {
            // Gia hạn: cộng thêm từ thời điểm hiện tại hoặc từ ngày hết hạn
            LocalDateTime base = enrollment.getRentalExpiresAt() != null
                    && enrollment.getRentalExpiresAt().isAfter(LocalDateTime.now())
                    ? enrollment.getRentalExpiresAt() : LocalDateTime.now();
            enrollment.setRentalExpiresAt(base.plusDays(durationDays));
            enrollment.setStatus("ACTIVE");
            enrollmentRepository.save(enrollment);
        }
    }
}
