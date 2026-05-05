package org.example.multileanproject.service;

import lombok.RequiredArgsConstructor;
import org.example.multileanproject.entity.RefreshToken;
import org.example.multileanproject.repository.RefreshTokenRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Quản lý toàn bộ vòng đời của Refresh Token:
 *   - Tạo mới khi login
 *   - Xác minh khi /refresh được gọi
 *   - Xóa khi /logout-all
 */
@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    /** Refresh Token sống 7 ngày */
    private static final long REFRESH_TOKEN_EXPIRY_DAYS = 7;

    private final RefreshTokenRepository refreshTokenRepository;

    /**
     * Tạo Refresh Token mới cho user và lưu vào DB.
     * Gọi hàm này mỗi lần user đăng nhập thành công.
     *
     * @param userId   ID của user (trong bảng students hoặc admins)
     * @param userType "STUDENT" | "ADMIN" | "INSTRUCTOR"
     */
    public RefreshToken createRefreshToken(Long userId, String userType) {
        RefreshToken token = RefreshToken.builder()
                .token(UUID.randomUUID().toString())
                .userId(userId)
                .userType(userType)
                .expiresAt(LocalDateTime.now().plusDays(REFRESH_TOKEN_EXPIRY_DAYS))
                .createdAt(LocalDateTime.now())
                .build();
        return refreshTokenRepository.save(token);
    }

    /**
     * Xác minh Refresh Token gửi lên từ FE.
     * Ném RuntimeException nếu không hợp lệ hoặc đã hết hạn.
     *
     * @param tokenStr chuỗi token FE gửi lên
     * @return RefreshToken entity đã được xác minh
     */
    public RefreshToken verifyRefreshToken(String tokenStr) {
        RefreshToken token = refreshTokenRepository.findByToken(tokenStr)
                .orElseThrow(() -> new RuntimeException("Refresh Token không hợp lệ hoặc đã bị thu hồi."));

        if (token.isExpired()) {
            // Tự dọn token hết hạn khỏi DB luôn
            refreshTokenRepository.delete(token);
            throw new RuntimeException("Refresh Token đã hết hạn. Vui lòng đăng nhập lại.");
        }

        return token;
    }

    /**
     * Xóa toàn bộ Refresh Token của user.
     * Dùng cho /logout-all → ép văng tất cả thiết bị đang đăng nhập.
     *
     * @param userId ID của user cần thu hồi toàn bộ phiên
     */
    @Transactional
    public void revokeAllTokens(Long userId, String userType) {
        refreshTokenRepository.deleteAllByUserIdAndUserType(userId, userType);
    }

    /**
     * Đếm số phiên đang active của user.
     * Dùng để hiển thị "Bạn đang đăng nhập trên X thiết bị".
     */
    public int countActiveSessions(Long userId) {
        return refreshTokenRepository.countByUserId(userId);
    }
}