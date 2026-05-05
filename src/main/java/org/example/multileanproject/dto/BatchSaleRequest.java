package org.example.multileanproject.dto;

import java.math.BigDecimal;
import java.util.List;

public class BatchSaleRequest {
    private List<Long> courseIds;
    private String saleType; // "PERCENT" (phần trăm) hoặc "FIXED" (giá cố định)
    private BigDecimal saleValue;

    public List<Long> getCourseIds() {
        return courseIds;
    }

    public void setCourseIds(List<Long> courseIds) {
        this.courseIds = courseIds;
    }

    public String getSaleType() {
        return saleType;
    }

    public void setSaleType(String saleType) {
        this.saleType = saleType;
    }

    public BigDecimal getSaleValue() {
        return saleValue;
    }

    public void setSaleValue(BigDecimal saleValue) {
        this.saleValue = saleValue;
    }
}