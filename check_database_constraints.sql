-- 检查 update_packages 表的约束
SHOW CREATE TABLE update_packages;

-- 检查唯一约束
SELECT 
    CONSTRAINT_NAME,
    COLUMN_NAME,
    REFERENCED_TABLE_NAME,
    REFERENCED_COLUMN_NAME
FROM information_schema.KEY_COLUMN_USAGE 
WHERE TABLE_NAME = 'update_packages' 
AND TABLE_SCHEMA = DATABASE();

-- 检查索引
SHOW INDEX FROM update_packages;

-- 查看表结构
DESCRIBE update_packages; 