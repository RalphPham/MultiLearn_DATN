package org.example.multileanproject.dto;

import java.util.List;

public class InstructorRevenueSummaryDTO {

    private RevenueMetricsDTO metrics;
    private List<RevenueChartPointDTO> chart;
    private List<TopCourseRevenueDTO> topCourses;
    private List<RecentTransactionDTO> transactions;

    public InstructorRevenueSummaryDTO() {
    }

    public InstructorRevenueSummaryDTO(
            RevenueMetricsDTO metrics,
            List<RevenueChartPointDTO> chart,
            List<TopCourseRevenueDTO> topCourses,
            List<RecentTransactionDTO> transactions) {
        this.metrics = metrics;
        this.chart = chart;
        this.topCourses = topCourses;
        this.transactions = transactions;
    }

    public RevenueMetricsDTO getMetrics() {
        return metrics;
    }

    public void setMetrics(RevenueMetricsDTO metrics) {
        this.metrics = metrics;
    }

    public List<RevenueChartPointDTO> getChart() {
        return chart;
    }

    public void setChart(List<RevenueChartPointDTO> chart) {
        this.chart = chart;
    }

    public List<TopCourseRevenueDTO> getTopCourses() {
        return topCourses;
    }

    public void setTopCourses(List<TopCourseRevenueDTO> topCourses) {
        this.topCourses = topCourses;
    }

    public List<RecentTransactionDTO> getTransactions() {
        return transactions;
    }

    public void setTransactions(List<RecentTransactionDTO> transactions) {
        this.transactions = transactions;
    }
}