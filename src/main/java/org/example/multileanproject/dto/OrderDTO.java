package org.example.multileanproject.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderDTO {
    private Long id;
    private String studentName;
    private String studentEmail;
    private List<String> courseNames;
    private BigDecimal originalAmount;
    private BigDecimal discountAmount;
    private BigDecimal finalAmount;
    private String couponCode;
    private String status;      // PENDING, COMPLETED, CANCELLED
    private String paymentMethod;
    private LocalDateTime createdAt;
}