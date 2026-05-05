package org.example.multileanproject.dto;

import java.math.BigDecimal;

public class CheckoutPreviewResponseDTO {

    private BigDecimal originalAmount;
    private BigDecimal couponEligibleAmount;
    private BigDecimal discountAmount;
    private BigDecimal finalAmount;
    private String couponCode;
    private Boolean valid;
    private String message;

    public CheckoutPreviewResponseDTO() {
    }

    public CheckoutPreviewResponseDTO(BigDecimal originalAmount,
                                      BigDecimal couponEligibleAmount,
                                      BigDecimal discountAmount,
                                      BigDecimal finalAmount,
                                      String couponCode,
                                      Boolean valid,
                                      String message) {
        this.originalAmount = originalAmount;
        this.couponEligibleAmount = couponEligibleAmount;
        this.discountAmount = discountAmount;
        this.finalAmount = finalAmount;
        this.couponCode = couponCode;
        this.valid = valid;
        this.message = message;
    }

    public BigDecimal getOriginalAmount() {
        return originalAmount;
    }

    public void setOriginalAmount(BigDecimal originalAmount) {
        this.originalAmount = originalAmount;
    }

    public BigDecimal getCouponEligibleAmount() {
        return couponEligibleAmount;
    }

    public void setCouponEligibleAmount(BigDecimal couponEligibleAmount) {
        this.couponEligibleAmount = couponEligibleAmount;
    }

    public BigDecimal getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(BigDecimal discountAmount) {
        this.discountAmount = discountAmount;
    }

    public BigDecimal getFinalAmount() {
        return finalAmount;
    }

    public void setFinalAmount(BigDecimal finalAmount) {
        this.finalAmount = finalAmount;
    }

    public String getCouponCode() {
        return couponCode;
    }

    public void setCouponCode(String couponCode) {
        this.couponCode = couponCode;
    }

    public Boolean getValid() {
        return valid;
    }

    public void setValid(Boolean valid) {
        this.valid = valid;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
