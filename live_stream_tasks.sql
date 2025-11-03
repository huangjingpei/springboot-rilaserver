-- 直播间任务管理表
CREATE TABLE IF NOT EXISTS live_stream_tasks (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    client_id VARCHAR(255) NOT NULL COMMENT '客户端ID，用于区分不同客户端',
    task_index INT NOT NULL COMMENT '直播间索引（自增，业务无意义）',
    live_url VARCHAR(1024) NOT NULL COMMENT '直播间地址',
    room_name VARCHAR(255) COMMENT '直播间名称（自定义）',
    platform VARCHAR(50) COMMENT '直播平台（douyin, kuaishou, bilibili等）',
    quality VARCHAR(20) DEFAULT 'HD' COMMENT '清晰度（HD|OD）',
    start_time TIME COMMENT '直播开始时间',
    end_time TIME COMMENT '直播结束时间',
    is_active BOOLEAN DEFAULT TRUE COMMENT '是否激活',
    last_check_time TIMESTAMP NULL COMMENT '最后检查时间',
    next_check_time TIMESTAMP NULL COMMENT '下次检查时间',
    check_interval INT DEFAULT 60 COMMENT '检查间隔（秒）',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    INDEX idx_client_id (client_id),
    INDEX idx_is_active (is_active),
    INDEX idx_next_check_time (next_check_time),
    INDEX idx_platform (platform),
    UNIQUE KEY uk_client_task (client_id, task_index)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='直播间任务管理表';

-- 流状态记录表
CREATE TABLE IF NOT EXISTS live_stream_status (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    task_id BIGINT NOT NULL COMMENT '关联的任务ID',
    client_id VARCHAR(255) NOT NULL COMMENT '客户端ID',
    platform VARCHAR(50) COMMENT '平台名称',
    anchor_name VARCHAR(255) COMMENT '主播名称',
    is_live BOOLEAN DEFAULT FALSE COMMENT '是否正在直播',
    title VARCHAR(500) COMMENT '直播标题',
    quality VARCHAR(20) COMMENT '清晰度',
    m3u8_url VARCHAR(2048) COMMENT 'M3U8播放地址',
    flv_url VARCHAR(2048) COMMENT 'FLV播放地址',
    record_url VARCHAR(2048) COMMENT '录播地址',
    cookies TEXT COMMENT 'Cookies信息',
    token TEXT COMMENT 'Token信息',
    extra_data TEXT COMMENT '额外数据（JSON格式）',
    status_code INT DEFAULT 0 COMMENT '状态码（0=成功，其他=失败）',
    error_message TEXT COMMENT '错误信息',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    
    INDEX idx_task_id (task_id),
    INDEX idx_client_id (client_id),
    INDEX idx_is_live (is_live),
    INDEX idx_created_at (created_at),
    FOREIGN KEY (task_id) REFERENCES live_stream_tasks(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='流状态记录表';

-- 任务执行日志表
CREATE TABLE IF NOT EXISTS live_stream_task_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    task_id BIGINT NOT NULL COMMENT '关联的任务ID',
    client_id VARCHAR(255) NOT NULL COMMENT '客户端ID',
    operation_type VARCHAR(50) NOT NULL COMMENT '操作类型（CREATE|UPDATE|DELETE|CHECK）',
    status VARCHAR(20) NOT NULL COMMENT '执行状态（SUCCESS|FAILED|RUNNING）',
    message TEXT COMMENT '执行消息',
    execution_time_ms BIGINT COMMENT '执行时间（毫秒）',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    
    INDEX idx_task_id (task_id),
    INDEX idx_client_id (client_id),
    INDEX idx_operation_type (operation_type),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at),
    FOREIGN KEY (task_id) REFERENCES live_stream_tasks(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='任务执行日志表';

-- 插入示例数据
INSERT INTO live_stream_tasks (
    client_id, task_index, live_url, room_name, platform, quality, start_time, end_time
) VALUES 
('client_001', 1, 'https://live.douyin.com/80017709309', '东方甄选直播间', 'douyin', 'HD', '09:00:00', '22:00:00'),
('client_001', 2, 'https://live.kuaishou.com/u/example', '快手直播间', 'kuaishou', 'HD', '10:00:00', '23:00:00'),
('client_002', 1, 'https://live.bilibili.com/123456', 'B站直播间', 'bilibili', 'HD', '08:00:00', '21:00:00');
