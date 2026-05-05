package org.example.multileanproject.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RatingTrendDTO {

    private Double overallAvg;
    private Long totalRatings;

    /** "UP" | "DOWN" | "STABLE" — so sánh 3 tháng gần nhất vs 3 tháng trước */
    private String trendDirection;

    /** Tối đa 6 tháng gần nhất */
    private List<MonthlyPoint> monthlyTrend;

    /** {1: count, 2: count, 3: count, 4: count, 5: count} */
    private Map<Integer, Long> starDistribution;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MonthlyPoint {
        private String label;    // "01/2025"
        private Double avgRating;
        private Long count;
    }
}
