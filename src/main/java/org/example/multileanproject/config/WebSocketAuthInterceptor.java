package org.example.multileanproject.config;

import lombok.RequiredArgsConstructor;
import org.example.multileanproject.entity.Student;
import org.example.multileanproject.repository.StudentRepository;
import org.example.multileanproject.util.JwtUtil; // Import JwtUtil của bạn
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

import java.security.Principal;

@Component
@RequiredArgsConstructor
public class WebSocketAuthInterceptor implements ChannelInterceptor {

    private final JwtUtil jwtUtil; // Tiêm JwtUtil vào đây
    private final StudentRepository studentRepository;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            String authHeader = accessor.getFirstNativeHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);

                try {
                    // Dùng JwtUtil bóc token lấy email
                    String email = jwtUtil.extractUsername(token);

                    if (email != null) {
                        Student student = studentRepository.findByEmail(email).orElse(null);
                        if (student != null) {
                            // Tạo một Custom Principal lấy ID làm tên, để STOMP map đúng vào /user/{userId}/...
                            Principal userPrincipal = () -> String.valueOf(student.getId());
                            accessor.setUser(userPrincipal);
                        }
                    }
                } catch (Exception e) {
                    System.out.println("Lỗi xác thực WebSocket: " + e.getMessage());
                }
            }
        }
        return message;
    }
}