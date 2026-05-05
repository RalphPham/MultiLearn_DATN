package org.example.multileanproject.dto;

import lombok.*;
import org.example.multileanproject.entity.Course;
import org.example.multileanproject.entity.SaleCampaignItem;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CourseListDTO {
    private Long id;
    private String title;
    private String slug;
    private String thumbnail;
    private BigDecimal price;
    private BigDecimal salePrice;
    private String level;
    private Double averageRating;
    private Integer studentCount;
    private Integer totalLessons;
    private Integer totalDuration;

    private String categoryName;
    private String authorName;
    private String instructorName;
    private String instructorAvatar;

    private Double progress;

    // 🔥 BỔ SUNG THÔNG TIN FLASH SALE 🔥
    private Integer totalSlots;
    private Integer soldSlots;
    private Boolean isFlashSale;

    public static CourseListDTO fromEntity(Course course, SaleCampaignItem activeSale) {
        if (course == null) return null;

        String catName = "Chưa phân loại";
        if (course.getCategory() != null) {
            catName = course.getCategory().getName();
        }

        String insName = "EduStar Teacher";
        String insAvatar = null;

        if (course.getInstructor() != null && course.getInstructor().getUser() != null) {
            insName = course.getInstructor().getUser().getFullName();
            insAvatar = course.getInstructor().getUser().getAvatar();
        }

        CourseListDTO dto = CourseListDTO.builder()
                .id(course.getId())
                .title(course.getTitle())
                .slug(course.getSlug())
                .thumbnail(course.getThumbnail())
                .price(course.getPrice())
                .salePrice(course.getSalePrice()) // Giá mặc định nếu không có sale
                .level(course.getLevel())
                .averageRating(course.getAverageRating() != null ? course.getAverageRating() : 0.0)
                .studentCount(course.getStudentCount() != null ? course.getStudentCount() : 0)
                .totalLessons(course.getTotalLessons())
                .totalDuration(course.getTotalDuration())
                .categoryName(catName)
                .authorName(insName)
                .instructorName(insName)
                .instructorAvatar(insAvatar)
                .progress(0.0)
                .build();

        // 🔥 NẾU ĐANG CÓ FLASH SALE -> GHI ĐÈ GIÁ VÀ BƠM SỐ SLOT VÀO DTO
        if (activeSale != null) {
            dto.setSalePrice(activeSale.getPromotionalPrice());
            dto.setTotalSlots(activeSale.getTotalSlots());
            dto.setSoldSlots(activeSale.getSoldSlots());
            dto.setIsFlashSale(true);
        } else {
            dto.setIsFlashSale(false);
        }

        return dto;
    }
}