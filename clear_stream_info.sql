-- 清理stream_info表中的现有数据
DELETE FROM stream_info;

-- 或者只清理有问题的status数据
-- DELETE FROM stream_info WHERE status NOT IN ('CREATED', 'PUSHING', 'PLAYING', 'STOPPED', 'ERROR'); 