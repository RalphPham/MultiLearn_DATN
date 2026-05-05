package org.example.multileanproject.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponse {
    private String token;
    private String refreshToken;
    private String role;
    private Long userId;
    private String fullName;
    private String email;
    private String phone;
    private String adminRole;
    private List<String> permissions;
}