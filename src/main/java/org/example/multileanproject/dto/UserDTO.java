package org.example.multileanproject.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class UserDTO {
    private Long id;
    private String fullName;
    private String email;
    private String role;
    private String status; // 'ACTIVE' hoáº·c 'LOCKED'
    private String avatar;
    private LocalDateTime createdAt;
    private int courseCount;
    private int learningCourseCount;
    private int teachingCourseCount;
}
