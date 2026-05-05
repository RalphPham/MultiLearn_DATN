package org.example.multileanproject.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WishlistResponse {
    private Long id;
    private Long courseId;
    private String title;
    private String slug; // 🔥 THÊM CÁI NÀY
    private String thumbnail;
    private BigDecimal price;
    private BigDecimal salePrice;
}