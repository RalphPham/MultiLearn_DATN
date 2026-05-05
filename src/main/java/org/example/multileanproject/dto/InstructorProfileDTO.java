package org.example.multileanproject.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InstructorProfileDTO {
    private Long id;
    private String fullName;
    private String email;
    private String phone;
    private String bio;
    private String avatarUrl;
    private String bankName;
    private String bankAccount;
    private BigDecimal walletBalance;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateRequest {
        private String fullName;
        private String phone;
        private String bio;
        private String avatarUrl;
        private String bankName;
        private String bankAccount;
    }
}
