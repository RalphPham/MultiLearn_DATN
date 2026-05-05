package org.example.multileanproject.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.multileanproject.entity.Course;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseDTO {
    private Long id;
    private String title;
    private String slug;
    private String thumbnail;
    private BigDecimal price;
    private BigDecimal salePrice;
    private String status;       // PENDING, PUBLISHED, BLOCKED...
    private Long categoryId;
    private String categoryName;

    // BIẾN LƯU GIÁ GỢI Ý CỦA AI
    private BigDecimal suggestedPrice;

    // --- BỔ SUNG BIẾN CHO VUE.JS ---
    private String authorName;
    private String instructorName;
    private String instructorAvatar;

    private Long studentCount;
    private LocalDateTime createdAt;

    // 🔥 LOGIC CHUYỂN ĐỔI VÀ TÍNH TOÁN AI (SMART PRICING)
    public static CourseDTO fromEntity(Course course) {
        if (course == null) return null;

        // Xử lý Category
        String catName = "Chưa phân loại";
        Long catId = null;
        if (course.getCategory() != null) {
            catName = course.getCategory().getName();
            catId = course.getCategory().getId();
        }

        // Xử lý lấy tên Giảng viên
        String insName = "Giảng viên EduStar";
        String insAvatar = null;

        if (course.getInstructor() != null && course.getInstructor().getUser() != null) {
            insName = course.getInstructor().getUser().getFullName();
            insAvatar = course.getInstructor().getUser().getAvatar();
        }

        // 🤖 THUẬT TOÁN AI: TÍNH TOÁN GIÁ ĐỀ XUẤT 🤖
        double basePrice = 150000.0; // Giá sàn cơ bản: 150,000đ
        int durationMins = course.getTotalDuration() != null ? course.getTotalDuration() : 0;
        int lessons = course.getTotalLessons() != null ? course.getTotalLessons() : 0;

        // 100k cho mỗi giờ học, 10k cho mỗi tài liệu/bài tập
        double durationBonus = (durationMins / 60.0) * 100000.0;
        double lessonBonus = lessons * 10000.0;

        double suggested = basePrice + durationBonus + lessonBonus;
        // Làm tròn lên đến hàng chục nghìn (VD: 342,000đ -> 350,000đ)
        suggested = Math.ceil(suggested / 10000.0) * 10000.0;

        // Đảm bảo status hiển thị đúng
        String currentStatus = course.getStatus() != null ? course.getStatus().name() : "DRAFT";
        if ("PENDING_APPROVAL".equals(currentStatus)) {
            currentStatus = "PENDING";
        }

        return CourseDTO.builder()
                .id(course.getId())
                .title(course.getTitle())
                .slug(course.getSlug())
                .thumbnail(course.getThumbnail())
                .price(course.getPrice())
                .salePrice(course.getSalePrice())
                .status(currentStatus)
                .categoryId(catId)
                .categoryName(catName)
                .authorName(insName)
                .instructorName(insName)
                .instructorAvatar(insAvatar)
                .suggestedPrice(BigDecimal.valueOf(suggested)) // Bơm giá AI vào đây
                .studentCount(course.getStudentCount() != null ? Long.valueOf(course.getStudentCount()) : 0L)
                .createdAt(course.getCreatedAt())
                .build();
    }
}
