-- 应用商城数据库表结构

-- 应用表
CREATE TABLE IF NOT EXISTS apps (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    description TEXT,
    short_description VARCHAR(500),
    app_icon VARCHAR(500),
    rating DECIMAL(3,2) DEFAULT 0.00,
    rating_count INT DEFAULT 0,
    price DECIMAL(10,2) DEFAULT 0.00,
    original_price DECIMAL(10,2),
    type VARCHAR(50),
    download_count BIGINT DEFAULT 0,
    file_size VARCHAR(50),
    version VARCHAR(50),
    developer VARCHAR(255),
    release_date DATETIME,
    last_updated DATETIME,
    is_featured BOOLEAN DEFAULT FALSE,
    is_active BOOLEAN DEFAULT TRUE,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_name (name),
    INDEX idx_type (type),
    INDEX idx_developer (developer),
    INDEX idx_rating (rating),
    INDEX idx_price (price),
    INDEX idx_download_count (download_count),
    INDEX idx_is_featured (is_featured),
    INDEX idx_is_active (is_active),
    INDEX idx_created_at (created_at)
);

-- 应用截图表
CREATE TABLE IF NOT EXISTS app_screenshots (
    app_id BIGINT NOT NULL,
    screenshot_url VARCHAR(500) NOT NULL,
    PRIMARY KEY (app_id, screenshot_url),
    FOREIGN KEY (app_id) REFERENCES apps(id) ON DELETE CASCADE
);

-- 应用平台表
CREATE TABLE IF NOT EXISTS app_platforms (
    app_id BIGINT NOT NULL,
    platform VARCHAR(50) NOT NULL,
    PRIMARY KEY (app_id, platform),
    FOREIGN KEY (app_id) REFERENCES apps(id) ON DELETE CASCADE
);

-- 应用下载表
CREATE TABLE IF NOT EXISTS app_downloads (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    app_id BIGINT NOT NULL,
    platform VARCHAR(50) NOT NULL,
    download_url VARCHAR(500) NOT NULL,
    file_size VARCHAR(50),
    version VARCHAR(50),
    minimum_os_version VARCHAR(50),
    is_active BOOLEAN DEFAULT TRUE,
    download_count BIGINT DEFAULT 0,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (app_id) REFERENCES apps(id) ON DELETE CASCADE,
    UNIQUE KEY uk_app_platform (app_id, platform),
    INDEX idx_platform (platform),
    INDEX idx_is_active (is_active),
    INDEX idx_download_count (download_count)
);

