下·-- 代播客户端配置表 (V2)
CREATE TABLE IF NOT EXISTS proxy_client_configs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL UNIQUE COMMENT '关联的用户ID（代播客户端的唯一标识）',
    
    -- 解析源信息
    live_url VARCHAR(1024) COMMENT '原始直播URL',
    
    -- 解析结果
    platform_id VARCHAR(50) COMMENT '平台ID (e.g., douyin)',
    platform_name VARCHAR(100) COMMENT '平台名称 (e.g., 抖音)',
    anchor_name VARCHAR(255) COMMENT '主播名称',
    is_live BOOLEAN DEFAULT FALSE COMMENT '是否正在直播',
    title VARCHAR(500) COMMENT '直播标题',
    quality VARCHAR(50) COMMENT '清晰度',
    
    -- 播放地址
    m3u8_url VARCHAR(2048) COMMENT 'M3U8播放地址',
    flv_url VARCHAR(2048) COMMENT 'FLV播放地址',
    record_url VARCHAR(2048) COMMENT '录播地址',
    
    -- 认证信息
    cookies TEXT COMMENT '更新后的Cookies',
    token TEXT COMMENT '更新后的Token',
    
    -- 额外信息
    extra_data TEXT COMMENT '额外数据 (JSON格式)',
    
    -- 时间戳
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    INDEX idx_user_id (user_id),
    INDEX idx_platform_id (platform_id),
    INDEX idx_anchor_name (anchor_name),
    INDEX idx_is_live (is_live)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='代播客户端配置表';

-- 删除旧的示例数据
TRUNCATE TABLE proxy_client_configs;

-- 插入新的示例数据
INSERT INTO proxy_client_configs (
    user_id, live_url, platform_id, platform_name, anchor_name, is_live, title, quality,
    m3u8_url, flv_url, record_url
) VALUES 
('proxy_user_1@example.com', 'https://live.douyin.com/some_room', 'douyin', '抖音', '东方甄选', 
 TRUE, '美好生活，尽在东方甄选', 'HD', 'http://example.com/playlist.m3u8', 'http://example.com/stream.flv', 'http://example.com/record.m3u8');
