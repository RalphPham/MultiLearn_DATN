package org.example.multileanproject.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "chat_messages")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class ChatMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long senderId;
    private Long receiverId;

    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String content;

    private LocalDateTime timestamp;
    private boolean isRead;

    @Column(name = "is_deleted", nullable = false, columnDefinition = "BIT NOT NULL DEFAULT 0")
    private boolean isDeleted;

    @Column(name = "attachment_url", length = 500)
    private String attachmentUrl;

    @Column(name = "attachment_type", length = 50)
    private String attachmentType;
}