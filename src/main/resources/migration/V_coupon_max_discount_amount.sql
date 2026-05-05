-- Optional cap for coupon discount amount.
-- NULL keeps existing behavior.

IF COL_LENGTH('coupons', 'max_discount_amount') IS NULL
BEGIN
    ALTER TABLE coupons
    ADD max_discount_amount DECIMAL(18, 2) NULL;
END;
