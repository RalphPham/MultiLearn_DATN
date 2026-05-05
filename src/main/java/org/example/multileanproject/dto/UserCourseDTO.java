package org.example.multileanproject.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO trả về thông tin tóm tắt 1 khoá học
 * Dùng trong 2 trường hợp:
 *  1. Học viên - khoá học đang học(enroll)
 *  2. Giảng viên - khoá học đang sở hữu / dạy
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserCourseDTO {
    private Long id;
    private String title;
    private String slug;
    private String thumbnail;
    private String status; // PUBLISHED / PENDING_APPROVAL / DRAFT / BLOCKED

    //Thông tin về giá (Chỉ liên quan với giảng viên)
    private BigDecimal price;
    private BigDecimal salePrice;

    //Số học viên đã đăng ký (Chỉ liên quan với giảng viên)
    private Long studentCount;

    //Ngày học viên đăng ký / ngày giảng viên tạo khoá
    private LocalDateTime enrolledAt;   //Dành cho Student
    private LocalDateTime createdAt;    //Dành cho Instructor

    //Tiến độ học(Chỉ liên quan với student, 0-100)
    private Integer progressPercent;

    //Tên danh mục
    private String categoryName;
}
