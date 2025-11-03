-- 检查应用是否存在
SELECT * FROM apps WHERE app_id = 'RilaLive';

-- 检查所有应用
SELECT app_id, name, is_active, platform FROM apps;

-- 检查升级包
SELECT app_id, version, platform, is_active FROM update_packages WHERE app_id = 'RilaLive'; 