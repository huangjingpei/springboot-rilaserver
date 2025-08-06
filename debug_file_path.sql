-- 查看数据库中的文件URL
SELECT id, version, platform, file_url, file_name FROM update_packages WHERE version = '1.2.3' AND platform = '1'; 