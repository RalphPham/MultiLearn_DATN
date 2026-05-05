package org.example.multileanproject.dto;

import lombok.Data;
import org.example.multileanproject.entity.Course;

import java.math.BigDecimal;

@Data
public class CartItemDTO {
    private Long id;
    private Course course; // Trả về full object Course để Frontend lấy ảnh, tên, giá
    private BigDecimal price;
}