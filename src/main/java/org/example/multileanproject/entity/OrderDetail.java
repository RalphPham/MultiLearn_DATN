package org.example.multileanproject.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "order_details") // Khớp với bảng trong SQL
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class OrderDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false) // Bắt buộc phải có Order
    @JsonIgnore
    private Order order;

    // --- SỬA LỖI CÚ PHÁP Ở DÒNG NÀY ---
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "course_id", nullable = false) // Bắt buộc phải có Course
    private Course course;

    @Column(name = "price", nullable = false)
    private BigDecimal price; // Giá tại thời điểm mua
}