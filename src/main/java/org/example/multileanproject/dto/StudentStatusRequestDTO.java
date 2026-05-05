package org.example.multileanproject.dto;

import lombok.Data;

@Data
public class StudentStatusRequestDTO {
    private boolean active;
    private String reason;
    private String adminNote;
    private boolean sendWarningEmail;
    private String source;
}
