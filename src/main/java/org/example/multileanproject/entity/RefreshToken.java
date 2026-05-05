package org.example.multileanproject.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Bảng lưu Refresh Token – trái tim của hệ thống Semi-stateful.
 *
 * Mỗi lần user login → tạo 1 bản ghi mới ở đây.
 * Khi /refresh được gọi → tìm bản ghi này, kiểm tra hết hạn chưa, rồi cấp Access Token mới.
 * Khi /logout-all được gọi → xóa toàn bộ bản ghi của userId đó → mọi thiết bị bị văng ra.
 */
@Entity
@Table(name = "refresh_tokens")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Chuỗi token ngẫu nhiên (UUID) – dài hạn 7 ngày.
     * Không phải JWT – chỉ là chuỗi ngẫu nhiên tra DB.
     */
    @Column(nullable = false, unique = true, length = 512)
    private String token;

    /**
     * Liên kết với userId trong bảng students.
     * Không dùng @ManyToOne để tránh load Student mỗi lần query.
     */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /**
     * Loại user: "STUDENT" | "ADMIN"
     * Dùng để biết cần tìm thông tin ở bảng nào khi cấp Access Token mới.
     */
    @Column(name = "user_type", nullable = false, length = 20)
    private String userType;

    /**
     * Thời điểm hết hạn – mặc định 7 ngày kể từ lúc tạo.
     * Dùng Instant để tránh vấn đề timezone.
     */
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    /** Kiểm tra token đã hết hạn chưa */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }
}