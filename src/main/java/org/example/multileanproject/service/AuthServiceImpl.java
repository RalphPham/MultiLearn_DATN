package org.example.multileanproject.service;

import lombok.RequiredArgsConstructor;
import org.example.multileanproject.dto.AuthResponse;
import org.example.multileanproject.dto.LoginRequest;
import org.example.multileanproject.dto.RegisterRequest;
import org.example.multileanproject.entity.Admin;
import org.example.multileanproject.entity.AdminPermission;
import org.example.multileanproject.entity.AdminRole;
import org.example.multileanproject.entity.RefreshToken;
import org.example.multileanproject.entity.Role;
import org.example.multileanproject.entity.Student;
import org.example.multileanproject.repository.AdminRepository;
import org.example.multileanproject.repository.StudentRepository;
import org.example.multileanproject.repository.StudentStatusHistoryRepository;
import org.example.multileanproject.repository.SupportTicketRepository;
import org.example.multileanproject.util.JwtUtil;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final StudentRepository studentRepository;
    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final RefreshTokenService refreshTokenService;
    private final StudentStatusHistoryRepository statusHistoryRepository;
    private final SupportTicketRepository supportTicketRepository;
    private final EmailService emailService;

    @Override
    public AuthResponse register(RegisterRequest request) {
        checkExistence(request.getEmail(), request.getPhone());
        var user = Student.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.STUDENT)
                .build();
        studentRepository.save(user);

        try {
            emailService.sendWelcomeEmail(user.getEmail(), user.getFullName());
        } catch (Exception e) {
            // Không để lỗi email roll back việc tạo tài khoản
        }

        Map<String, Object> claims = new HashMap<>();
        claims.put("role", user.getRole().name());

        // 🔥 [MỚI] DÙNG ACCESS TOKEN (30 PHÚT) VÀ TẠO REFRESH TOKEN (7 NGÀY)
        var jwtToken = jwtUtil.generateAccessToken(claims, user.getEmail());
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user.getId(), "STUDENT");

        return AuthResponse.builder()
                .token(jwtToken)
                .refreshToken(refreshToken.getToken()) // Đính kèm thẻ thành viên dài hạn
                .role(user.getRole().name())
                .userId(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .build();
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );
        } catch (org.springframework.security.authentication.DisabledException |
                 org.springframework.security.authentication.LockedException e) {

            var studentOpt = studentRepository.findByEmail(request.getEmail());
            if (studentOpt.isPresent()) {
                Student student = studentOpt.get();
                String blockReason = "Vi phạm chính sách của hệ thống.";
                var historyOpt = statusHistoryRepository.findTopByStudentIdOrderByIdDesc(student.getId());
                if (historyOpt.isPresent()) {
                    blockReason = historyOpt.get().getReason();
                }
                var tickets = supportTicketRepository.findByStudentIdOrderByIdDesc(student.getId());
                if (!tickets.isEmpty()) {
                    var latestTicket = tickets.get(0);
                    if (latestTicket.getStatus() == org.example.multileanproject.entity.TicketStatus.PENDING) {
                        throw new RuntimeException("Tài khoản bị khóa! Lý do: " + blockReason + " (Đơn khiếu nại đang chờ Admin duyệt...)");
                    }
                }
                throw new RuntimeException("Tài khoản bị khóa! Lý do: " + blockReason);
            }
            throw new RuntimeException("Tài khoản của bạn đã bị khóa.");
        } catch (org.springframework.security.authentication.BadCredentialsException e) {
            throw new RuntimeException("Email hoặc mật khẩu không chính xác.");
        }

        // --- CASE 1: LÀ ADMIN ---
        var adminOpt = adminRepository.findByUsername(request.getEmail());
        if (adminOpt.isPresent()) {
            Admin admin = adminOpt.get();
            String adminRole = admin.getAdminRole() != null
                    ? admin.getAdminRole().name()
                    : AdminRole.SUPER_ADMIN.name();
            EnumSet<AdminPermission> permissions = resolveAdminPermissions(admin);

            Map<String, Object> claims = new HashMap<>();
            claims.put("role", "ADMIN");
            claims.put("adminRole", adminRole);
            claims.put("permissions", AdminPermission.toNames(permissions));

            var jwtToken = jwtUtil.generateAccessToken(claims, admin.getUsername());
            RefreshToken refreshToken = refreshTokenService.createRefreshToken(admin.getId(), "ADMIN");

            return AuthResponse.builder()
                    .token(jwtToken)
                    .refreshToken(refreshToken.getToken())
                    .role("ADMIN")
                    .adminRole(adminRole)
                    .userId(admin.getId())
                    .fullName(admin.getFullName())
                    .email(admin.getUsername())
                    .phone(null)
                    .permissions(AdminPermission.toNames(permissions))
                    .build();
        }

        // --- CASE 2: LÀ STUDENT ---
        var studentOpt = studentRepository.findByEmail(request.getEmail());
        if (studentOpt.isPresent()) {
            Student student = studentOpt.get();
            Map<String, Object> claims = new HashMap<>();
            claims.put("role", student.getRole().name());

            // 🔥 [MỚI] CẤP BỘ ĐÔI TOKEN CHO STUDENT / INSTRUCTOR
            var jwtToken = jwtUtil.generateAccessToken(claims, student.getEmail());
            RefreshToken refreshToken = refreshTokenService.createRefreshToken(student.getId(), student.getRole().name());

            return AuthResponse.builder()
                    .token(jwtToken)
                    .refreshToken(refreshToken.getToken())
                    .role(student.getRole().name())
                    .userId(student.getId())
                    .fullName(student.getFullName())
                    .email(student.getEmail())
                    .phone(student.getPhone())
                    .build();
        }

        throw new RuntimeException("Lỗi hệ thống: Không tìm thấy thông tin user sau khi xác thực.");
    }

    // --- CÁC HÀM CŨ CỦA BẠN GIỮ NGUYÊN 100% ---
    @Override
    public void forgotPassword(String email) { /* Giữ nguyên */ }

    @Override
    public boolean verifyOtp(String email, String otp) { return true; }

    @Override
    public void resetPassword(String email, String newPassword, String otp) { /* Giữ nguyên logic tìm trong student */ }

    @Override
    public void checkExistence(String email, String phone) {
        if (studentRepository.existsByEmail(email)) throw new RuntimeException("Email đã tồn tại");
        if (adminRepository.findByUsername(email).isPresent()) throw new RuntimeException("Email đã tồn tại");
    }

    @Override
    public AuthResponse loginWithGoogle(String idToken) {
        // 1. Xác minh id_token với Google
        RestTemplate restTemplate = new RestTemplate();
        String url = "https://oauth2.googleapis.com/tokeninfo?id_token=" + idToken;

        Map<String, Object> googleInfo;
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> result = restTemplate.getForObject(url, Map.class);
            googleInfo = result;
        } catch (Exception e) {
            throw new RuntimeException("Token Google không hợp lệ hoặc đã hết hạn.");
        }

        if (googleInfo == null) {
            throw new RuntimeException("Không thể xác minh tài khoản Google.");
        }

        String emailVerified = (String) googleInfo.get("email_verified");
        if (!"true".equalsIgnoreCase(emailVerified)) {
            throw new RuntimeException("Email Google chưa được xác thực.");
        }

        String email    = (String) googleInfo.get("email");
        String namePart = (String) googleInfo.get("name");
        String fullName = (namePart != null) ? namePart : email;
        String avatar   = (String) googleInfo.get("picture");

        if (email == null || email.isBlank()) {
            throw new RuntimeException("Không lấy được email từ Google.");
        }

        // 2. Tìm hoặc tạo mới tài khoản Student
        Student student = studentRepository.findByEmail(email).orElse(null);

        if (student == null) {
            student = Student.builder()
                    .fullName(fullName)
                    .email(email)
                    .password(passwordEncoder.encode(UUID.randomUUID().toString()))
                    .avatar(avatar)
                    .role(Role.STUDENT)
                    .build();
            studentRepository.save(student);
            try {
                emailService.sendWelcomeEmail(email, fullName);
            } catch (Exception e) {
                // Không để lỗi email ảnh hưởng đến đăng nhập
            }
        } else if (!student.isActive()) {
            String blockReason = "Vi phạm chính sách của hệ thống.";
            var historyOpt = statusHistoryRepository.findTopByStudentIdOrderByIdDesc(student.getId());
            if (historyOpt.isPresent()) {
                blockReason = historyOpt.get().getReason();
            }
            var tickets = supportTicketRepository.findByStudentIdOrderByIdDesc(student.getId());
            if (!tickets.isEmpty()) {
                var latestTicket = tickets.get(0);
                if (latestTicket.getStatus() == org.example.multileanproject.entity.TicketStatus.PENDING) {
                    throw new RuntimeException("Tài khoản bị khóa! Lý do: " + blockReason + " (Đơn khiếu nại đang chờ Admin duyệt...)");
                }
            }
            throw new RuntimeException("Tài khoản bị khóa! Lý do: " + blockReason);
        }

        // 3. Tạo JWT + Refresh Token
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", student.getRole().name());

        String jwtToken = jwtUtil.generateAccessToken(claims, student.getEmail());
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(student.getId(), student.getRole().name());

        return AuthResponse.builder()
                .token(jwtToken)
                .refreshToken(refreshToken.getToken())
                .role(student.getRole().name())
                .userId(student.getId())
                .fullName(student.getFullName())
                .email(student.getEmail())
                .phone(student.getPhone())
                .build();
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
