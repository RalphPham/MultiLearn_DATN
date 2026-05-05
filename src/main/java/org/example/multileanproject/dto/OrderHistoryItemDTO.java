package org.example.multileanproject.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderHistoryItemDTO {
    private Long id;
    private Long courseId;
    private String title;
    private String thumbnail;
    private String slug;
    private BigDecimal price;
}