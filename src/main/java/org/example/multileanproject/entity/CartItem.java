package org.example.multileanproject.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "cart_items")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class CartItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id", nullable = false)
    @JsonIgnore // ✅ QUAN TRỌNG: Ngắt vòng lặp JSON Cart -> CartItem -> Cart
    private Cart cart;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "course_id", nullable = false)
    // Không cần JsonIgnore ở đây vì ta cần lấy thông tin khóa học
    private Course course;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    private BigDecimal price;

    @PrePersist
    protected void onCreate() { createdAt = LocalDateTime.now(); }
}