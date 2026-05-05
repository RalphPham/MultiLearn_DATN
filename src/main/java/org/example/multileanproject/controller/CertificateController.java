package org.example.multileanproject.controller;

import lombok.RequiredArgsConstructor;
import org.example.multileanproject.entity.Certificate;
import org.example.multileanproject.repository.StudentRepository;
import org.example.multileanproject.service.CertificateService;
import org.example.multileanproject.service.PdfCertificateService;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/certificates")
@RequiredArgsConstructor
public class CertificateController {

    private final CertificateService certificateService;
    private final PdfCertificateService pdfCertificateService;
    private final StudentRepository studentRepository;

    private Long getCurrentStudentId(Authentication authentication) {
        if (authentication == null || authentication.getName() == null || "anonymousUser".equals(authentication.getName())) {
            throw new RuntimeException("Ban chua dang nhap.");
        }
        return studentRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Khong tim thay hoc vien."))
                .getId();
    }

    @GetMapping("/my-certificates")
    @Transactional(readOnly = true)
    public ResponseEntity<?> getMyCertificates(Authentication authentication) {
        Long studentId = getCurrentStudentId(authentication);
        List<Certificate> certificates = certificateService.getCertificatesByStudentId(studentId);

        List<Map<String, Object>> response = certificates.stream().map(certificate -> {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", certificate.getId());
            item.put("courseId", certificate.getCourse() != null ? certificate.getCourse().getId() : null);
            item.put("courseName", certificate.getCourse() != null ? certificate.getCourse().getTitle() : null);
            item.put("issueDate", certificate.getIssuedAt());
            item.put("verifyCode", certificate.getCertificateCode());
            return item;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/download/{courseId}")
    @Transactional(readOnly = true)
    public ResponseEntity<Resource> downloadCertificate(
            @PathVariable Long courseId,
            Authentication authentication
    ) {
        Long studentId = getCurrentStudentId(authentication);
        List<Certificate> certificates = certificateService.getCertificatesByStudentId(studentId);

        Certificate myCertificate = certificates.stream()
                .filter(certificate -> certificate.getCourse() != null && courseId.equals(certificate.getCourse().getId()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Ban khong so huu chung chi cua khoa hoc nay."));

        // Always regenerate on download so existing certificates immediately use the newest template.
        pdfCertificateService.generateCertificatePdf(myCertificate);

        String fileName = myCertificate.getCertificateCode() + ".pdf";
        Path filePath = Paths.get("generated-certificates").resolve(fileName).normalize();

        try {
            Resource resource = new UrlResource(filePath.toUri());
            if (!resource.exists() || !resource.isReadable()) {
                throw new RuntimeException("Khong tim thay file PDF chung chi.");
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_PDF)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                    .body(resource);
        } catch (MalformedURLException e) {
            throw new RuntimeException("Khong doc duoc file chung chi.", e);
        }
    }

    @GetMapping("/verify/{code}")
    @Transactional(readOnly = true)
    public ResponseEntity<?> verifyCertificate(@PathVariable String code) {
        Certificate certificate = certificateService.getByCode(code);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("id", certificate.getId());
        response.put("certificateCode", certificate.getCertificateCode());
        response.put("issuedAt", certificate.getIssuedAt());
        response.put("completedAt", certificate.getCompletedAt());
        response.put("verifyUrl", certificate.getVerifyUrl());
        response.put("active", certificate.getIsActive());

        if (certificate.getStudent() != null) {
            response.put("studentName", certificate.getStudent().getFullName());
        }

        if (certificate.getCourse() != null) {
            response.put("courseTitle", certificate.getCourse().getTitle());
            response.put("courseId", certificate.getCourse().getId());
            response.put("thumbnail", certificate.getCourse().getThumbnail());
        }

        return ResponseEntity.ok(response);
    }
}
