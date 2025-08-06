-- 测试用户数据脚本
-- 注意：密码使用BCrypt加密，原始密码为 'password123'

-- 插入测试用户
INSERT INTO users (user_id, name, password, email, phone, type, max_devices, status, created_at) 
VALUES (
    'test@example.com',
    '测试用户',
    '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDa', -- password123
    'test@example.com',
    '13800138000',
    'register',
    5,
    'ACTIVE',
    NOW()
);

-- 插入另一个测试用户
INSERT INTO users (user_id, name, password, email, phone, type, max_devices, status, created_at) 
VALUES (
    'admin@example.com',
    '管理员',
    '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDa', -- password123
    'admin@example.com',
    '13800138001',
    'auth',
    10,
    'ACTIVE',
    NOW()
);

-- 验证插入结果
SELECT user_id, name, type, status, created_at FROM users WHERE user_id IN ('test@example.com', 'admin@example.com'); 