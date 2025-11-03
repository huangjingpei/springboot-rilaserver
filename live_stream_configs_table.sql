-- 直播间配置表
CREATE TABLE IF NOT EXISTS live_stream_configs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '直播间索引（自增）',
    client_id VARCHAR(255) NOT NULL COMMENT '客户端ID（用于区分不同客户端）',
    live_url VARCHAR(1024) NOT NULL COMMENT '直播间地址',
    stream_name VARCHAR(255) NOT NULL COMMENT '直播间名称（自定义）',
    start_time TIME NOT NULL COMMENT '直播开始时间',
    end_time TIME NOT NULL COMMENT '直播结束时间',
    platform VARCHAR(50) NOT NULL COMMENT '直播平台（douyin, kuaishou, bilibili等）',
    quality VARCHAR(20) DEFAULT 'HD' COMMENT '清晰度（HD, OD等）',
    
    -- 当前状态
    is_active BOOLEAN DEFAULT TRUE COMMENT '是否启用',
    is_live BOOLEAN DEFAULT FALSE COMMENT '是否正在直播',
    last_check_time TIMESTAMP NULL COMMENT '最后检查时间',
    last_update_time TIMESTAMP NULL COMMENT '最后更新时间',
    
    -- 解析结果
    anchor_name VARCHAR(255) COMMENT '主播名称',
    title VARCHAR(500) COMMENT '直播标题',
    m3u8_url VARCHAR(2048) COMMENT 'M3U8播放地址',
    flv_url VARCHAR(2048) COMMENT 'FLV播放地址',
    record_url VARCHAR(2048) COMMENT '录播地址',
    cookies TEXT COMMENT 'Cookies信息',
    token TEXT COMMENT 'Token信息',
    extra_data TEXT COMMENT '额外数据（JSON格式）',
    
    -- 错误信息
    error_message TEXT COMMENT '错误信息',
    retry_count INT DEFAULT 0 COMMENT '重试次数',
    
    -- 时间戳
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    INDEX idx_client_id (client_id),
    INDEX idx_platform (platform),
    INDEX idx_is_active (is_active),
    INDEX idx_is_live (is_live),
    INDEX idx_start_time (start_time),
    INDEX idx_end_time (end_time),
    INDEX idx_last_check_time (last_check_time),
    UNIQUE KEY uk_client_url (client_id, live_url)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='直播间配置表';

-- 直播间状态变更日志表
CREATE TABLE IF NOT EXISTS live_stream_status_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    config_id BIGINT NOT NULL COMMENT '关联的配置ID',
    client_id VARCHAR(255) NOT NULL COMMENT '客户端ID',
    old_status BOOLEAN COMMENT '之前状态',
    new_status BOOLEAN COMMENT '新状态',
    change_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '变更时间',
    change_reason VARCHAR(255) COMMENT '变更原因',
    
    INDEX idx_config_id (config_id),
    INDEX idx_client_id (client_id),
    INDEX idx_change_time (change_time),
    
    FOREIGN KEY (config_id) REFERENCES live_stream_configs(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='直播间状态变更日志表';

-- 插入示例数据
INSERT INTO live_stream_configs (
    client_id, live_url, stream_name, start_time, end_time, platform, quality
) VALUES 
('client_001', 'https://live.douyin.com/80017709309', '东方甄选直播间', '09:00:00', '22:00:00', 'douyin', 'HD'),
('client_001', 'https://live.kuaishou.com/u/example', '快手测试直播间', '10:00:00', '21:00:00', 'kuaishou', 'HD'),
('client_002', 'https://live.bilibili.com/123456', 'B站测试直播间', '08:00:00', '23:00:00', 'bilibili', 'HD');

