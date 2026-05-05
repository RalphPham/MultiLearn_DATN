package org.example.multileanproject.service;

import lombok.RequiredArgsConstructor;
import org.example.multileanproject.dto.WithdrawActionDTO;
import org.example.multileanproject.dto.WithdrawRequestDTO;
import org.example.multileanproject.dto.WithdrawResponseDTO;
import org.example.multileanproject.entity.Instructor;
import org.example.multileanproject.entity.WithdrawRequest;
import org.example.multileanproject.repository.InstructorRepository;
import org.example.multileanproject.repository.OrderRepository;
import org.example.multileanproject.repository.WithdrawRequestRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class WithdrawServiceImpl implements WithdrawService {

    private static final BigDecimal MIN_WITHDRAW_AMOUNT = BigDecimal.valueOf(10000);

    private final WithdrawRequestRepository withdrawRequestRepository;
    private final InstructorRepository instructorRepository;
    private final OrderRepository orderRepository;

    @Override
    @Transactional
    public WithdrawResponseDTO createRequest(String instructorEmail, WithdrawRequestDTO request) {
        Instructor instructor = findInstructorByEmail(instructorEmail);

        BigDecimal amount = request.getAmount() == null ? BigDecimal.ZERO : request.getAmount();
        if (amount.compareTo(MIN_WITHDRAW_AMOUNT) < 0) {
            throw new RuntimeException("So tien rut toi thieu la 10,000 VND.");
        }

        BigDecimal currentBalance = computeRealBalance(instructor);
        if (currentBalance.compareTo(amount) < 0) {
            throw new RuntimeException("So du vi khong du de tao yeu cau rut tien.");
        }

        boolean hasPending = withdrawRequestRepository.existsByInstructor_IdAndStatus(instructor.getId(), "PENDING");
        if (hasPending) {
            throw new RuntimeException("Ban dang co yeu cau rut tien cho xu ly. Vui long doi admin xu ly truoc.");
        }

        String bankInfo = String.format(
                "%s | %s | %s",
                safeTrim(request.getBankName()),
                safeTrim(request.getBankAccountNumber()),
                safeTrim(request.getAccountName())
        );

        WithdrawRequest saved = withdrawRequestRepository.save(
                WithdrawRequest.builder()
                        .instructor(instructor)
                        .amount(amount)
                        .bankInfo(bankInfo)
                        .status("PENDING")
                        .build()
        );

        return WithdrawResponseDTO.fromEntity(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<WithdrawResponseDTO> getMyHistory(String instructorEmail) {
        Instructor instructor = findInstructorByEmail(instructorEmail);
        return withdrawRequestRepository.findByInstructor_IdOrderByCreatedAtDesc(instructor.getId())
                .stream()
                .map(WithdrawResponseDTO::fromEntity)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getWalletBalance(String instructorEmail) {
        Instructor instructor = findInstructorByEmail(instructorEmail);
        return computeRealBalance(instructor);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<WithdrawResponseDTO> getAllRequests(String status, Pageable pageable) {
        String statusFilter = normalizeStatusFilter(status);
        return withdrawRequestRepository
                .findAllByStatusFilter(statusFilter, pageable)
                .map(entity -> {
                    WithdrawResponseDTO dto = WithdrawResponseDTO.fromEntity(entity);
                    if (entity.getInstructor() != null) {
                        dto.setInstructorWalletBalance(computeRealBalance(entity.getInstructor()));
                    }
                    return dto;
                });
    }

    @Override
    @Transactional
    public WithdrawResponseDTO processRequest(Long requestId, WithdrawActionDTO action) {
        WithdrawRequest request = withdrawRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Khong tim thay yeu cau rut tien #" + requestId));

        if (!"PENDING".equalsIgnoreCase(request.getStatus())) {
            throw new RuntimeException("Yeu cau nay da duoc xu ly.");
        }

        String normalizedAction = action != null ? normalizeAction(action.getAction()) : "";
        if ("APPROVED".equals(normalizedAction)) {
            Instructor instructor = request.getInstructor();
            BigDecimal currentBalance = computeRealBalance(instructor);
            if (currentBalance.compareTo(request.getAmount()) < 0) {
                throw new RuntimeException("So du vi hien tai khong du de duyet yeu cau nay.");
            }
            // Không cần trừ stored walletBalance vì balance được tính động từ orders - approved_withdrawals
            request.setStatus("APPROVED");
        } else if ("REJECTED".equals(normalizedAction)) {
            request.setStatus("REJECTED");
        } else {
            throw new RuntimeException("Hanh dong khong hop le. Chi chap nhan APPROVED hoac REJECTED.");
        }

        WithdrawRequest updated = withdrawRequestRepository.save(request);
        return WithdrawResponseDTO.fromEntity(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public long countPendingRequests() {
        return withdrawRequestRepository.countByStatus("PENDING");
    }

    private Instructor findInstructorByEmail(String email) {
        return instructorRepository.findByUser_Email(email)
                .orElseThrow(() -> new RuntimeException("Khong tim thay thong tin giang vien."));
    }

    /** Tính số dư thực = net earnings từ orders - tổng đã rút (APPROVED) */
    private BigDecimal computeRealBalance(Instructor instructor) {
        BigDecimal netEarnings = orderRepository.calculateTotalNetEarnings(instructor.getUser().getEmail());
        if (netEarnings == null) netEarnings = BigDecimal.ZERO;

        BigDecimal withdrawn = withdrawRequestRepository.sumApprovedWithdrawsByInstructorId(instructor.getId());
        if (withdrawn == null) withdrawn = BigDecimal.ZERO;

        return netEarnings.subtract(withdrawn).max(BigDecimal.ZERO);
    }

    private BigDecimal safeBalance(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private String normalizeStatusFilter(String status) {
        if (status == null || status.isBlank()) return null;
        String normalized = status.trim().toUpperCase(Locale.ROOT);
        return "ALL".equals(normalized) ? null : normalized;
    }

    private String normalizeAction(String action) {
        return action == null ? "" : action.trim().toUpperCase(Locale.ROOT);
    }

    private String safeTrim(String value) {
        return value == null ? "" : value.trim();
    }
}

