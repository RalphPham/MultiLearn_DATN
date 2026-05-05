package org.example.multileanproject.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderHistoryResponseDTO {
    private Long id;
    private BigDecimal originalAmount;
    private BigDecimal finalAmount;
    private String couponCode;
    private String note;
    private String status;
    private String paymentMethod;
    private String transactionRef;
    private LocalDateTime createdAt;
    private List<OrderHistoryItemDTO> orderItems;
}