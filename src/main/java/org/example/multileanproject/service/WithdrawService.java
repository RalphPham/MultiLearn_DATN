package org.example.multileanproject.service;

import org.example.multileanproject.dto.WithdrawActionDTO;
import org.example.multileanproject.dto.WithdrawRequestDTO;
import org.example.multileanproject.dto.WithdrawResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;

public interface WithdrawService {
    WithdrawResponseDTO createRequest(String instructorEmail, WithdrawRequestDTO request);

    List<WithdrawResponseDTO> getMyHistory(String instructorEmail);

    BigDecimal getWalletBalance(String instructorEmail);

    Page<WithdrawResponseDTO> getAllRequests(String status, Pageable pageable);

    WithdrawResponseDTO processRequest(Long requestId, WithdrawActionDTO action);

    long countPendingRequests();
}

