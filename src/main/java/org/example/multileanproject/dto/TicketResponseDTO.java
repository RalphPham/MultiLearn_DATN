package org.example.multileanproject.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class TicketResponseDTO {
    private Long id;
    private String title;
    private String content;
    private String adminReply;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private StudentInfo student;

    @Data
    @Builder
    public static class StudentInfo {
        private Long id;
        private String fullName;
        private String email;
    }
}
