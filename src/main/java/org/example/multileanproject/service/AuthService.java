package org.example.multileanproject.service;

import org.example.multileanproject.dto.AuthResponse;
import org.example.multileanproject.dto.LoginRequest;
import org.example.multileanproject.dto.RegisterRequest;

public interface AuthService {
    // 1. Đăng ký
    AuthResponse register(RegisterRequest request);

    // 2. Đăng nhập
    AuthResponse login(LoginRequest request);

    // 3. Quên mật khẩu
    void forgotPassword(String email);

    // 4. Xác thực OTP
    boolean verifyOtp(String email, String otp);

    // 5. Đặt lại mật khẩu
    void resetPassword(String email, String newPassword, String otp);

    // --- MỚI: HÀM KIỂM TRA TRÙNG (Cho API check nhanh) ---
    void checkExistence(String email, String phone);

    // --- Đăng nhập bằng Google ---
    AuthResponse loginWithGoogle(String idToken);
}