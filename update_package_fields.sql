-- 更新packages表，添加新字段
-- 执行时间：2024年

-- 添加自动续费字段
ALTER TABLE packages ADD COLUMN auto_renew BOOLEAN DEFAULT FALSE COMMENT '自动续费，默认不启用';

-- 添加支付方式字段
ALTER TABLE packages ADD COLUMN payment_method VARCHAR(20) DEFAULT 'yearly' COMMENT '支付方式，默认年付';

-- 添加试用期字段
ALTER TABLE packages ADD COLUMN trial_period INT DEFAULT 0 COMMENT '试用期天数，默认0';

-- 添加宽限期字段
ALTER TABLE packages ADD COLUMN grace_period INT DEFAULT 0 COMMENT '宽限期天数，默认0';

-- 添加套餐总价字段
ALTER TABLE packages ADD COLUMN total_price DECIMAL(10,2) COMMENT '套餐总价（价格 × 订阅时长）';

-- 更新现有记录的默认值
UPDATE packages SET 
    auto_renew = FALSE,
    payment_method = 'yearly',
    trial_period = 0,
    grace_period = 0
WHERE auto_renew IS NULL;

-- 为现有记录计算套餐总价（假设为年付）
UPDATE packages SET 
    total_price = price * (discount / 100) * 12
WHERE total_price IS NULL AND price IS NOT NULL AND discount IS NOT NULL; 