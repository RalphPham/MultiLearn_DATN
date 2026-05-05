package org.example.multileanproject.dto;

import lombok.Data;

@Data
public class ChatMessageRequest {
    private Long receiverId;
    private String content;
    private String attachmentUrl;
    private String attachmentType;
}
