package org.example.multileanproject.dto;

import lombok.Data;

@Data
public class WithdrawActionDTO {
    /**
     * APPROVED or REJECTED
     */
    private String action;

    /**
     * Optional admin note. Current DB schema does not persist this yet.
     */
    private String note;
}

