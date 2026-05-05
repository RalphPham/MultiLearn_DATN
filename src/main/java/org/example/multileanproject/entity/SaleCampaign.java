package org.example.multileanproject.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.beans.Transient;
import java.time.LocalDateTime;

@Entity
@Table(name = "sale_campaigns")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SaleCampaign {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name; // Ví dụ: "Siêu Sale Black Friday"

    @Column(nullable = false)
    private LocalDateTime startDate; // Giờ bắt đầu

    @Column(nullable = false)
    private LocalDateTime endDate; // Giờ kết thúc

    @Column(nullable = false)
    @Builder.Default
    private boolean isActive = true; // Nút tắt khẩn cấp cho Admin

    // Hàm phụ trợ lấy trạng thái hiện tại (Không lưu vào DB)
    @Column(name = "allow_coupon_stacking", nullable = false)
    @Builder.Default
    private boolean allowCouponStacking = false;

    @Transient
    public String getStatus() {
        LocalDateTime now = LocalDateTime.now();
        if (!isActive) return "DISABLED";
        if (now.isBefore(startDate)) return "UPCOMING";
        if (now.isAfter(endDate)) return "ENDED";
        return "ACTIVE";
    }
}
