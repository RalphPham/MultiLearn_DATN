package org.example.multileanproject.dto;

import java.math.BigDecimal;

public class RevenueMetricsDTO {

    private Double grossRevenue;
    private Double refundAmount;
    private Double netRevenue;
    private Long studentsCount;
    private Long salesCount;

    public RevenueMetricsDTO() {
    }

    public RevenueMetricsDTO(Double grossRevenue,
                             Double refundAmount,
                             Double netRevenue,
                             Long studentsCount,
                             Long salesCount) {
        this.grossRevenue = grossRevenue;
        this.refundAmount = refundAmount;
        this.netRevenue = netRevenue;
        this.studentsCount = studentsCount;
        this.salesCount = salesCount;
    }

    // Constructor để JPQL/Hibernate map khi SUM trả về BigDecimal
    public RevenueMetricsDTO(BigDecimal grossRevenue,
                             BigDecimal refundAmount,
                             Double netRevenue,
                             Long studentsCount,
                             Long salesCount) {
        this.grossRevenue = grossRevenue != null ? grossRevenue.doubleValue() : 0.0;
        this.refundAmount = refundAmount != null ? refundAmount.doubleValue() : 0.0;
        this.netRevenue = netRevenue != null ? netRevenue : 0.0;
        this.studentsCount = studentsCount != null ? studentsCount : 0L;
        this.salesCount = salesCount != null ? salesCount : 0L;
    }

    // Constructor dự phòng nếu netRevenue cũng bị trả về BigDecimal
    public RevenueMetricsDTO(BigDecimal grossRevenue,
                             BigDecimal refundAmount,
                             BigDecimal netRevenue,
                             Long studentsCount,
                             Long salesCount) {
        this.grossRevenue = grossRevenue != null ? grossRevenue.doubleValue() : 0.0;
        this.refundAmount = refundAmount != null ? refundAmount.doubleValue() : 0.0;
        this.netRevenue = netRevenue != null ? netRevenue.doubleValue() : 0.0;
        this.studentsCount = studentsCount != null ? studentsCount : 0L;
        this.salesCount = salesCount != null ? salesCount : 0L;
    }

    // Constructor dự phòng khi COALESCE(SUM(null), 0) trả về Integer (không có dữ liệu cho năm được chọn)
    public RevenueMetricsDTO(Number grossRevenue,
                             Number refundAmount,
                             Number netRevenue,
                             Number studentsCount,
                             Number salesCount) {
        this.grossRevenue = grossRevenue != null ? grossRevenue.doubleValue() : 0.0;
        this.refundAmount = refundAmount != null ? refundAmount.doubleValue() : 0.0;
        this.netRevenue = netRevenue != null ? netRevenue.doubleValue() : 0.0;
        this.studentsCount = studentsCount != null ? studentsCount.longValue() : 0L;
        this.salesCount = salesCount != null ? salesCount.longValue() : 0L;
    }

    public Double getGrossRevenue() {
        return grossRevenue;
    }

    public void setGrossRevenue(Double grossRevenue) {
        this.grossRevenue = grossRevenue;
    }

    public Double getRefundAmount() {
        return refundAmount;
    }

    public void setRefundAmount(Double refundAmount) {
        this.refundAmount = refundAmount;
    }

    public Double getNetRevenue() {
        return netRevenue;
    }

    public void setNetRevenue(Double netRevenue) {
        this.netRevenue = netRevenue;
    }

    public Long getStudentsCount() {
        return studentsCount;
    }

    public void setStudentsCount(Long studentsCount) {
        this.studentsCount = studentsCount;
    }

    public Long getSalesCount() {
        return salesCount;
    }

    public void setSalesCount(Long salesCount) {
        this.salesCount = salesCount;
    }
}