package org.example.multileanproject.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.multileanproject.entity.*;
import org.example.multileanproject.repository.EnrollmentRepository;
import org.example.multileanproject.repository.LearningProgressRepository;
import org.example.multileanproject.repository.LessonRepository;
import org.example.multileanproject.repository.RefundRequestRepository;
import org.example.multileanproject.repository.StudentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class LearningProgressServiceImpl implements LearningProgressService {

    private final LearningProgressRepository learningProgressRepository;
    private final LessonRepository lessonRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final StudentRepository studentRepository;
    private final RefundRequestRepository refundRequestRepository;
    private final EmailService emailService;
    private final CertificateService certificateService;
    private final MinioUploadService minioUploadService;

    @Override
    @Transactional
    public double markLessonCompleted(String email, Long lessonId) {
        Student student = studentRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy học viên"));

        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bài học"));

        Course course = lesson.getSection().getCourse();

        // [SỬA THEO REPO GỐC] Dùng findByStudent_IdAndCourse_Id
        Enrollment enrollment = enrollmentRepository.findByStudent_IdAndCourse_Id(student.getId(), course.getId())
                .orElseThrow(() -> new RuntimeException("Chưa đăng ký khóa học này"));

        // [SỬA THEO REPO GỐC] Dùng existsByStudent_IdAndLesson_Id
        if (refundRequestRepository.countPendingRefundByStudentAndCourse(student.getId(), course.getId()) > 0) {
            throw new RuntimeException("Khoá học đang có yêu cầu hoàn tiền chờ duyệt. Tạm thời không thể tiếp tục học.");
        }

        boolean alreadyCompleted = learningProgressRepository.existsByStudent_IdAndLesson_Id(student.getId(), lessonId);

        if (!alreadyCompleted) {
            LearningProgress progress = LearningProgress.builder()
                    .student(student)
                    .enrollment(enrollment)
                    .lesson(lesson)
                    .isCompleted(true)
                    .completedAt(LocalDateTime.now())
                    .build();
            learningProgressRepository.save(progress);

            // Khoá quyền hoàn tiền ngay khi học bài đầu tiên
            if (enrollment.getRefundLockedAt() == null) {
                enrollment.setRefundLockedAt(LocalDateTime.now());
            }
        }

        // [SỬA] Tính toán dựa vào studentId thay vì enrollmentId
        double progressPercent = calculateProgress(student.getId(), course.getId());

        // [SỬA THEO ENTITY GỐC] Lưu vào biến progress (kiểu BigDecimal)
        enrollment.setProgress(BigDecimal.valueOf(progressPercent));
        enrollmentRepository.save(enrollment);

        if (progressPercent >= 100.0) {
            handleCourseCompletion(enrollment, student, course);
        }

        return progressPercent;
    }

    private double calculateProgress(Long studentId, Long courseId) {
        Enrollment enrollment = enrollmentRepository.findByStudent_IdAndCourse_Id(studentId, courseId).orElse(null);
        if (enrollment != null && Boolean.TRUE.equals(enrollment.getIsCourseCompleted())) {
            return 100.0;
        }

        long totalLessons = lessonRepository.countBySection_Course_Id(courseId);
        if (totalLessons == 0) return 0.0;

        long completedLessons = learningProgressRepository.countCompletedLessons(studentId, courseId);
        double progress = ((double) completedLessons / totalLessons) * 100;
        return BigDecimal.valueOf(progress).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }

    private void handleCourseCompletion(Enrollment enrollment, Student student, Course course) {
        if (!Boolean.TRUE.equals(enrollment.getIsCourseCompleted())) {
            enrollment.setIsCourseCompleted(true);
            enrollment.setCourseCompletedAt(LocalDateTime.now());
        }

        // Cấp chứng chỉ — không throw để tránh rollback tiến độ
        Certificate certificate = null;
        if (!Boolean.TRUE.equals(enrollment.getCertificateIssued())) {
            try {
                certificate = certificateService.issueCertificate(enrollment);
                enrollment.setCertificateIssued(true);
            } catch (Exception e) {
                log.error("Lỗi cấp chứng chỉ cho enrollment {}: {}", enrollment.getId(), e.getMessage());
                // Không throw — tiến độ vẫn được lưu, cert sẽ retry sau
            }
        }

        // Gửi email hoàn thành
        if (!Boolean.TRUE.equals(enrollment.getCompletionEmailSent())) {
            try {
                // Nếu cert chưa được cấp trong lần này, thử lấy từ DB
                if (certificate == null && Boolean.TRUE.equals(enrollment.getCertificateIssued())) {
                    certificate = certificateService.issueCertificate(enrollment);
                }
                if (certificate != null) {
                    emailService.sendCertificateIssuedEmail(student, course, certificate);
                    enrollment.setCompletionEmailSent(true);
                }
            } catch (Exception e) {
                log.error("Lỗi gửi email hoàn thành cho enrollment {}: {}", enrollment.getId(), e.getMessage());
                // Không throw — email sẽ retry sau
            }
        }
    }

    @Override
    @Transactional
    public void markCourseStarted(String email, Long courseId) {
        Student student = studentRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy học viên"));
        enrollmentRepository.findByStudent_IdAndCourse_Id(student.getId(), courseId)
                .ifPresent(enrollment -> {
                    if (enrollment.getRefundLockedAt() == null) {
                        enrollment.setRefundLockedAt(LocalDateTime.now());
                        enrollmentRepository.save(enrollment);
                    }
                });
    }

    // 🔥 LOGIC CHỐNG HACK VIDEO CỦA PHÚ
    @Override
    @Transactional(readOnly = true)
    public String getLessonVideoUrl(String email, Long lessonId) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bài học"));

        if (Boolean.TRUE.equals(lesson.getIsPreview())) {
            return lesson.getVideoUrl();
        }

        if (email == null || email.isBlank() || "anonymousUser".equals(email)) {
            throw new RuntimeException("Bạn cần đăng nhập để xem bài học này.");
        }

        Course course = lesson.getSection().getCourse();

        boolean isInstructorOwner = course.getInstructor() != null
                && course.getInstructor().getUser() != null
                && email.equalsIgnoreCase(course.getInstructor().getUser().getEmail());

        if (!isInstructorOwner && course.getStatus() == org.example.multileanproject.entity.CourseStatus.BLOCKED) {
            throw new RuntimeException("Khoá học này hiện đang bị khoá và không thể xem.");
        }

        boolean isEnrolled = enrollmentRepository.existsByStudent_EmailAndCourse_IdAndStatus(email, course.getId(), "ACTIVE");

        if (!isEnrolled && !isInstructorOwner) {
            throw new RuntimeException("FORBIDDEN: Bạn chưa mua khóa học này, không thể xem video!");
        }

        if (!isInstructorOwner) {
            Student student = studentRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy học viên."));
            if (refundRequestRepository.countPendingRefundByStudentAndCourse(student.getId(), course.getId()) > 0) {
                throw new RuntimeException("Khoá học đang có yêu cầu hoàn tiền chờ duyệt. Tạm thời không thể xem video.");
            }
        }

        String objectKey = minioUploadService.extractObjectKey(lesson.getVideoUrl());
        return minioUploadService.getPresignedUrl(objectKey, 120);
    }

    @Override
    @Transactional(readOnly = true)
    public String getLessonDocumentUrl(String email, Long lessonId) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bài học"));

        if (lesson.getDocumentUrl() == null) {
            throw new RuntimeException("Bài học này không có tài liệu.");
        }

        if (Boolean.TRUE.equals(lesson.getIsPreview())) {
            String objectKey = minioUploadService.extractObjectKey(lesson.getDocumentUrl());
            return minioUploadService.getPresignedUrl(objectKey, 120);
        }

        if (email == null || email.isBlank() || "anonymousUser".equals(email)) {
            throw new RuntimeException("Bạn cần đăng nhập để xem tài liệu này.");
        }

        Course course = lesson.getSection().getCourse();
        boolean isInstructorOwner = course.getInstructor() != null
                && course.getInstructor().getUser() != null
                && email.equalsIgnoreCase(course.getInstructor().getUser().getEmail());

        boolean isEnrolled = enrollmentRepository.existsByStudent_EmailAndCourse_IdAndStatus(email, course.getId(), "ACTIVE");

        if (!isEnrolled && !isInstructorOwner) {
            throw new RuntimeException("FORBIDDEN: Bạn chưa mua khóa học này, không thể xem tài liệu!");
        }

        if (!isInstructorOwner) {
            Student student = studentRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy học viên."));
            if (refundRequestRepository.countPendingRefundByStudentAndCourse(student.getId(), course.getId()) > 0) {
                throw new RuntimeException("Khoá học đang có yêu cầu hoàn tiền chờ duyệt. Tạm thời không thể xem tài liệu.");
            }
        }

        String objectKey = minioUploadService.extractObjectKey(lesson.getDocumentUrl());
        return minioUploadService.getPresignedUrl(objectKey, 120);
    }
}
