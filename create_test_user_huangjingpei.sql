-- 创建测试用户 huangjingpei@gmail.com
-- 密码: 123456 (BCrypt加密)

-- 首先检查用户是否已存在
SELECT 'Checking if user exists...' as status;
SELECT user_id, name, type, status, created_at 
FROM users 
WHERE user_id = 'huangjingpei@gmail.com';

-- 如果用户不存在，插入新用户
-- 注意：密码 '123456' 的BCrypt加密值
INSERT IGNORE INTO users (user_id, name, password, phone, type, max_devices, max_streams, status, created_at) 
VALUES (
    'huangjingpei@gmail.com',
    '黄景培',
    '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', -- 123456
    'huangjingpei@gmail.com',
    'register',
    5,
    1,
    'ACTIVE',
    NOW()
);

-- 验证插入结果
SELECT 'User creation result:' as status;
SELECT user_id, name, type, status, max_devices, max_streams, created_at 
FROM users 
WHERE user_id = 'huangjingpei@gmail.com';

-- 重置失败登录次数（如果有的话）
UPDATE users 
SET failed_login_attempts = 0, 
    account_locked_until = NULL 
WHERE user_id = 'huangjingpei@gmail.com';

SELECT 'User setup completed!' as status;