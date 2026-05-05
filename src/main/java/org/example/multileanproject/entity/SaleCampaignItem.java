package org.example.multileanproject.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Entity
@Table(name = "sale_campaign_items", uniqueConstraints = {
        // Đảm bảo 1 khóa học chỉ xuất hiện 1 lần trong 1 chiến dịch
        @UniqueConstraint(columnNames = {"campaign_id", "course_id"})
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SaleCampaignItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "campaign_id", nullable = false)
    private SaleCampaign campaign;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @Column(nullable = false)
    private BigDecimal promotionalPrice; // Giá sale (Ví dụ: 99.000đ)

    // 🔥 TÍNH NĂNG XỊN: GIỚI HẠN SỐ LƯỢNG SUẤT (FOMO)
    @Column(nullable = false)
    @Builder.Default
    private int totalSlots = 100; // Tổng số lượt được mua giá sale

    @Column(nullable = false)
    @Builder.Default
    private int soldSlots = 0; // Số lượt đã bán
}