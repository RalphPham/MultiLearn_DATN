package org.example.multileanproject.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** BE trả về Access Token mới sau khi /refresh thành công */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefreshTokenResponse {
    /** Access Token mới - ngắn hạn 30 phút */
    private String accessToken;

    /**
     * Refresh Token - trả lại chính cái cũ (không rotate)
     * Nếu muốn nâng cấp bảo mật sau này: Tao token mới + xoá token cũ (Token Rotation).
     */
    private String refreshToken;

    private String tokenType = "Bearer";
}
