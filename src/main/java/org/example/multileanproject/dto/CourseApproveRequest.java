package org.example.multileanproject.dto;

import java.math.BigDecimal;

public class CourseApproveRequest {
    private BigDecimal price;
    private BigDecimal salePrice;
    private BigDecimal commissionRate; // Tỉ lệ phần trăm, ví dụ 0.7 = 70%

    // Getters and Setters
    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public BigDecimal getSalePrice() {
        return salePrice;
    }

    public void setSalePrice(BigDecimal salePrice) {
        this.salePrice = salePrice;
    }

    public BigDecimal getCommissionRate() {
        return commissionRate;
    }

    public void setCommissionRate(BigDecimal commissionRate) {
        this.commissionRate = commissionRate;
    }
}
