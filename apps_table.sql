-- 创建应用表
CREATE TABLE IF NOT EXISTS apps (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    app_id VARCHAR(100) NOT NULL UNIQUE COMMENT '应用唯一标识符',
    name VARCHAR(200) NOT NULL COMMENT '应用显示名称',
    description VARCHAR(500) COMMENT '应用描述',
    current_version VARCHAR(100) COMMENT '当前版本',
    min_version VARCHAR(100) COMMENT '最低支持版本',
    recommended_version VARCHAR(100) COMMENT '推荐版本',
    icon_url VARCHAR(200) COMMENT '应用图标URL',
    website_url VARCHAR(200) COMMENT '应用官网URL',
    developer VARCHAR(100) COMMENT '开发者名称',
    developer_email VARCHAR(200) COMMENT '开发者邮箱',
    license VARCHAR(200) COMMENT '许可证信息',
    features TEXT COMMENT '应用特性（JSON格式）',
    changelog TEXT COMMENT '更新日志',
    is_active BOOLEAN NOT NULL DEFAULT TRUE COMMENT '是否激活',
    is_public BOOLEAN NOT NULL DEFAULT TRUE COMMENT '是否公开',
    is_mandatory_update BOOLEAN NOT NULL DEFAULT FALSE COMMENT '是否强制更新',
    category VARCHAR(50) COMMENT '应用分类',
    tags VARCHAR(50) COMMENT '应用标签（逗号分隔）',
    app_type VARCHAR(20) COMMENT '应用类型',
    platform VARCHAR(20) COMMENT '支持平台',
    is_free BOOLEAN NOT NULL DEFAULT TRUE COMMENT '是否免费',
    is_featured BOOLEAN NOT NULL DEFAULT FALSE COMMENT '是否推荐',
    download_count BIGINT NOT NULL DEFAULT 0 COMMENT '下载次数',
    rating DOUBLE NOT NULL DEFAULT 0.0 COMMENT '评分',
    rating_count BIGINT NOT NULL DEFAULT 0 COMMENT '评分次数',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    last_update_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '最后更新时间',
    
    INDEX idx_app_id (app_id),
    INDEX idx_is_active (is_active),
    INDEX idx_is_public (is_public),
    INDEX idx_category (category),
    INDEX idx_developer (developer),
    INDEX idx_app_type (app_type),
    INDEX idx_platform (platform),
    INDEX idx_is_free (is_free),
    INDEX idx_is_featured (is_featured),
    INDEX idx_download_count (download_count),
    INDEX idx_rating (rating),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='应用信息表';

-- 插入示例应用数据
INSERT INTO apps (app_id, name, description, current_version, min_version, recommended_version, 
                 icon_url, website_url, developer, developer_email, license, category, tags, 
                 app_type, platform, is_free, is_featured, download_count, rating, rating_count,
                 is_active, is_public, is_mandatory_update) VALUES
('rila-live-assistant', 'Rila Live Assistant', '专业的直播助手工具，提供实时直播管理和监控功能', 
 '1.2.3', '1.0.0', '1.2.3', 
 'https://example.com/icons/rila-live-assistant.png', 'https://rila.com/live-assistant', 
 'Rila Team', 'support@rila.com', 'MIT License', 'live-streaming', 'live,streaming,assistant',
 'TOOL', 'WINDOWS', TRUE, TRUE, 1250, 4.5, 89, TRUE, TRUE, FALSE),
 
('rila-stream-manager', 'Rila Stream Manager', '强大的流媒体管理平台，支持多路流媒体处理和分发', 
 '2.1.0', '2.0.0', '2.1.0', 
 'https://example.com/icons/rila-stream-manager.png', 'https://rila.com/stream-manager', 
 'Rila Team', 'support@rila.com', 'MIT License', 'media-processing', 'streaming,media,manager',
 'TOOL', 'CROSS_PLATFORM', TRUE, TRUE, 2100, 4.8, 156, TRUE, TRUE, TRUE),
 
('rila-media-converter', 'Rila Media Converter', '高效的媒体格式转换工具，支持多种音视频格式', 
 '1.5.2', '1.5.0', '1.5.2', 
 'https://example.com/icons/rila-media-converter.png', 'https://rila.com/media-converter', 
 'Rila Team', 'support@rila.com', 'MIT License', 'media-processing', 'converter,media,format',
 'UTILITY', 'WINDOWS', TRUE, FALSE, 890, 4.2, 67, TRUE, TRUE, FALSE),
 
('rila-dashboard', 'Rila Dashboard', '统一的管理控制台，提供系统监控和配置管理', 
 '3.0.1', '3.0.0', '3.0.1', 
 'https://example.com/icons/rila-dashboard.png', 'https://rila.com/dashboard', 
 'Rila Team', 'support@rila.com', 'MIT License', 'management', 'dashboard,management,monitor',
 'TOOL', 'WEB', TRUE, TRUE, 1800, 4.6, 123, TRUE, TRUE, FALSE);

-- 更新现有的update_packages表，添加app_id字段的索引
ALTER TABLE update_packages ADD INDEX idx_app_id (app_id);
ALTER TABLE update_packages ADD INDEX idx_app_id_platform (app_id, platform); 