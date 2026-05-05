package org.example.multileanproject.dto;

import lombok.Data;

@Data
public class MarketInsightRequest {
    /** Chủ đề giảng viên muốn phân tích (có thể để trống để phân tích tổng quát) */
    private String topic;
}
