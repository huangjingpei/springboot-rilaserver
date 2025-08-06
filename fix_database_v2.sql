-- 修复users表的字段问题
-- 如果name字段不存在，添加它
ALTER TABLE users ADD COLUMN IF NOT EXISTS name VARCHAR(255);

-- 如果name字段存在但没有默认值，允许为空
ALTER TABLE users MODIFY COLUMN name VARCHAR(255) NULL;

-- 为现有的记录设置name值（如果name为空，使用email前缀）
UPDATE users SET name = CONCAT('用户', SUBSTRING_INDEX(user_id, '@', 1)) WHERE name IS NULL OR name = '';

-- 确保user_id字段有唯一约束
ALTER TABLE users ADD UNIQUE INDEX IF NOT EXISTS uk_user_id (user_id);

-- 确保email字段有唯一约束（但允许为空）
ALTER TABLE users ADD UNIQUE INDEX IF NOT EXISTS uk_email (email);

-- 显示表结构
DESCRIBE users;

-- 显示一些示例数据
SELECT id, user_id, name, email, type, max_devices FROM users LIMIT 5; 