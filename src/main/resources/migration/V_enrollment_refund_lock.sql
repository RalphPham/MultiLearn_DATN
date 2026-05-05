-- Migration: Thêm cột refund_locked_at vào bảng enrollments
-- Mục đích: Khoá quyền hoàn tiền ngay khi học viên bắt đầu học lần đầu tiên

ALTER TABLE enrollments
    ADD refund_locked_at DATETIME NULL;

-- Rollback:
-- ALTER TABLE enrollments DROP COLUMN refund_locked_at;
