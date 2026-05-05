package org.example.multileanproject.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AdminAccountDTO {
    private Long id;
    private String username;
    private String password;
    private String fullName;
    private String adminRole;
    private Boolean isActive;
    private List<String> permissions;
    private LocalDateTime createdAt;
}