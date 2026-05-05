package org.example.multileanproject.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class WithdrawRequestDTO {

    @NotNull(message = "So tien khong duoc de trong")
    @DecimalMin(value = "10000", message = "So tien rut toi thieu la 10,000 VND")
    private BigDecimal amount;

    @NotBlank(message = "Ten ngan hang khong duoc de trong")
    private String bankName;

    @NotBlank(message = "So tai khoan khong duoc de trong")
    private String bankAccountNumber;

    @NotBlank(message = "Ten chu tai khoan khong duoc de trong")
    private String accountName;

    /**
     * Optional note from instructor. Current DB schema does not persist this yet.
     */
    private String note;
}

