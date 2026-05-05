package org.example.multileanproject.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChartDTO {
    private String label;  // Ngày (VD: 15/02)
    private BigDecimal value; // Tổng doanh thu (VD: 5000000)
    private BigDecimal platformValue;   // Phần nền tảng giữ lại
    private BigDecimal instructorValue; // Phần chia cho giảng viên

    public ChartDTO(String label, BigDecimal value) {
        this.label = label;
        this.value = value;
    }
}