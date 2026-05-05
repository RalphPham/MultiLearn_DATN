package org.example.multileanproject.dto;

import java.math.BigDecimal;

public record VnpayCreateRequest(
        Long orderId,
        BigDecimal amount,
        String bankCode
) {
}
