package org.example.multileanproject.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class SaleCampaignRequest {
    private String name;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private boolean isActive;
    private boolean allowCouponStacking;
    private List<CampaignItemRequest> items;

    @Data
    public static class CampaignItemRequest {
        private Long courseId;
        private BigDecimal promotionalPrice;
        private int totalSlots;
    }
}
