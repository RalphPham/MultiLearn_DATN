-- Tạo bảng ghi nhật ký hành động của admin
-- Chạy script này trước khi khởi động Spring Boot (vì ddl-auto=validate)

CREATE TABLE admin_action_logs (
    id             BIGINT IDENTITY(1,1) PRIMARY KEY,
    admin_username NVARCHAR(100) NOT NULL,
    action         NVARCHAR(100) NOT NULL,
    description    NVARCHAR(500) NULL,
    target_id      BIGINT        NULL,
    target_type    NVARCHAR(50)  NULL,
    created_at     DATETIME2     DEFAULT GETDATE() NOT NULL
);

CREATE INDEX idx_aal_admin ON admin_action_logs (admin_username);
CREATE INDEX idx_aal_action ON admin_action_logs (action);
CREATE INDEX idx_aal_created ON admin_action_logs (created_at DESC);
