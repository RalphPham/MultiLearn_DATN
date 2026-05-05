package org.example.multileanproject.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ChatMessageDTO {
    private Long id;
    private Long senderId;
    private String senderRole;
    private String content;
    private LocalDateTime createdAt;
    private boolean deleted;
    private String attachmentUrl;
    private String attachmentType;
}
