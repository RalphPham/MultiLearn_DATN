package org.example.multileanproject.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Builder
public class DashboardStatsDTO {
    private long totalStudents;
    private long totalCourses;
    private BigDecimal totalRevenue;
    private BigDecimal platformRevenue;
    private BigDecimal instructorPayout;
    private long pendingCourses;
    private long newOrders;
    private long newStudentsThisMonth;
    private long completedOrders;
    private long cancelledOrders;
    private long refundedOrders;
    private double completionRate;
}
