package org.example.multileanproject.config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig {
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOriginPatterns("*")
                        .allowedMethods(
                                "GET",      // Đọc dữ liệu
                                "POST",     // Tạo mới
                                "PUT",      // Cập nhật toàn bộ
                                "PATCH",    // Cập nhật một phần
                                "DELETE",   // Xóa
                                "OPTIONS"   // Preflight request
                        )

                        .allowedHeaders("*")
                        .allowCredentials(true)
                        .maxAge(3600);
            }
        };
    }
}