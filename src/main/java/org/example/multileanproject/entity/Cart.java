package org.example.multileanproject.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "carts") // Khớp với ảnh image_d580f9.jpg
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class Cart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Quan trọng: EAGER để load luôn thông tin sinh viên, tránh lỗi proxy khi convert sang JSON
    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "student_id", unique = true, nullable = false)
    private Student student;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    @PreUpdate
    protected void onUpdate() { updatedAt = LocalDateTime.now(); }
    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<CartItem> cartItems;
}