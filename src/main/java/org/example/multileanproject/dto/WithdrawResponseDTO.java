package org.example.multileanproject.dto;

import lombok.Builder;
import lombok.Data;
import org.example.multileanproject.entity.WithdrawRequest;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class WithdrawResponseDTO {
    private Long id;
    private BigDecimal amount;
    private String bankName;
    private String bankAccountNumber;
    private String accountName;
    private String note;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Admin-side info
    private Long instructorId;
    private String instructorName;
    private String instructorEmail;
    private BigDecimal instructorWalletBalance;

    public static WithdrawResponseDTO fromEntity(WithdrawRequest entity) {
        ParsedBankInfo parsed = parseBankInfo(entity.getBankInfo());
        return WithdrawResponseDTO.builder()
                .id(entity.getId())
                .amount(entity.getAmount())
                .bankName(parsed.bankName())
                .bankAccountNumber(parsed.bankAccountNumber())
                .accountName(parsed.accountName())
                .note(null) // DB schema currently has no note column.
                .status(entity.getStatus())
                .createdAt(entity.getCreatedAt())
                .updatedAt(null) // DB schema currently has no updated_at column.
                .instructorId(entity.getInstructor() != null ? entity.getInstructor().getId() : null)
                .instructorName(entity.getInstructor() != null ? entity.getInstructor().getFullName() : null)
                .instructorEmail(entity.getInstructor() != null && entity.getInstructor().getUser() != null
                        ? entity.getInstructor().getUser().getEmail()
                        : null)
                .instructorWalletBalance(entity.getInstructor() != null
                        ? entity.getInstructor().getWalletBalance()
                        : BigDecimal.ZERO)
                .build();
    }

    private static ParsedBankInfo parseBankInfo(String bankInfo) {
        if (bankInfo == null || bankInfo.isBlank()) {
            return new ParsedBankInfo("", "", "");
        }

        String[] parts = bankInfo.split("\\|");
        if (parts.length >= 3) {
            return new ParsedBankInfo(
                    parts[0].trim(),
                    parts[1].trim(),
                    parts[2].trim()
            );
        }

        return new ParsedBankInfo(bankInfo.trim(), "", "");
    }

    private record ParsedBankInfo(String bankName, String bankAccountNumber, String accountName) {
    }
}

