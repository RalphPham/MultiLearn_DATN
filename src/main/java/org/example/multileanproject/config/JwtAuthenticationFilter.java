package org.example.multileanproject.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.example.multileanproject.entity.AdminPermission;
import org.example.multileanproject.repository.StudentRepository;
import org.example.multileanproject.util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final StudentRepository studentRepository;
    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String userEmail;

        // 1. Nếu không có Token, cho đi tiếp luôn (để SecurityConfig xử lý quyền public)
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 2. Lấy Token
        jwt = authHeader.substring(7);

        // Cho phép locked user truy cập các endpoint auth công khai (vd: /api/auth/appeal, /api/auth/refresh)
        // — không chặn ở filter, để controller xử lý tiếp.
        final String requestPath = request.getRequestURI();
        final boolean isPublicAuthPath = requestPath != null && requestPath.startsWith("/api/auth/");

        try {
            // 3. Trích xuất Email từ Token
            userEmail = jwtUtil.extractUsername(jwt);

            if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                // 🔥 KIỂM TRA TRẠNG THÁI TÀI KHOẢN TRONG DATABASE
                // Chỉ kiểm tra Student; ADMIN dùng AdminRepository riêng nên skip nếu không tìm thấy
                var studentOpt = studentRepository.findByEmail(userEmail);
                if (studentOpt.isPresent()) {
                    if (!studentOpt.get().isActive() && !isPublicAuthPath) {
                        logger.warn("⚠️ Chặn truy cập: Tài khoản {} đang bị KHÓA nhưng vẫn cố dùng Token cũ.", userEmail);
                        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                        response.setContentType("application/json;charset=UTF-8");
                        response.getWriter().write("{\"message\": \"Tài khoản của bạn đã bị khóa! Vui lòng đăng nhập lại.\"}");
                        return;
                    }
                } else {
                    // User không tồn tại trong DB — chỉ chặn nếu role là STUDENT hoặc INSTRUCTOR
                    // (ADMIN lưu trong bảng admins riêng, không có trong students)
                    String tokenRole = jwtUtil.extractClaim(jwt, claims -> claims.get("role", String.class));
                    if (!"ADMIN".equals(tokenRole)) {
                        logger.warn("⚠️ Chặn truy cập: User {} không tồn tại trong DB.", userEmail);
                        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                        response.setContentType("application/json;charset=UTF-8");
                        response.getWriter().write("{\"message\": \"Tài khoản không tồn tại.\"}");
                        return;
                    }
                }

                // 4. Trích xuất Role từ Token
                String role = jwtUtil.extractClaim(jwt, claims -> claims.get("role", String.class));

                // Fallback: Nếu không có role trong token, mặc định là STUDENT
                if (role == null || role.isEmpty()) {
                    role = "STUDENT";
                }

                // 5. Validate Token và set quyền
                if (jwtUtil.validateToken(jwt, userEmail)) {
                    // Tạo Authority danh sách (cho phép check cả ADMIN lẫn SUPER_ADMIN)
                    List<SimpleGrantedAuthority> authorities;
                    if ("ADMIN".equals(role)) {
                        String adminRole = jwtUtil.extractClaim(jwt, claims -> claims.get("adminRole", String.class));
                        List<SimpleGrantedAuthority> adminAuthorities = new ArrayList<>();
                        adminAuthorities.add(new SimpleGrantedAuthority("ADMIN"));

                        if ("SUPER_ADMIN".equals(adminRole)) {
                            adminAuthorities.add(new SimpleGrantedAuthority("SUPER_ADMIN"));
                        }

                        Object permissionsClaim = jwtUtil.extractClaim(jwt, claims -> claims.get("permissions"));
                        if (permissionsClaim instanceof List<?> rawList) {
                            for (Object item : rawList) {
                                if (item == null) continue;
                                String permission = String.valueOf(item).trim();
                                if (!permission.isBlank()) {
                                    adminAuthorities.add(new SimpleGrantedAuthority(permission));
                                }
                            }
                        }

                        if (adminAuthorities.stream().noneMatch(a -> !"ADMIN".equals(a.getAuthority()) && !"SUPER_ADMIN".equals(a.getAuthority()))) {
                            List<String> fallbackPermissions = "SUPER_ADMIN".equals(adminRole)
                                    ? AdminPermission.toNames(AdminPermission.allPermissions())
                                    : AdminPermission.toNames(AdminPermission.defaultStaffPermissions());
                            for (String permission : fallbackPermissions) {
                                adminAuthorities.add(new SimpleGrantedAuthority(permission));
                            }
                        }

                        authorities = adminAuthorities;
                    } else {
                        authorities = List.of(new SimpleGrantedAuthority(role));
                    }

                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userEmail,
                            null,
                            authorities
                    );

                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    // 6. Set Authentication vào Context (Đăng nhập thành công)
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    request.setAttribute("AUTHENTICATED_BY_JWT", Boolean.TRUE);

                    // Log để debug: In ra xem user đang có quyền gì
                    logger.info("User: {}, Role extracted: {}", userEmail, role);
                }
            }
        } catch (Exception e) {
            // Log lỗi nhưng không throw exception để request tiếp tục (có thể vào permitAll)
            logger.error("Lỗi xác thực JWT: {}", e.getMessage());
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }
}
