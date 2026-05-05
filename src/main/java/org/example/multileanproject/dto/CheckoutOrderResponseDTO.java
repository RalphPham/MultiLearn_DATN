package org.example.multileanproject.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.multileanproject.entity.Order;
import org.example.multileanproject.entity.OrderStatus;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CheckoutOrderResponseDTO {
    private Long id;
    private BigDecimal originalAmount;
    private BigDecimal finalAmount;
    private String couponCode;
    private OrderStatus status;
    private String note;

    public static CheckoutOrderResponseDTO fromEntity(Order order) {
        return new CheckoutOrderResponseDTO(
                order.getId(),
                order.getOriginalAmount(),
                order.getFinalAmount(),
                order.getCouponCode(),
                order.getStatus(),
                order.getNote()
        );
    }
}