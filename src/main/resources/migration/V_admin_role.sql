-- Thêm cột admin_role vào bảng admins
-- Chạy script này trước khi khởi động Spring Boot (vì ddl-auto=validate)

ALTER TABLE admins
    ADD admin_role NVARCHAR(20) NOT NULL DEFAULT 'SUPER_ADMIN';

-- (Tùy chọn) Đặt tài khoản staff thành STAFF:
-- UPDATE admins SET admin_role = 'STAFF' WHERE username = 'staff@multilearn.vn';
