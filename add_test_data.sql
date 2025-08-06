-- 添加测试数据（包含版本1.2.3和平台1）
-- 如果数据已存在，先删除
DELETE FROM update_packages WHERE version = '1.2.3' AND platform = '1';

-- 插入测试数据
INSERT INTO update_packages (version, platform, file_url, file_hash, release_notes, is_mandatory, file_name, file_size, file_type, description) VALUES
('1.2.3', '1', 'updates/test-platform/app-1.2.3-test.zip', 'e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b864', '测试版本1.2.3', false, 'app-1.2.3-test.zip', 10485760, 'application/zip', '测试平台版本1.2.3');

-- 验证数据是否插入成功
SELECT * FROM update_packages WHERE version = '1.2.3' AND platform = '1'; 