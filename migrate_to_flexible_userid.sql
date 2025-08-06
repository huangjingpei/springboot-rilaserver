-- 数据库迁移脚本：支持邮箱和手机号的灵活userId
-- 执行前请备份数据库

-- 1. 确保phone字段存在（如果不存在则添加）
ALTER TABLE users ADD COLUMN IF NOT EXISTS phone VARCHAR(100);

-- 2. 将现有的email数据迁移到phone字段（如果email字段存在）
-- 注意：这里假设email字段还存在，如果已经删除了，可以跳过这步
-- UPDATE users SET phone = email WHERE phone IS NULL AND email IS NOT NULL;

-- 3. 确保user_id字段有唯一约束
ALTER TABLE users ADD UNIQUE INDEX IF NOT EXISTS uk_user_id (user_id);

-- 4. 确保phone字段有唯一约束（但允许为空）
ALTER TABLE users ADD UNIQUE INDEX IF NOT EXISTS uk_phone (phone);

-- 5. 添加name字段（如果不存在）
ALTER TABLE users ADD COLUMN IF NOT EXISTS name VARCHAR(100);

-- 6. 为现有的记录设置name值（如果name为空）
UPDATE users SET name = CONCAT('用户', SUBSTRING(user_id, 1, 8)) WHERE name IS NULL OR name = '';

-- 7. 显示表结构
DESCRIBE users;

-- 8. 显示一些示例数据
SELECT id, user_id, phone, name, type, max_devices FROM users LIMIT 5;

-- 9. 验证数据完整性
SELECT COUNT(*) as total_users FROM users;
SELECT COUNT(*) as users_with_phone FROM users WHERE phone IS NOT NULL;
SELECT COUNT(*) as users_with_name FROM users WHERE name IS NOT NULL; 