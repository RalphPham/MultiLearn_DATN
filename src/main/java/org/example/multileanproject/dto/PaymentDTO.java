package org.example.multileanproject.dto;
import lombok.Builder;
import lombok.Data;

public class PaymentDTO {
    @Data
    @Builder
    public static class VNPayResponse {
        public String status;
        public String message;
        public String paymentUrl;
    }
}