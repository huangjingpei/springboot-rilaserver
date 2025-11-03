-- 升级系统测试数据
-- 执行此脚本前请确保已创建相关表

-- 插入测试应用
INSERT INTO apps (
    app_id, name, description, current_version, min_version, recommended_version,
    developer, developer_email, license, features, changelog, is_active, is_public,
    is_mandatory_update, category, tags, app_type, platform, is_free, is_featured,
    download_count, rating, rating_count, created_at, updated_at
) VALUES 
('test-app-1', '测试应用1', '这是一个测试应用', '1.0.0', '1.0.0', '1.2.0',
'测试开发者', 'dev@test.com', 'MIT', '功能1,功能2', '初始版本', true, true,
false, '工具', '测试,工具', 'TOOL', 'WINDOWS', true, false, 100, 4.5, 20,
NOW(), NOW()),

('test-app-2', '测试应用2', '另一个测试应用', '2.0.0', '2.0.0', '2.1.0',
'测试开发者2', 'dev2@test.com', 'GPL', '功能A,功能B', '版本2.0', true, true,
true, '游戏', '测试,游戏', 'GAME', 'WINDOWS', false, true, 500, 4.8, 50,
NOW(), NOW()),

('mobile-app-1', '移动应用1', '移动端测试应用', '1.5.0', '1.5.0', '1.6.0',
'移动开发者', 'mobile@test.com', 'Apache', '移动功能', '移动版本', true, true,
false, '社交', '移动,社交', 'SOCIAL', 'ANDROID', true, false, 1000, 4.2, 100,
NOW(), NOW());

-- 插入测试升级包
INSERT INTO update_packages (
    app_id, version, platform, file_name, file_path, file_size, file_hash,
    hash_algorithm, release_notes, description, is_active, is_mandatory,
    release_date, created_at, updated_at
) VALUES 
('test-app-1', '1.1.0', 'WINDOWS', 'test-app-1-1.1.0.exe', 'uploads/test-app-1-1.1.0.exe', 1024000,
'abc123def456', 'SHA-256', '修复了一些bug', '版本1.1.0更新', true, false,
NOW(), NOW(), NOW()),

('test-app-1', '1.2.0', 'WINDOWS', 'test-app-1-1.2.0.exe', 'uploads/test-app-1-1.2.0.exe', 1050000,
'def456ghi789', 'SHA-256', '新增功能A和B', '版本1.2.0更新', true, false,
NOW(), NOW(), NOW()),

('test-app-2', '2.1.0', 'WINDOWS', 'test-app-2-2.1.0.exe', 'uploads/test-app-2-2.1.0.exe', 2048000,
'ghi789jkl012', 'SHA-256', '重要安全更新', '版本2.1.0更新', true, true,
NOW(), NOW(), NOW()),

('mobile-app-1', '1.6.0', 'ANDROID', 'mobile-app-1-1.6.0.apk', 'uploads/mobile-app-1-1.6.0.apk', 5120000,
'jkl012mno345', 'SHA-256', '移动端优化', '版本1.6.0更新', true, false,
NOW(), NOW(), NOW());

-- 验证数据
SELECT '应用数据' as info, COUNT(*) as count FROM apps
UNION ALL
SELECT '升级包数据', COUNT(*) FROM update_packages; 