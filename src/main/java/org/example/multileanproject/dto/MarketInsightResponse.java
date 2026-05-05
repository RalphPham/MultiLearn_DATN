package org.example.multileanproject.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class MarketInsightResponse {

    /** Các chủ đề đang trending */
    private String trendingTopics;

    /** Phân tích nhu cầu thị trường */
    private String demandAnalysis;

    /** Cơ hội cho giảng viên */
    private String opportunities;

    /** Lời khuyên cụ thể */
    private String recommendations;

    /** Top khóa học hot (lấy từ DB thực tế) */
    private List<HotCourse> hotCourses;

    @Data
    @Builder
    public static class HotCourse {
        private String title;
        private String category;
        private Integer students;
        private Double rating;
    }
}
