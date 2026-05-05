package org.example.multileanproject.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "messages")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Message {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long senderId;
    private Long receiverId;

    @Column(columnDefinition = "NVARCHAR(MAX)") // Để lưu tiếng Việt thoải mái
    private String content;

    private String senderRole; // "STUDENT" hoặc "INSTRUCTOR"
    private LocalDateTime createdAt = LocalDateTime.now();
}