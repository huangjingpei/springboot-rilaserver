-- 修复stream_info表的status字段
-- 1. 先备份现有数据（可选）
-- CREATE TABLE stream_info_backup AS SELECT * FROM stream_info;

-- 2. 清理现有数据
DELETE FROM stream_info;

-- 3. 修改status字段类型
ALTER TABLE stream_info MODIFY COLUMN status ENUM('CREATED', 'PUSHING', 'PLAYING', 'STOPPED', 'ERROR');

-- 4. 验证修改结果
DESCRIBE stream_info; 