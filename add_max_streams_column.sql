-- 添加推流数量限制字段到users表
-- 为users表添加max_streams字段

-- 1. 添加max_streams列
ALTER TABLE users ADD COLUMN max_streams INT DEFAULT 1 COMMENT '最大推流数量';

-- 2. 根据用户类型设置默认的max_streams值
UPDATE users SET max_streams = CASE 
    WHEN type = 'register' THEN 1
    WHEN type = 'auth' THEN 5  
    WHEN type = 'enterprise' THEN 20
    ELSE 1
END;

-- 3. 设置非空约束
ALTER TABLE users MODIFY COLUMN max_streams INT NOT NULL DEFAULT 1;

-- 4. 验证更新结果
SELECT type, COUNT(*) as count, AVG(max_streams) as avg_max_streams 
FROM users 
GROUP BY type;

-- 5. 显示表结构确认
DESCRIBE users; 