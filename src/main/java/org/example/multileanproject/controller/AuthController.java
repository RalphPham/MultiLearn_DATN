package org.example.multileanproject.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.multileanproject.dto.*;
import org.example.multileanproject.entity.Admin;
import org.example.multileanproject.entity.AdminPermission;
import org.example.multileanproject.entity.AdminRole;
import org.example.multileanproject.entity.RefreshToken;
import org.example.multileanproject.entity.Student;
import org.example.multileanproject.repository.AdminRepository;
import org.example.multileanproject.repository.StudentRepository;
import org.example.multileanproject.dto.TicketRequestDTO;
import org.example.multileanproject.service.AuthService;
import org.example.multileanproject.service.RefreshTokenService;
import org.example.multileanproject.service.SupportTicketService;
import org.example.multileanproject.util.JwtUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService          authService;
    private final RefreshTokenService  refreshTokenService;
    private final JwtUtil              jwtUtil;
    private final StudentRepository    studentRepository;
    private final AdminRepository      adminRepository;
    private final SupportTicketService ticketService;

    // ── ĐĂNG KÝ ──────────────────────────────────────────────────────────
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody @Valid RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    // ── ĐĂNG NHẬP ─────────────────────────────────────────────────────────
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            return ResponseEntity.ok(authService.login(request));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ── [MỚI] CẤP LẠI ACCESS TOKEN ───────────────────────────────────────
    /**
     * POST /api/auth/refresh
     *
     * FE gọi khi Access Token hết hạn (nhận được 401).
     * Gửi lên Refresh Token cũ → nhận về Access Token mới.
     *
     * Request body:
     *   { "refreshToken": "uuid-string-7-ngay" }
     *
     * Response 200:
     *   { "accessToken": "eyJ...", "refreshToken": "uuid...", "tokenType": "Bearer" }
     *
     * Response 400 nếu token không hợp lệ hoặc hết hạn.
     */
    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@RequestBody RefreshTokenRequest request) {
        try {
            // 1. Xác minh Refresh Token (ném exception nếu sai/hết hạn)
            RefreshToken refreshToken = refreshTokenService.verifyRefreshToken(request.getRefreshToken());

            // 2. Tìm thông tin user để đưa vào claims
            Map<String, Object> claims = new HashMap<>();
            String username;

            if ("ADMIN".equals(refreshToken.getUserType())) {
                Admin admin = adminRepository.findById(refreshToken.getUserId())
                        .orElseThrow(() -> new RuntimeException("Admin không tồn tại"));
                claims.put("role", "ADMIN");
                String adminRole = admin.getAdminRole() != null
                        ? admin.getAdminRole().name()
                        : AdminRole.SUPER_ADMIN.name();
                claims.put("adminRole", adminRole);
                claims.put("permissions", AdminPermission.toNames(resolveAdminPermissions(admin)));
                username = admin.getUsername();
            } else {
                Student student = studentRepository.findById(refreshToken.getUserId())
                        .orElseThrow(() -> new RuntimeException("User không tồn tại"));
                claims.put("role", student.getRole().name());
                username = student.getEmail();
            }

            // 3. Cấp Access Token mới 30 phút
            String newAccessToken = jwtUtil.generateAccessToken(claims, username);

            return ResponseEntity.ok(RefreshTokenResponse.builder()
                    .accessToken(newAccessToken)
                    .refreshToken(refreshToken.getToken()) // Trả lại token cũ
                    .tokenType("Bearer")
                    .build());

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    // ── [MỚI] ĐĂNG XUẤT TẤT CẢ THIẾT BỊ ─────────────────────────────────
    /**
     * POST /api/auth/logout-all
     *
     * Thu hồi toàn bộ Refresh Token của user hiện tại.
     * Sau khi gọi: user bị văng ra khỏi mọi thiết bị khi Access Token hiện tại hết hạn (≤30 phút).
     *
     * Yêu cầu: Access Token hợp lệ trong header Authorization.
     *
     * Response 200:
     *   { "message": "Đã đăng xuất khỏi tất cả thiết bị." }
     */
    @PostMapping("/logout-all")
    public ResponseEntity<?> logoutAll() {
        try {
            // Lấy email từ Access Token đang dùng (đã được filter xác minh)
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String email = (String) auth.getPrincipal();

            // Tìm userId + userType (thử student trước, rồi admin)
            Long userId;
            String userType;
            var studentOpt = studentRepository.findByEmail(email);
            if (studentOpt.isPresent()) {
                userId   = studentOpt.get().getId();
                userType = studentOpt.get().getRole().name(); // STUDENT hoặc INSTRUCTOR
            } else {
                userId   = adminRepository.findByUsername(email)
                        .orElseThrow(() -> new RuntimeException("Không tìm thấy user"))
                        .getId();
                userType = "ADMIN";
            }

            // Xóa Refresh Token đúng loại user → không ảnh hưởng user khác có cùng ID
            refreshTokenService.revokeAllTokens(userId, userType);

            return ResponseEntity.ok(Map.of("message", "Đã đăng xuất khỏi tất cả thiết bị."));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    // ── Các API cũ giữ nguyên ─────────────────────────────────────────────

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestParam String email) {
        try {
            authService.forgotPassword(email);
            return ResponseEntity.ok(Map.of("message", "Mã OTP đã được gửi đến email của bạn!"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(@RequestParam String email, @RequestParam String otp) {
        try {
            boolean isValid = authService.verifyOtp(email, otp);
            if (isValid) return ResponseEntity.ok(Map.of("message", "OTP hợp lệ", "valid", true));
            return ResponseEntity.badRequest().body(Map.of("message", "OTP không đúng"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(
            @RequestParam String email,
            @RequestParam String otp,
            @RequestParam String newPassword) {
        try {
            authService.resetPassword(email, newPassword, otp);
            return ResponseEntity.ok(Map.of("message", "Đổi mật khẩu thành công! Vui lòng đăng nhập lại."));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/check-existence")
    public ResponseEntity<?> checkExistence(@RequestBody Map<String, String> request) {
        try {
            authService.checkExistence(request.get("email"), request.get("phone"));
            return ResponseEntity.ok(Map.of("message", "Thông tin hợp lệ"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    // ── ĐĂNG NHẬP BẰNG GOOGLE ─────────────────────────────────────────────
    @PostMapping("/google")
    public ResponseEntity<?> loginWithGoogle(@RequestBody Map<String, String> body) {
        try {
            String idToken = body.get("idToken");
            if (idToken == null || idToken.isBlank()) {
                return ResponseEntity.badRequest().body(Map.of("message", "idToken không được để trống"));
            }
            return ResponseEntity.ok(authService.loginWithGoogle(idToken));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/appeal")
    public ResponseEntity<?> submitAppeal(@RequestBody Map<String, String> payload) {
        try {
            String email   = payload.get("email");
            String title   = payload.get("title");
            String content = payload.get("content");

            if (email == null || email.isBlank()) {
                return ResponseEntity.badRequest().body(Map.of("message", "Không tìm thấy email của tài khoản bị khóa!"));
            }

            TicketRequestDTO.Create request = new TicketRequestDTO.Create();
            request.setTitle(title);
            request.setContent(content);

            return ResponseEntity.ok(ticketService.createTicket(email, request));
        } catch (Exception e) {
            String errorMsg = e.getMessage() != null ? e.getMessage() : "Lỗi hệ thống khi tạo đơn khiếu nại!";
            return ResponseEntity.badRequest().body(Map.of("message", errorMsg));
        }
    }

    private EnumSet<AdminPermission> resolveAdminPermissions(Admin admin) {
        AdminRole role = admin.getAdminRole() != null ? admin.getAdminRole() : AdminRole.STAFF;
        if (role == AdminRole.SUPER_ADMIN) {
            return AdminPermission.allPermissions();
        }
        EnumSet<AdminPermission> assigned = AdminPermission.fromCsv(admin.getPermissions());
        if (assigned.isEmpty()) {
            return AdminPermission.defaultStaffPermissions();
        }
        return assigned;
    }
}
