-- Add optional campaign-level control for coupon stacking.
-- Default is disabled: courses in flash sale cannot receive checkout coupon discounts.

IF COL_LENGTH('sale_campaigns', 'allow_coupon_stacking') IS NULL
BEGIN
    ALTER TABLE sale_campaigns
    ADD allow_coupon_stacking BIT NOT NULL
        CONSTRAINT DF_sale_campaigns_allow_coupon_stacking DEFAULT 0;
END;
