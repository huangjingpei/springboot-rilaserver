-- 检查数据库中的升级包数据

-- 1. 查看所有升级包
SELECT * FROM update_packages ORDER BY platform, release_date DESC;

-- 2. 查看平台为'1'的升级包
SELECT * FROM update_packages WHERE platform = '1' ORDER BY release_date DESC;

-- 3. 查看平台为'1'且激活的升级包
SELECT * FROM update_packages WHERE platform = '1' AND is_active = true ORDER BY release_date DESC;

-- 4. 查看平台为'windows-x64'的升级包
SELECT * FROM update_packages WHERE platform = 'windows-x64' ORDER BY release_date DESC;

-- 5. 查看所有平台
SELECT DISTINCT platform FROM update_packages;

-- 6. 查看所有版本号
SELECT DISTINCT version FROM update_packages ORDER BY version;

-- 7. 查看强制更新的版本
SELECT * FROM update_packages WHERE is_mandatory = true AND is_active = true ORDER BY platform, release_date DESC; 