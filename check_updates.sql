-- 检查 RilaLive 的升级包
SELECT 
    id,
    app_id,
    version,
    platform,
    file_name,
    is_active,
    is_mandatory,
    release_date,
    created_at
FROM update_packages 
WHERE app_id = 'RilaLive'
ORDER BY release_date DESC;

-- 检查所有升级包
SELECT 
    app_id,
    version,
    platform,
    is_active,
    release_date
FROM update_packages 
ORDER BY app_id, release_date DESC; 