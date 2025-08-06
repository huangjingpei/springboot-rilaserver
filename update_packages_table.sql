-- 升级包表
CREATE TABLE IF NOT EXISTS update_packages (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    version VARCHAR(50) NOT NULL UNIQUE,
    platform VARCHAR(50) NOT NULL,
    file_url VARCHAR(512) NOT NULL,
    file_hash VARCHAR(128) NOT NULL,
    hash_algorithm VARCHAR(20) NOT NULL DEFAULT 'SHA-256',
    release_notes TEXT,
    is_mandatory BOOLEAN NOT NULL DEFAULT FALSE,
    release_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    file_name VARCHAR(100),
    file_size BIGINT NOT NULL,
    file_type VARCHAR(50),
    description VARCHAR(200),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    -- 索引
    INDEX idx_platform_active (platform, is_active),
    INDEX idx_version_platform (version, platform),
    INDEX idx_release_date (release_date),
    INDEX idx_is_active (is_active),
    INDEX idx_is_mandatory (is_mandatory),
    
    -- 约束
    CONSTRAINT uk_version_platform UNIQUE (version, platform)
);

-- 插入示例数据
INSERT INTO update_packages (version, platform, file_url, file_hash, release_notes, is_mandatory, file_name, file_size, file_type, description) VALUES
('1.0.0', 'windows-x64', 'updates/windows-x64/app-1.0.0-windows-x64.zip', 'e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855', '初始版本发布', false, 'app-1.0.0-windows-x64.zip', 10485760, 'application/zip', 'Windows 64位版本'),
('1.0.0', 'macos-x64', 'updates/macos-x64/app-1.0.0-macos-x64.dmg', 'e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b856', '初始版本发布', false, 'app-1.0.0-macos-x64.dmg', 10485760, 'application/x-apple-diskimage', 'macOS 64位版本'),
('1.0.0', 'linux-x64', 'updates/linux-x64/app-1.0.0-linux-x64.tar.gz', 'e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b857', '初始版本发布', false, 'app-1.0.0-linux-x64.tar.gz', 10485760, 'application/gzip', 'Linux 64位版本'),
('1.1.0', 'windows-x64', 'updates/windows-x64/app-1.1.0-windows-x64.zip', 'e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b858', '修复已知问题，提升性能', false, 'app-1.1.0-windows-x64.zip', 10485760, 'application/zip', 'Windows 64位版本 - 性能优化'),
('1.1.0', 'macos-x64', 'updates/macos-x64/app-1.1.0-macos-x64.dmg', 'e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b859', '修复已知问题，提升性能', false, 'app-1.1.0-macos-x64.dmg', 10485760, 'application/x-apple-diskimage', 'macOS 64位版本 - 性能优化'),
('1.1.0', 'linux-x64', 'updates/linux-x64/app-1.1.0-linux-x64.tar.gz', 'e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b860', '修复已知问题，提升性能', false, 'app-1.1.0-linux-x64.tar.gz', 10485760, 'application/gzip', 'Linux 64位版本 - 性能优化'),
('1.2.0', 'windows-x64', 'updates/windows-x64/app-1.2.0-windows-x64.zip', 'e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b861', '新增重要功能，安全更新', true, 'app-1.2.0-windows-x64.zip', 10485760, 'application/zip', 'Windows 64位版本 - 重要更新'),
('1.2.0', 'macos-x64', 'updates/macos-x64/app-1.2.0-macos-x64.dmg', 'e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b862', '新增重要功能，安全更新', true, 'app-1.2.0-macos-x64.dmg', 10485760, 'application/x-apple-diskimage', 'macOS 64位版本 - 重要更新'),
('1.2.0', 'linux-x64', 'updates/linux-x64/app-1.2.0-linux-x64.tar.gz', 'e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b863', '新增重要功能，安全更新', true, 'app-1.2.0-linux-x64.tar.gz', 10485760, 'application/gzip', 'Linux 64位版本 - 重要更新');

-- 插入测试数据（包含你测试的版本1.2.3和平台1）
INSERT INTO update_packages (version, platform, file_url, file_hash, release_notes, is_mandatory, file_name, file_size, file_type, description) VALUES
('1.2.3', '1', 'updates/test-platform/app-1.2.3-test.zip', 'e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b864', '测试版本1.2.3', false, 'app-1.2.3-test.zip', 10485760, 'application/zip', '测试平台版本1.2.3'),
('1.2.3', 'windows-x64', 'updates/windows-x64/app-1.2.3-windows-x64.zip', 'e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b865', '测试版本1.2.3 Windows', false, 'app-1.2.3-windows-x64.zip', 10485760, 'application/zip', 'Windows 64位版本1.2.3'),
('1.2.3', 'macos-x64', 'updates/macos-x64/app-1.2.3-macos-x64.dmg', 'e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b866', '测试版本1.2.3 macOS', false, 'app-1.2.3-macos-x64.dmg', 10485760, 'application/x-apple-diskimage', 'macOS 64位版本1.2.3'),
('1.2.3', 'linux-x64', 'updates/linux-x64/app-1.2.3-linux-x64.tar.gz', 'e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b867', '测试版本1.2.3 Linux', false, 'app-1.2.3-linux-x64.tar.gz', 10485760, 'application/gzip', 'Linux 64位版本1.2.3'); 