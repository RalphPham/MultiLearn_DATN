package org.example.multileanproject.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CartDTO {
    // Chúng ta không trả về entity CartItem trực tiếp để tránh lộ cấu trúc DB
    // Nên tạo thêm CartItemDTO, nhưng tạm thời dùng CartItem nếu bạn chưa tạo DTO con
    private Long id;
    private List<CartItemDTO> items;
    private BigDecimal totalPrice;
    private BigDecimal discountAmount;
    private BigDecimal finalTotal;
}