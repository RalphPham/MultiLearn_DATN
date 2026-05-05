package org.example.multileanproject.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class BadgeDTO {
    private String key;          // FIRST_COURSE, STREAK_7, ...
    private String name;
    private String description;
    private String icon;         // emoji
    private String category;     // COURSE, STREAK, SPECIAL
    private Boolean isEarned;
    private LocalDateTime earnedAt; // null nếu chưa đạt
}
