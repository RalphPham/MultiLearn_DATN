package org.example.multileanproject.dto;

import lombok.Data;

@Data
public class BlogGenerateRequest {
    /**
     * Loại bài viết:
     * SPOTLIGHT  - Spotlight một khóa học cụ thể (cần courseId)
     * TIPS       - Mẹo học tập (cần topic)
     * TRENDING   - Xu hướng khóa học nổi bật
     * WEEKLY     - Tổng kết tuần
     */
    private String type;
    private Long courseId;  // dùng cho SPOTLIGHT
    private String topic;   // gợi ý chủ đề cho TIPS
}
