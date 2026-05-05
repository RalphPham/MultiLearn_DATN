package org.example.multileanproject.service;

import org.example.multileanproject.dto.RefundRequestDTO;

import java.util.List;
import java.util.Map;

public interface RefundService {

    void createRefundRequest(String email, RefundRequestDTO dto);

    List<Map<String, Object>> getAllRefundRequests();

    void approveRefund(Long refundId, String adminNote);

    void rejectRefund(Long refundId, String adminNote);
}
