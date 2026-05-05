-- Thêm cột instructor_id vào bảng coupons để phân biệt coupon của admin và instructor
-- Chạy script này trước khi khởi động Spring Boot (vì ddl-auto=validate)

ALTER TABLE coupons
    ADD instructor_id BIGINT NULL;

ALTER TABLE coupons
    ADD CONSTRAINT FK_coupons_instructor
    FOREIGN KEY (instructor_id) REFERENCES students(id) ON DELETE SET NULL;

-- instructor_id = NULL  → coupon toàn hệ thống (do Admin tạo)
-- instructor_id = <id>  → coupon riêng của giảng viên đó
