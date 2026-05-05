package org.example.multileanproject.dto;

import lombok.*;
import org.example.multileanproject.entity.BlogPost;
import org.example.multileanproject.entity.BlogStatus;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BlogPostDTO {
    private Long id;
    private String title;
    private String content;
    private String excerpt;
    private String thumbnail;
    private String category;
    private String authorName;
    private LocalDateTime publishedAt;
    private String readTime;
    private BlogStatus status;
    private int views;
    private LocalDateTime createdAt;

    public static BlogPostDTO fromEntity(BlogPost post) {
        return BlogPostDTO.builder()
                .id(post.getId())
                .title(post.getTitle())
                .content(post.getContent())
                .excerpt(post.getExcerpt())
                .thumbnail(post.getThumbnail())
                .category(post.getCategory())
                .authorName(post.getAuthorName())
                .publishedAt(post.getPublishedAt())
                .readTime(post.getReadTime())
                .status(post.getStatus())
                .views(post.getViews())
                .createdAt(post.getCreatedAt())
                .build();
    }
}
