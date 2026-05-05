package org.example.multileanproject.service;

import org.example.multileanproject.entity.Certificate;

public interface PdfCertificateService {

    String generateCertificatePdf(Certificate certificate);

}