-- Migration: Cập nhật commission_rate về 0.3 (30% cho giảng viên / 70% nền tảng)
-- Lý do: Giảng viên chỉ là cộng tác viên, không phải chủ sở hữu nội dung độc lập.
-- Chỉ update những course có commission_rate > 0.3 (tức là rate cũ 0.7, 0.8, v.v.)

UPDATE courses
SET commission_rate = 0.3
WHERE commission_rate > 0.3;

-- Rollback:
-- UPDATE courses SET commission_rate = 0.8 WHERE commission_rate = 0.3;
