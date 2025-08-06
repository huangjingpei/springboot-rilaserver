-- 修复users表的email字段
-- 如果email字段不存在，添加它
ALTER TABLE users ADD COLUMN IF NOT EXISTS email VARCHAR(255);

-- 如果email字段存在但没有默认值，允许为空
ALTER TABLE users MODIFY COLUMN email VARCHAR(255) NULL;

-- 为现有的记录设置email值（如果email为空，使用user_id）
UPDATE users SET email = user_id WHERE email IS NULL;

-- 确保user_id字段有唯一约束
ALTER TABLE users ADD UNIQUE INDEX IF NOT EXISTS uk_user_id (user_id);

-- 确保email字段有唯一约束（但允许为空）
ALTER TABLE users ADD UNIQUE INDEX IF NOT EXISTS uk_email (email);

-- 显示表结构
DESCRIBE users; 