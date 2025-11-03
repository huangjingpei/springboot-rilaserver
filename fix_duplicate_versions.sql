-- 检查重复的版本记录
SELECT 
    app_id,
    version,
    platform,
    COUNT(*) as count,
    GROUP_CONCAT(id) as ids
FROM update_packages 
GROUP BY app_id, version, platform
HAVING COUNT(*) > 1;

-- 查看 RilaLive 的所有版本记录
SELECT 
    id,
    app_id,
    version,
    platform,
    file_name,
    is_active,
    created_at
FROM update_packages 
WHERE app_id = 'RilaLive'
ORDER BY version, created_at DESC;

-- 删除重复记录（保留最新的）
DELETE up1 FROM update_packages up1
INNER JOIN update_packages up2 
WHERE up1.id < up2.id 
AND up1.app_id = up2.app_id 
AND up1.version = up2.version 
AND up1.platform = up2.platform;

-- 验证修复结果
SELECT 
    app_id,
    version,
    platform,
    COUNT(*) as count
FROM update_packages 
GROUP BY app_id, version, platform
HAVING COUNT(*) > 1; 