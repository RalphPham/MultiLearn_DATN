package org.example.multileanproject.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Data // Tự sinh Getter/Setter (bao gồm getOrder, setOrder)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ✅ THÊM ĐOẠN NÀY ĐỂ SỬA LỖI
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id") // Khóa ngoại trỏ đến bảng orders
    private Order order;

    @Column(name = "amount")
    private BigDecimal amount;

    @Column(name = "bank_code")
    private String bankCode;

    @Column(name = "transaction_ref")
    private String transactionRef;

    @Column(name = "status") // PENDING, SUCCESS, FAILED
    private String status;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}