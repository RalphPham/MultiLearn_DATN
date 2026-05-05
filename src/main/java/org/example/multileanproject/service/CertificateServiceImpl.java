package org.example.multileanproject.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.multileanproject.entity.Certificate;
import org.example.multileanproject.entity.Course;
import org.example.multileanproject.entity.Enrollment;
import org.example.multileanproject.entity.Student;
import org.example.multileanproject.repository.CertificateRepository;
import org.example.multileanproject.repository.EnrollmentRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CertificateServiceImpl implements CertificateService {

    @Value("${app.frontend.url:http://localhost:5173}")
    private String frontendBaseUrl;

    private final CertificateRepository certificateRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final PdfCertificateService pdfCertificateService;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Certificate issueCertificate(Enrollment enrollment) {
        if (enrollment == null) {
            throw new RuntimeException("Không tìm thấy thông tin ghi danh.");
        }

        Student student = enrollment.getStudent();
        Course course = enrollment.getCourse();

        if (student == null) {
            throw new RuntimeException("Không tìm thấy học viên để cấp chứng chỉ.");
        }

        if (course == null) {
            throw new RuntimeException("Không tìm thấy khóa học để cấp chứng chỉ.");
        }

        if (!isCompletionReached(enrollment)) {
            throw new RuntimeException("Khóa học chưa hoàn thành, không thể cấp chứng chỉ.");
        }

        boolean enrollmentUpdated = false;
        if (!Boolean.TRUE.equals(enrollment.getIsCourseCompleted())) {
            enrollment.setIsCourseCompleted(true);
            if (enrollment.getCourseCompletedAt() == null) {
                enrollment.setCourseCompletedAt(LocalDateTime.now());
            }
            enrollmentUpdated = true;
        }
        if (!Boolean.TRUE.equals(enrollment.getCertificateIssued())) {
            enrollment.setCertificateIssued(true);
            enrollmentUpdated = true;
        }
        if (enrollmentUpdated) {
            enrollmentRepository.save(enrollment);
        }

        Certificate existingCertificate = null;

        if (enrollment.getId() != null) {
            existingCertificate = certificateRepository.findByEnrollment_Id(enrollment.getId()).orElse(null);
        }

        if (existingCertificate == null && student.getId() != null && course.getId() != null) {
            existingCertificate = certificateRepository
                    .findByStudent_IdAndCourse_Id(student.getId(), course.getId())
                    .orElse(null);
        }

        if (existingCertificate != null) {
            boolean changed = false;

            if (existingCertificate.getEnrollment() == null && enrollment.getId() != null) {
                existingCertificate.setEnrollment(enrollment);
                changed = true;
            }

            if (existingCertificate.getCompletedAt() == null) {
                existingCertificate.setCompletedAt(resolveCompletedAt(enrollment));
                changed = true;
            }

            if (existingCertificate.getIssuedAt() == null) {
                existingCertificate.setIssuedAt(LocalDateTime.now());
                changed = true;
            }

            if (existingCertificate.getVerifyUrl() == null || existingCertificate.getVerifyUrl().isBlank()) {
                existingCertificate.setVerifyUrl(buildVerifyUrl(existingCertificate.getCertificateCode()));
                changed = true;
            }

            if (existingCertificate.getPdfUrl() == null || existingCertificate.getPdfUrl().isBlank()) {
                String pdfUrl = pdfCertificateService.generateCertificatePdf(existingCertificate);
                existingCertificate.setPdfUrl(pdfUrl);
                changed = true;
            }

            if (existingCertificate.getIsActive() == null) {
                existingCertificate.setIsActive(true);
                changed = true;
            }

            if (changed) {
                existingCertificate = certificateRepository.save(existingCertificate);
            }

            log.info(
                    "Certificate already exists. code={}, studentId={}, courseId={}",
                    existingCertificate.getCertificateCode(),
                    student.getId(),
                    course.getId()
            );

            return existingCertificate;
        }

        String certificateCode = generateUniqueCertificateCode(student.getId(), course.getId());

        Certificate certificate = Certificate.builder()
                .certificateCode(certificateCode)
                .student(student)
                .course(course)
                .enrollment(enrollment)
                .issuedAt(LocalDateTime.now())
                .completedAt(resolveCompletedAt(enrollment))
                .verifyUrl(buildVerifyUrl(certificateCode))
                .isActive(true)
                .build();

        certificate = certificateRepository.save(certificate);

        String pdfUrl = pdfCertificateService.generateCertificatePdf(certificate);
        certificate.setPdfUrl(pdfUrl);

        certificate = certificateRepository.save(certificate);

        log.info(
                "Issued new certificate. code={}, studentId={}, courseId={}",
                certificate.getCertificateCode(),
                student.getId(),
                course.getId()
        );

        return certificate;
    }

    @Override
    @Transactional(readOnly = true)
    public Certificate getByCode(String code) {
        if (code == null || code.isBlank()) {
            throw new RuntimeException("Mã chứng chỉ không hợp lệ.");
        }

        return certificateRepository.findByCertificateCode(code.trim())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy chứng chỉ với mã: " + code));
    }

    @Override
    @Transactional
    public List<Certificate> getCertificatesByStudentId(Long studentId) {
        if (studentId == null) {
            throw new RuntimeException("Student ID không hợp lệ.");
        }

        List<Certificate> certificates = new ArrayList<>(
                certificateRepository.findByStudent_IdOrderByIssuedAtDesc(studentId)
        );

        Set<Long> certifiedCourseIds = new HashSet<>();
        for (Certificate cert : certificates) {
            if (cert.getCourse() != null && cert.getCourse().getId() != null) {
                certifiedCourseIds.add(cert.getCourse().getId());
            }
        }

        List<Enrollment> enrollments = enrollmentRepository.findByStudent_IdAndStatus(studentId, "ACTIVE");
        for (Enrollment enrollment : enrollments) {
            if (!isEligibleForBackfill(enrollment)) {
                continue;
            }

            Long courseId = enrollment.getCourse().getId();
            if (certifiedCourseIds.contains(courseId)) {
                continue;
            }

            try {
                Certificate generated = issueCertificate(enrollment);
                certificates.add(generated);
                certifiedCourseIds.add(courseId);
            } catch (Exception e) {
                log.warn(
                        "Backfill certificate failed for studentId={}, enrollmentId={}, reason={}",
                        studentId,
                        enrollment.getId(),
                        e.getMessage()
                );
            }
        }

        certificates.sort(
                Comparator.comparing(
                        Certificate::getIssuedAt,
                        Comparator.nullsLast(Comparator.reverseOrder())
                )
        );

        return certificates;
    }

    private String generateUniqueCertificateCode(Long studentId, Long courseId) {
        String code;
        int retry = 0;

        do {
            code = generateCertificateCode(studentId, courseId);
            retry++;

            if (retry > 20) {
                throw new RuntimeException("Không thể tạo mã chứng chỉ duy nhất.");
            }
        } while (certificateRepository.existsByCertificateCode(code));

        return code;
    }

    private String generateCertificateCode(Long studentId, Long courseId) {
        String datePart = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String randomPart = UUID.randomUUID()
                .toString()
                .replace("-", "")
                .substring(0, 8)
                .toUpperCase();

        return "EDU-" + courseId + "-" + studentId + "-" + datePart + "-" + randomPart;
    }

    private String buildVerifyUrl(String certificateCode) {
        return frontendBaseUrl + "/certificate/verify/" + certificateCode;
    }

    private LocalDateTime resolveCompletedAt(Enrollment enrollment) {
        if (enrollment.getCourseCompletedAt() != null) {
            return enrollment.getCourseCompletedAt();
        }
        return LocalDateTime.now();
    }

    private boolean isCompletionReached(Enrollment enrollment) {
        if (Boolean.TRUE.equals(enrollment.getIsCourseCompleted())) {
            return true;
        }

        BigDecimal progress = enrollment.getProgress();
        return progress != null && progress.compareTo(BigDecimal.valueOf(100)) >= 0;
    }

    private boolean isEligibleForBackfill(Enrollment enrollment) {
        return enrollment != null
                && enrollment.getCourse() != null
                && enrollment.getCourse().getId() != null
                && isCompletionReached(enrollment);
    }
}
