-- 数据库迁移脚本：将email字段改为phone字段
-- 执行前请备份数据库

-- 1. 添加phone字段（如果不存在）
ALTER TABLE users ADD COLUMN IF NOT EXISTS phone VARCHAR(20);

-- 2. 将现有的email数据迁移到phone字段
UPDATE users SET phone = email WHERE phone IS NULL AND email IS NOT NULL;

-- 3. 删除email字段（如果存在）
ALTER TABLE users DROP COLUMN IF EXISTS email;

-- 4. 确保phone字段有唯一约束
ALTER TABLE users ADD UNIQUE INDEX IF NOT EXISTS uk_phone (phone);

-- 5. 确保user_id字段有唯一约束
ALTER TABLE users ADD UNIQUE INDEX IF NOT EXISTS uk_user_id (user_id);

-- 6. 显示表结构
DESCRIBE users;

-- 7. 显示一些示例数据
SELECT id, user_id, phone, name, type, max_devices FROM users LIMIT 5;

-- 8. 验证数据完整性
SELECT COUNT(*) as total_users FROM users;
SELECT COUNT(*) as users_with_phone FROM users WHERE phone IS NOT NULL; 