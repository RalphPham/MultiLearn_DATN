package org.example.multileanproject.dto;

import lombok.Data;

/** FE gửi lên khi muốn lấy Access Token mới */
@Data
public class RefreshTokenRequest {
    private String refreshToken;
}