-- 应用评论表
CREATE TABLE IF NOT EXISTS app_reviews (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    app_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    rating DECIMAL(2,1) NOT NULL,
    comment TEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_helpful BOOLEAN DEFAULT FALSE,
    helpful_count INT DEFAULT 0,
    is_verified_purchase BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (app_id) REFERENCES apps(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE KEY uk_app_user (app_id, user_id),
    INDEX idx_app_id (app_id),
    INDEX idx_user_id (user_id),
    INDEX idx_rating (rating),
    INDEX idx_created_at (created_at),
    INDEX idx_is_helpful (is_helpful),
    INDEX idx_helpful_count (helpful_count)
);

-- 插入示例数据

-- 插入应用类型枚举值
INSERT INTO apps (name, description, short_description, app_icon, type, price, developer, is_featured, is_active) VALUES
('微信', '腾讯微信是一款跨平台的通讯工具。支持单人、多人参与。通过手机网络发送语音、图片、视频和文字。', '跨平台通讯工具', 'https://example.com/wechat-icon.png', 'SOCIAL', 0.00, '腾讯', TRUE, TRUE),
('支付宝', '支付宝是全球领先的独立第三方支付平台，致力于为广大用户提供安全快速的电子支付/网上支付/安全支付/手机支付体验，及转账收款/水电煤缴费/信用卡还款/AA收款等生活服务应用。', '移动支付平台', 'https://example.com/alipay-icon.png', 'FINANCE', 0.00, '蚂蚁集团', TRUE, TRUE),
('王者荣耀', '《王者荣耀》是腾讯第一5V5团队公平竞技手游，国民MOBA手游大作！5V5王者峡谷、公平对战、还原MOBA经典体验；契约之战、五军对决、边境突围等，带来花式作战乐趣！', '5V5团队竞技手游', 'https://example.com/honor-icon.png', 'GAME', 0.00, '腾讯游戏', TRUE, TRUE),
('抖音', '抖音，记录美好生活！抖音是一个帮助用户表达自我，记录美好生活的短视频平台。', '短视频社交平台', 'https://example.com/douyin-icon.png', 'SOCIAL', 0.00, '字节跳动', TRUE, TRUE),
('网易云音乐', '网易云音乐是一款专注于发现与分享的音乐产品，依托专业音乐人、DJ、好友推荐及社交功能，为用户打造全新的音乐生活。', '音乐播放器', 'https://example.com/netease-icon.png', 'MUSIC', 0.00, '网易', FALSE, TRUE),
('剪映', '剪映是一款视频编辑工具，具有全面的剪辑功能，支持变速，有多样滤镜和美颜的效果，有丰富的曲库资源。', '视频编辑工具', 'https://example.com/jianying-icon.png', 'VIDEO', 0.00, '字节跳动', FALSE, TRUE),
('美团', '美团是中国领先的生活服务电子商务平台，公司拥有美团、大众点评、美团外卖等消费者熟知的App，服务涵盖餐饮、外卖、生鲜零售、打车、共享单车、酒店旅游、电影、休闲娱乐等200多个品类。', '生活服务平台', 'https://example.com/meituan-icon.png', 'SHOPPING', 0.00, '美团', FALSE, TRUE),
('高德地图', '高德地图是中国领先的数字地图内容、导航和位置服务解决方案提供商。', '地图导航应用', 'https://example.com/amap-icon.png', 'TOOL', 0.00, '阿里巴巴', FALSE, TRUE),
('小红书', '小红书是一个生活方式平台和消费决策入口，创始人为毛文超和瞿芳。在小红书，用户通过文字、图片、视频笔记的分享，记录了这个时代年轻人的正能量和美好生活。', '生活方式平台', 'https://example.com/xiaohongshu-icon.png', 'SOCIAL', 0.00, '小红书', FALSE, TRUE),
('哔哩哔哩', '哔哩哔哩（bilibili）现为国内领先的年轻人文化社区，该网站于2009年6月26日创建，被粉丝们亲切的称为"B站"。', '视频弹幕网站', 'https://example.com/bilibili-icon.png', 'VIDEO', 0.00, '哔哩哔哩', FALSE, TRUE);

-- 插入应用平台数据
INSERT INTO app_platforms (app_id, platform) VALUES
(1, 'ANDROID'), (1, 'IOS'),
(2, 'ANDROID'), (2, 'IOS'),
(3, 'ANDROID'), (3, 'IOS'),
(4, 'ANDROID'), (4, 'IOS'),
(5, 'ANDROID'), (5, 'IOS'),
(6, 'ANDROID'), (6, 'IOS'),
(7, 'ANDROID'), (7, 'IOS'),
(8, 'ANDROID'), (8, 'IOS'),
(9, 'ANDROID'), (9, 'IOS'),
(10, 'ANDROID'), (10, 'IOS');

-- 插入应用截图数据
INSERT INTO app_screenshots (app_id, screenshot_url) VALUES
(1, 'https://example.com/wechat-screenshot1.png'),
(1, 'https://example.com/wechat-screenshot2.png'),
(2, 'https://example.com/alipay-screenshot1.png'),
(2, 'https://example.com/alipay-screenshot2.png'),
(3, 'https://example.com/honor-screenshot1.png'),
(3, 'https://example.com/honor-screenshot2.png');

-- 插入应用下载链接
INSERT INTO app_downloads (app_id, platform, download_url, file_size, version) VALUES
(1, 'ANDROID', 'https://play.google.com/store/apps/details?id=com.tencent.mm', '150MB', '8.0.0'),
(1, 'IOS', 'https://apps.apple.com/app/wechat/id414478124', '200MB', '8.0.0'),
(2, 'ANDROID', 'https://play.google.com/store/apps/details?id=com.eg.android.AlipayGphone', '120MB', '10.2.0'),
(2, 'IOS', 'https://apps.apple.com/app/alipay/id333206289', '150MB', '10.2.0'),
(3, 'ANDROID', 'https://play.google.com/store/apps/details?id=com.tencent.tmgp.sgame', '2.5GB', '1.70.0'),
(3, 'IOS', 'https://apps.apple.com/app/王者荣耀/id989673964', '3.0GB', '1.70.0');

-- 更新应用评分和下载次数
UPDATE apps SET 
    rating = 4.5, rating_count = 1000000, download_count = 10000000
WHERE id = 1;

UPDATE apps SET 
    rating = 4.3, rating_count = 800000, download_count = 8000000
WHERE id = 2;

UPDATE apps SET 
    rating = 4.2, rating_count = 500000, download_count = 5000000
WHERE id = 3;

UPDATE apps SET 
    rating = 4.4, rating_count = 1200000, download_count = 12000000
WHERE id = 4;

UPDATE apps SET 
    rating = 4.1, rating_count = 600000, download_count = 6000000
WHERE id = 5; 