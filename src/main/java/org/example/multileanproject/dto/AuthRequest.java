package org.example.multileanproject.dto;
import lombok.Data;

@Data
public class AuthRequest {
    private String email; // Hoặc username tùy DB của bạn
    private String password;
}