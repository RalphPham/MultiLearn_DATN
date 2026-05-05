package org.example.multileanproject.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final AuthenticationProvider authenticationProvider;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider)
                .authorizeHttpRequests(auth -> auth

                        // 🔥 [MỚI] Bắt buộc đăng nhập mới được gọi API logout-all
                        .requestMatchers("/api/auth/logout-all").authenticated()

                        .requestMatchers(
                                "/api/auth/**",
                                "/uploads/**",
                                "/generated-certificates/**",
                                "/api/public/**",
                                "/api/uploads/avatar",
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/swagger-ui.html",
                                "/ws/**"
                        ).permitAll()

                        // VNPay callback gọi từ server VNPay (không có token) → phải permit
                        .requestMatchers(HttpMethod.GET, "/api/payment/vnpay-callback").permitAll()

                        // PUBLIC GET
                        .requestMatchers(HttpMethod.GET, "/api/courses/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/categories/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/campaigns/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/certificates/verify/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/learning/lessons/*/preview-url").permitAll()

                        // RATINGS
                        .requestMatchers(HttpMethod.GET, "/api/ratings/course/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/ratings/course/**").authenticated()
                        .requestMatchers("/api/ratings/instructor/**").hasAnyAuthority("INSTRUCTOR", "ADMIN")

                        // PRIVATE ROUTES
                        .requestMatchers("/api/courses/my-courses").authenticated()
                        .requestMatchers("/api/courses/mine").authenticated()
                        .requestMatchers("/api/courses/*/learn").authenticated()

                        .requestMatchers(
                                "/api/cart/**",
                                "/api/orders/**",
                                "/api/wishlist/**",
                                "/api/students/**",
                                "/api/learning/**",
                                "/api/notifications/**",
                                "/api/enrollments/**",
                                "/api/messages/**",
                                "/api/student/refunds/**",
                                "/api/certificates/**"
                        ).authenticated()

                        .requestMatchers("/api/instructor/performance/**")
                        .hasAuthority("INSTRUCTOR")

                        .requestMatchers("/api/instructor/coupons/**")
                        .denyAll()

                        .requestMatchers("/api/instructor/resources/**")
                        .hasAuthority("INSTRUCTOR")

                        .requestMatchers("/api/instructor/withdraw/**")
                        .hasAuthority("INSTRUCTOR")

                        .requestMatchers("/api/instructor/courses/**")
                        .hasAuthority("INSTRUCTOR")

                        .requestMatchers(HttpMethod.POST, "/api/courses/**").hasAuthority("INSTRUCTOR")
                        .requestMatchers(HttpMethod.PUT, "/api/courses/**").hasAuthority("INSTRUCTOR")
                        .requestMatchers(HttpMethod.DELETE, "/api/courses/**").hasAuthority("INSTRUCTOR")

                        .requestMatchers(HttpMethod.POST, "/api/categories/**").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/categories/**").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/categories/**").hasAuthority("ADMIN")

                        // SUPER_ADMIN ONLY
                        .requestMatchers("/api/admin/accounts/**").hasAuthority("SUPER_ADMIN")

                        // ADMIN ONLY
                        .requestMatchers("/api/admin/**").hasAuthority("ADMIN")

                        // QUIZ STUDENT FLOW (take + submit)
                        .requestMatchers(HttpMethod.GET, "/api/quizzes/*/take").hasAnyAuthority("STUDENT", "INSTRUCTOR")
                        .requestMatchers(HttpMethod.POST, "/api/quizzes/submit").hasAnyAuthority("STUDENT", "INSTRUCTOR")
                        .requestMatchers(HttpMethod.POST, "/api/quizzes/*/submit").hasAnyAuthority("STUDENT", "INSTRUCTOR")

                        // INSTRUCTOR ONLY
                        .requestMatchers(HttpMethod.POST, "/api/sections/**").hasAuthority("INSTRUCTOR")
                        .requestMatchers(HttpMethod.PUT, "/api/sections/**").hasAuthority("INSTRUCTOR")
                        .requestMatchers(HttpMethod.DELETE, "/api/sections/**").hasAuthority("INSTRUCTOR")
                        .requestMatchers(HttpMethod.POST, "/api/lessons/**").hasAuthority("INSTRUCTOR")
                        .requestMatchers(HttpMethod.PUT, "/api/lessons/**").hasAuthority("INSTRUCTOR")
                        .requestMatchers(HttpMethod.DELETE, "/api/lessons/**").hasAuthority("INSTRUCTOR")
                        .requestMatchers(HttpMethod.GET, "/api/quizzes/*").hasAnyAuthority("INSTRUCTOR", "ADMIN", "SUPER_ADMIN", "COURSE_VIEW")
                        .requestMatchers(HttpMethod.POST, "/api/quizzes/**").hasAuthority("INSTRUCTOR")
                        .requestMatchers(HttpMethod.PUT, "/api/quizzes/**").hasAuthority("INSTRUCTOR")
                        .requestMatchers(HttpMethod.DELETE, "/api/quizzes/**").hasAuthority("INSTRUCTOR")
                        .requestMatchers(HttpMethod.POST, "/api/assignments/**").hasAuthority("INSTRUCTOR")
                        .requestMatchers(HttpMethod.PUT, "/api/assignments/**").hasAuthority("INSTRUCTOR")
                        .requestMatchers(HttpMethod.DELETE, "/api/assignments/**").hasAuthority("INSTRUCTOR")
                        .requestMatchers(HttpMethod.GET, "/api/bundles/{id}").hasAuthority("INSTRUCTOR")
                        .requestMatchers(HttpMethod.POST, "/api/bundles/**").hasAuthority("INSTRUCTOR")
                        .requestMatchers(HttpMethod.PUT, "/api/bundles/**").hasAuthority("INSTRUCTOR")
                        .requestMatchers(HttpMethod.DELETE, "/api/bundles/**").hasAuthority("INSTRUCTOR")
                        .requestMatchers(HttpMethod.POST, "/api/media/**").hasAuthority("INSTRUCTOR")
                        .requestMatchers(HttpMethod.DELETE, "/api/media/**").hasAuthority("INSTRUCTOR")

                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(e -> e
                        .authenticationEntryPoint((request, response, authException) -> {
                            boolean hasAuthenticatedUser = Boolean.TRUE.equals(request.getAttribute("AUTHENTICATED_BY_JWT"));

                            if (hasAuthenticatedUser) {
                                response.sendError(HttpStatus.FORBIDDEN.value());
                            } else {
                                response.sendError(HttpStatus.UNAUTHORIZED.value());
                            }
                        })
                        .accessDeniedHandler((request, response, accessDeniedException) ->
                                response.sendError(HttpStatus.FORBIDDEN.value()))
                );

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of("*"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
