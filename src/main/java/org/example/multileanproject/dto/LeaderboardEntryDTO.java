package org.example.multileanproject.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeaderboardEntryDTO {
    private Integer rank;
    private Long studentId;
    private String displayName;
    private String avatar;
    private Double progress;
    private Boolean isCourseCompleted;
    private LocalDateTime courseCompletedAt;
    /** true nếu đây là bản thân người đang xem */
    private Boolean isMe;
}
