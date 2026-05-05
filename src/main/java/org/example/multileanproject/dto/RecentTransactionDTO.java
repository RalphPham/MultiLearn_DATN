package org.example.multileanproject.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class RecentTransactionDTO {

    private String studentName;
    private String courseTitle;
    private Double amount;
    private LocalDateTime createdAt;

    public RecentTransactionDTO() {
    }

    public RecentTransactionDTO(String studentName,
                                String courseTitle,
                                Double amount,
                                LocalDateTime createdAt) {
        this.studentName = studentName;
        this.courseTitle = courseTitle;
        this.amount = amount;
        this.createdAt = createdAt;
    }

    // Constructor để Hibernate/JPQL map khi amount trả về BigDecimal
    public RecentTransactionDTO(String studentName,
                                String courseTitle,
                                BigDecimal amount,
                                LocalDateTime createdAt) {
        this.studentName = studentName;
        this.courseTitle = courseTitle;
        this.amount = amount != null ? amount.doubleValue() : 0.0;
        this.createdAt = createdAt;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getCourseTitle() {
        return courseTitle;
    }

    public void setCourseTitle(String courseTitle) {
        this.courseTitle = courseTitle;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}