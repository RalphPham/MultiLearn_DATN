package org.example.multileanproject.service;

import org.example.multileanproject.entity.Certificate;
import org.example.multileanproject.entity.Enrollment;

import java.util.List;

public interface CertificateService {

    Certificate issueCertificate(Enrollment enrollment);

    Certificate getByCode(String code);

    List<Certificate> getCertificatesByStudentId(Long studentId);
}