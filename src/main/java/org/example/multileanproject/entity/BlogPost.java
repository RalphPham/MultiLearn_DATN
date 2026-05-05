package org.example.multileanproject.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "blog_posts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class BlogPost {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "NVARCHAR(500)", nullable = false)
    private String title;

    @Column(columnDefinition = "NVARCHAR(MAX)", nullable = false)
    private String content;

    @Column(columnDefinition = "NVARCHAR(500)")
    private String excerpt;

    @Column(columnDefinition = "NVARCHAR(500)")
    private String thumbnail;

    @Column(columnDefinition = "NVARCHAR(100)")
    private String category;

    @Column(name = "author_name", columnDefinition = "NVARCHAR(200)")
    @Builder.Default
    private String authorName = "MultiLearn Editorial";

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    @Column(name = "read_time", columnDefinition = "NVARCHAR(50)")
    @Builder.Default
    private String readTime = "3 phút đọc";

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    @Builder.Default
    private BlogStatus status = BlogStatus.DRAFT;

    @Builder.Default
    private int views = 0;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
