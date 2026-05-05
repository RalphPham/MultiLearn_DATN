package org.example.multileanproject.dto;

import java.math.BigDecimal;

public class RevenueChartPointDTO {

    private Integer year;
    private Integer month;
    private Double gross;
    private Double refund;
    private Double net;

    public RevenueChartPointDTO() {
    }

    public RevenueChartPointDTO(Integer year, Integer month, BigDecimal gross, BigDecimal refund, BigDecimal net) {
        this.year = year;
        this.month = month;
        this.gross = gross != null ? gross.doubleValue() : 0.0;
        this.refund = refund != null ? refund.doubleValue() : 0.0;
        this.net = net != null ? net.doubleValue() : 0.0;
    }

    // Legacy constructor (no year)
    public RevenueChartPointDTO(Integer month, BigDecimal gross, BigDecimal refund, BigDecimal net) {
        this.month = month;
        this.gross = gross != null ? gross.doubleValue() : 0.0;
        this.refund = refund != null ? refund.doubleValue() : 0.0;
        this.net = net != null ? net.doubleValue() : 0.0;
    }

    public Integer getYear() { return year; }
    public void setYear(Integer year) { this.year = year; }

    public Integer getMonth() {
        return month;
    }

    public void setMonth(Integer month) {
        this.month = month;
    }

    public Double getGross() {
        return gross;
    }

    public void setGross(Double gross) {
        this.gross = gross;
    }

    public Double getRefund() {
        return refund;
    }

    public void setRefund(Double refund) {
        this.refund = refund;
    }

    public Double getNet() {
        return net;
    }

    public void setNet(Double net) {
        this.net = net;
    }
}