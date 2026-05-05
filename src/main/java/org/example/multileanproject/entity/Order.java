package org.example.multileanproject.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    @JsonIgnore
    private Student student;


    @Builder.Default
    @Column(name = "original_amount", nullable = false)
    private BigDecimal originalAmount = BigDecimal.ZERO;


    @Builder.Default
    @Column(name = "final_amount", nullable = false)
    private BigDecimal finalAmount = BigDecimal.ZERO;


    @Column(name = "coupon_code")
    private String couponCode;

    @Column(name = "transaction_ref") // Thêm dòng này
    private String transactionRef;    // Mã giao dịch từ VNPay (Cần thiết để hoàn tiền)


    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private OrderStatus status = OrderStatus.PENDING;
    // PENDING, COMPLETED, FAILED


    @Column(name = "payment_method")
    private String paymentMethod;

    @Column(name = "note")
    private String note;


    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<OrderDetail> orderItems;


    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;


    @PrePersist
    @PreUpdate
    public void ensureAmounts() {
        if (this.originalAmount == null) {
            this.originalAmount = BigDecimal.ZERO;
        }
        if (this.finalAmount == null) {
            this.finalAmount = this.originalAmount;
        }
    }
}
