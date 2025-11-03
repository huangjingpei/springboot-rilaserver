-- 创建 RilaLive 应用记录
INSERT INTO apps (
    app_id, name, description, current_version, min_version, recommended_version,
    developer, developer_email, license, features, changelog, is_active, is_public,
    is_mandatory_update, category, tags, app_type, platform, is_free, is_featured,
    download_count, rating, rating_count, created_at, updated_at
) VALUES 
('RilaLive', 'RilaLive', 'RilaLive 应用', '1.9.6', '1.0.0', '1.9.6',
'RilaLive Team', 'dev@rilalive.com', 'Commercial', '直播功能,实时通信', '最新版本', true, true,
false, '社交', '直播,社交', 'SOCIAL', 'WINDOWS', false, true, 1000, 4.8, 200,
NOW(), NOW());

-- 验证插入结果
SELECT * FROM apps WHERE app_id = 'RilaLive'; 