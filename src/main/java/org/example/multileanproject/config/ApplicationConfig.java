package org.example.multileanproject.config;

import lombok.RequiredArgsConstructor;
import org.example.multileanproject.repository.AdminRepository;
import org.example.multileanproject.repository.StudentRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
public class ApplicationConfig {

    private final StudentRepository studentRepository;
    private final AdminRepository adminRepository; // Inject thêm AdminRepository

    @Bean
    public UserDetailsService userDetailsService() {
        return username -> {
            // 1. Ưu tiên tìm trong bảng ADMINS trước
            // Lưu ý: Trong DB bảng admins của bạn cột là 'username', nhưng form gửi lên là email
            var adminOpt = adminRepository.findByUsername(username);
            if (adminOpt.isPresent()) {
                var admin = adminOpt.get();
                // Trả về đối tượng User của Spring Security
                return User.builder()
                        .username(admin.getUsername())
                        .password(admin.getPassword())
                        .roles("ADMIN") // Gán cứng quyền ADMIN
                        .build();
            }

            // 2. Nếu không phải Admin, tìm trong bảng STUDENTS (Bao gồm cả Student & Instructor)
            return studentRepository.findByEmail(username)
                    .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy tài khoản: " + username));
        };
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService());
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}