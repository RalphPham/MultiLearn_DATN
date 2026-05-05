package org.example.multileanproject.repository;

import org.example.multileanproject.entity.Certificate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CertificateRepository extends JpaRepository<Certificate, Long> {

    Optional<Certificate> findByCertificateCode(String certificateCode);

    Optional<Certificate> findByStudent_IdAndCourse_Id(Long studentId, Long courseId);

    Optional<Certificate> findByEnrollment_Id(Long enrollmentId);

    List<Certificate> findByStudent_IdOrderByIssuedAtDesc(Long studentId);

    boolean existsByCertificateCode(String certificateCode);
}