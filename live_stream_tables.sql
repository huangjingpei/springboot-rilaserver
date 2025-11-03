-- 直播间管理系统数据库表设计

-- 1. 直播间配置表
CREATE TABLE IF NOT EXISTS live_stream_configs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    client_id VARCHAR(255) NOT NULL COMMENT '客户端ID（用于区分不同客户端）',
    
    -- 直播间基本信息
    stream_index INT NOT NULL COMMENT '直播间索引（业务无关的自增值）',
    stream_url VARCHAR(1024) NOT NULL COMMENT '直播间地址（重要：不能填错）',
    stream_name VARCHAR(255) NOT NULL COMMENT '直播间名称（用户自定义）',
    
    -- 时间控制
    start_time TIME NOT NULL COMMENT '直播开始时间（服务器开始爬取的时间）',
    end_time TIME NOT NULL COMMENT '直播结束时间（服务器停止爬取的时间）',
    
    -- 平台和质量设置
    platform VARCHAR(50) NOT NULL COMMENT '直播平台（douyin, kuaishou, bilibili等）',
    quality VARCHAR(10) NOT NULL DEFAULT 'HD' COMMENT '视频质量（OD, HD）',
    
    -- 状态字段
    is_active BOOLEAN DEFAULT TRUE COMMENT '是否启用',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    -- 索引
    INDEX idx_client_id (client_id),
    INDEX idx_platform (platform),
    INDEX idx_active (is_active),
    INDEX idx_time_range (start_time, end_time),
    
    -- 唯一约束：同一客户端下的stream_index不能重复
    UNIQUE KEY uk_client_stream (client_id, stream_index)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='直播间配置表';

-- 2. 直播间状态表
CREATE TABLE IF NOT EXISTS live_stream_status (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    config_id BIGINT NOT NULL COMMENT '关联的配置ID',
    client_id VARCHAR(255) NOT NULL COMMENT '客户端ID',
    
    -- Python脚本执行结果
    fetch_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '最后抓取时间',
    is_success BOOLEAN DEFAULT FALSE COMMENT '是否抓取成功',
    error_message TEXT COMMENT '错误信息',
    
    -- 解析结果 - 基本信息
    platform_name VARCHAR(100) COMMENT '平台中文名称',
    anchor_name VARCHAR(255) COMMENT '主播名称',
    is_live BOOLEAN DEFAULT FALSE COMMENT '是否正在直播',
    title VARCHAR(500) COMMENT '直播标题',
    quality VARCHAR(50) COMMENT '实际获取的质量',
    
    -- 解析结果 - 播放地址
    m3u8_url VARCHAR(2048) COMMENT 'M3U8播放地址',
    flv_url VARCHAR(2048) COMMENT 'FLV播放地址', 
    record_url VARCHAR(2048) COMMENT '录播地址',
    
    -- 额外信息
    new_cookies TEXT COMMENT '新的Cookies',
    new_token TEXT COMMENT '新的Token',
    extra_data TEXT COMMENT '额外数据',
    
    -- 状态变更跟踪
    status_changed BOOLEAN DEFAULT FALSE COMMENT '状态是否有变化（用于通知）',
    last_notified_at TIMESTAMP NULL COMMENT '最后通知时间',
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    -- 索引
    INDEX idx_config_id (config_id),
    INDEX idx_client_id (client_id),
    INDEX idx_fetch_time (fetch_time),
    INDEX idx_is_live (is_live),
    INDEX idx_status_changed (status_changed),
    
    -- 外键约束
    FOREIGN KEY (config_id) REFERENCES live_stream_configs(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='直播间状态表';

-- 3. 任务执行日志表
CREATE TABLE IF NOT EXISTS live_stream_task_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    task_id VARCHAR(100) NOT NULL COMMENT '任务ID',
    client_id VARCHAR(255) NOT NULL COMMENT '客户端ID',
    config_id BIGINT COMMENT '配置ID（可为空，批量任务）',
    
    -- 任务信息
    task_type VARCHAR(50) NOT NULL COMMENT '任务类型（FETCH_SINGLE, FETCH_BATCH, EXCEL_UPLOAD）',
    command TEXT COMMENT '执行的Python命令',
    
    -- 执行结果
    start_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '开始时间',
    end_time TIMESTAMP NULL COMMENT '结束时间',
    duration_ms BIGINT COMMENT '执行时长（毫秒）',
    is_success BOOLEAN DEFAULT FALSE COMMENT '是否成功',
    result_data TEXT COMMENT '返回结果数据',
    error_message TEXT COMMENT '错误信息',
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    
    -- 索引
    INDEX idx_task_id (task_id),
    INDEX idx_client_id (client_id),
    INDEX idx_task_type (task_type),
    INDEX idx_start_time (start_time),
    INDEX idx_success (is_success)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='任务执行日志表';

-- 插入示例数据
INSERT INTO live_stream_configs (
    client_id, stream_index, stream_url, stream_name, start_time, end_time, platform, quality
) VALUES 
('client_001', 1, 'https://live.douyin.com/80017709309', '东方甄选直播间', '08:00:00', '22:00:00', 'douyin', 'HD'),
('client_001', 2, 'https://live.kuaishou.com/u/123456', '快手测试直播间', '09:00:00', '21:00:00', 'kuaishou', 'HD'),
('client_002', 1, 'https://live.bilibili.com/12345', 'B站游戏直播', '19:00:00', '23:00:00', 'bilibili', 'HD');

