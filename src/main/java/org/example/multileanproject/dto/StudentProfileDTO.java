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
public class StudentProfileDTO {
    private Long id;
    private String fullName;
    private String email;
    private String phone;
    private String avatar;
    private BigDecimal balance;
    private String role;
}