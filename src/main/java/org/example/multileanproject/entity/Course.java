package org.example.multileanproject.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "courses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class Course {

    // ================== BASIC INFO ==================

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, columnDefinition = "NVARCHAR(255)")
    private String title;

    @Column(unique = true, nullable = false, length = 255)
    private String slug;

    @Builder.Default
    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal price = BigDecimal.ZERO;

    @Column(name = "sale_price", precision = 18, scale = 2)
    private BigDecimal salePrice;

    /**
     * Hoa hồng cho giáo viên
     * Ví dụ 0.8 = 80%.
     */
    @Builder.Default
    @Column(name = "commission_rate", nullable = false)
    private Double commissionRate = 0.3;

    @Column(length = 500)
    private String thumbnail;

    @Column(name = "short_description", length = 500, columnDefinition = "NVARCHAR(500)")
    private String shortDescription;

    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String description;

    @Column(name = "language", length = 50)
    private String language;

    @Column(name = "learning_outcomes", columnDefinition = "NVARCHAR(MAX)")
    private String learningOutcomes;

    @Builder.Default
    @Column(length = 20)
    private String level = "BEGINNER";

    // ================== STATUS ==================

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(length = 30, nullable = false)
    private CourseStatus status = CourseStatus.DRAFT;

    // ================== STATISTICS ==================

    @Builder.Default
    @Column(name = "student_count", nullable = false)
    private Integer studentCount = 0;

    @Builder.Default
    @Column(name = "total_lessons", nullable = false)
    private Integer totalLessons = 0;

    /**
     * Tổng thời lượng VIDEO tính theo giây
     */
    @Builder.Default
    @Column(name = "total_duration", nullable = false)
    private Integer totalDuration = 0;

    @Builder.Default
    @Column(name = "average_rating", nullable = false)
    private Double averageRating = 0.0;

    /** Bật tính năng thuê khóa học */
    @Builder.Default
    @Column(name = "rental_enabled", nullable = false)
    private Boolean rentalEnabled = false;

    // ================== AUDIT ==================

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ================== RELATIONSHIPS ==================

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "instructor_id", nullable = false)
    @JsonIgnore
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Instructor instructor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Category category;

    @OneToMany(
            mappedBy = "course",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    @OrderBy("orderIndex ASC")
    @Builder.Default
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<Section> sections = new ArrayList<>();

    // ================== BUSINESS LOGIC ==================

    /**
     * Tính lại totalLessons và totalDuration từ dữ liệu lesson THẬT
     *
     * totalLessons:
     * - đếm toàn bộ lesson thuộc tất cả sections
     *
     * totalDuration:
     * - chỉ cộng lesson VIDEO
     * - duration lưu theo giây
     *
     * Lý do không cộng từ section.getLessonCount()/getTotalDuration():
     * - các field summary ở section có thể chưa được sync
     * - dễ gây dữ liệu ảo như 0p hoặc lệch tổng
     */
    public void recalculateTotals() {
        if (sections == null || sections.isEmpty()) {
            this.totalLessons = 0;
            this.totalDuration = 0;
            return;
        }

        int lessonCount = 0;
        int duration = 0;

        for (Section section : sections) {
            if (section == null || section.getLessons() == null) {
                continue;
            }

            for (Lesson lesson : section.getLessons()) {
                if (lesson == null) {
                    continue;
                }

                lessonCount++;

                if (lesson.getType() == LessonType.VIDEO && lesson.getDuration() != null) {
                    duration += Math.max(lesson.getDuration(), 0);
                }
            }
        }

        this.totalLessons = lessonCount;
        this.totalDuration = duration;
    }

    /**
     * Giá đang hiển thị thực tế
     */
    @Transient
    public BigDecimal getEffectivePrice() {
        if (salePrice != null && salePrice.compareTo(BigDecimal.ZERO) > 0 && salePrice.compareTo(price) < 0) {
            return salePrice;
        }
        return price != null ? price : BigDecimal.ZERO;
    }

    /**
     * % giảm giá
     */
    @Transient
    public Integer getDiscountPercentage() {
        if (price == null || salePrice == null) return 0;
        if (price.compareTo(BigDecimal.ZERO) <= 0) return 0;
        if (salePrice.compareTo(BigDecimal.ZERO) <= 0) return 0;
        if (salePrice.compareTo(price) >= 0) return 0;

        BigDecimal discount = price.subtract(salePrice)
                .multiply(BigDecimal.valueOf(100))
                .divide(price, 0, java.math.RoundingMode.HALF_UP);

        return discount.intValue();
    }

    // ================== SAFETY HOOK ==================

    @PrePersist
    @PreUpdate
    public void ensureDefaults() {
        if (price == null) price = BigDecimal.ZERO;
        if (commissionRate == null) commissionRate = 0.3;
        if (studentCount == null) studentCount = 0;
        if (totalLessons == null) totalLessons = 0;
        if (totalDuration == null) totalDuration = 0;
        if (averageRating == null) averageRating = 0.0;
        if (status == null) status = CourseStatus.DRAFT;
        if (sections == null) sections = new ArrayList<>();
    }
}
