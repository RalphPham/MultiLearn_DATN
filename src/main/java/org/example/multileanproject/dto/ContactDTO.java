package org.example.multileanproject.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class ContactDTO {
    private Long userId;
    private String userName;
    private String lastMessageContent;
    private LocalDateTime lastMessageTime;
    private long unreadCount;
    private boolean online;
}