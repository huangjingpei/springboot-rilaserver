-- 安全功能相关表结构

-- 1. 更新users表，添加安全相关字段
ALTER TABLE users 
ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
ADD COLUMN failed_login_attempts INT DEFAULT 0,
ADD COLUMN account_locked_until DATETIME NULL,
ADD COLUMN password_changed_at DATETIME NULL,
ADD COLUMN last_login_at DATETIME NULL,
ADD COLUMN created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
ADD COLUMN updated_at DATETIME NULL;

-- 2. 创建登录尝试记录表
CREATE TABLE login_attempts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL,
    ip_address VARCHAR(45),
    user_agent TEXT,
    success BOOLEAN NOT NULL,
    attempt_time DATETIME NOT NULL,
    failure_reason VARCHAR(255),
    INDEX idx_user_id (user_id),
    INDEX idx_ip_address (ip_address),
    INDEX idx_attempt_time (attempt_time)
);

-- 3. 创建审计日志表
CREATE TABLE audit_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id VARCHAR(255),
    action VARCHAR(100) NOT NULL,
    resource VARCHAR(100),
    ip_address VARCHAR(45),
    user_agent TEXT,
    details TEXT,
    timestamp DATETIME NOT NULL,
    status VARCHAR(50),
    INDEX idx_user_id (user_id),
    INDEX idx_action (action),
    INDEX idx_timestamp (timestamp),
    INDEX idx_ip_address (ip_address)
);

-- 4. 创建用户设备表
CREATE TABLE user_devices (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL,
    device_id VARCHAR(255) NOT NULL,
    device_name VARCHAR(255),
    device_type VARCHAR(50),
    ip_address VARCHAR(45),
    user_agent TEXT,
    last_login_time DATETIME,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_time DATETIME NOT NULL,
    UNIQUE KEY uk_user_device (user_id, device_id),
    INDEX idx_user_id (user_id),
    INDEX idx_device_id (device_id),
    INDEX idx_is_active (is_active)
);

-- 5. 创建索引优化查询性能
CREATE INDEX idx_users_status ON users(status);
CREATE INDEX idx_users_failed_attempts ON users(failed_login_attempts);
CREATE INDEX idx_users_account_locked ON users(account_locked_until);
CREATE INDEX idx_login_attempts_success_time ON login_attempts(success, attempt_time);
CREATE INDEX idx_audit_logs_user_action ON audit_logs(user_id, action); 