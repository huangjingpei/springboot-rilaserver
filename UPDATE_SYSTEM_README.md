# 软件升级系统

这是一个基于Spring Boot的完整软件升级系统，支持多平台、安全验证、版本管理等企业级功能。

## 功能特性

### 🔒 安全性
- **文件完整性验证**：使用SHA-256哈希值确保升级包不被篡改
- **权限控制**：管理员权限验证，防止未授权访问
- **版本号验证**：严格的语义化版本号格式检查
- **文件类型限制**：只允许安全的文件类型上传

### 🌐 多平台支持
- **Windows**：支持.exe、.msi、.zip格式
- **macOS**：支持.dmg、.pkg格式  
- **Linux**：支持.deb、.rpm、.tar.gz格式
- **ARM架构**：支持ARM64平台

### 📦 版本管理
- **语义化版本**：遵循SemVer标准（如1.2.3）
- **强制更新**：支持标记必须更新的版本
- **发布说明**：详细的更新日志
- **版本比较**：智能的版本号比较算法

### 🚀 高性能
- **静态文件服务**：使用Nginx提供文件下载
- **CDN支持**：可集成CDN加速下载
- **分页查询**：支持大量数据的分页显示
- **缓存友好**：支持HTTP缓存头

## 系统架构

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   客户端应用     │    │   Spring Boot   │    │   文件存储      │
│                │    │   升级服务器     │    │                │
│ - 检查更新      │◄──►│ - API接口       │◄──►│ - 本地文件系统  │
│ - 下载升级包    │    │ - 版本管理      │    │ - Nginx静态服务 │
│ - 验证完整性    │    │ - 安全验证      │    │ - 对象存储(可选)│
└─────────────────┘    └─────────────────┘    └─────────────────┘
                              │
                              ▼
                       ┌─────────────────┐
                       │   数据库        │
                       │                │
                       │ - 版本信息      │
                       │ - 文件元数据    │
                       │ - 发布记录      │
                       └─────────────────┘
```

## API接口

### 客户端接口

#### 1. 检查最新版本
```http
GET /api/v1/updates/latest?currentVersion=1.0.0&platform=windows-x64
```

**响应示例：**
```json
{
  "hasUpdate": true,
  "latestVersion": "1.2.0",
  "releaseNotes": "新增重要功能，安全更新",
  "isMandatory": true,
  "releaseDate": "2023-10-27T10:00:00Z",
  "downloadUrl": "/api/v1/updates/download/1.2.0?platform=windows-x64",
  "fileHash": "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b861",
  "hashAlgorithm": "SHA-256",
  "fileSize": 10485760,
  "fileName": "app-1.2.0-windows-x64.zip",
  "description": "Windows 64位版本 - 重要更新"
}
```

#### 2. 下载升级包
```http
GET /api/v1/updates/download/1.2.0?platform=windows-x64
```
返回302重定向到实际文件位置

#### 3. 直接下载文件
```http
GET /api/v1/updates/file/1.2.0?platform=windows-x64
```
直接返回文件内容

#### 4. 获取强制更新
```http
GET /api/v1/updates/mandatory/windows-x64
```

#### 5. 版本号验证
```http
GET /api/v1/updates/validate-version?version=1.2.3
```

#### 6. 版本比较
```http
GET /api/v1/updates/compare-versions?version1=1.0.0&version2=1.1.0
```

### 管理员接口

#### 1. 上传升级包
```http
POST /api/v1/updates
Content-Type: multipart/form-data

file: [升级包文件]
version: 1.2.0
platform: windows-x64
releaseNotes: 新增重要功能
isMandatory: true
description: Windows 64位版本
```

#### 2. 获取升级包列表
```http
GET /api/v1/updates?page=0&size=20&sortBy=releaseDate&sortDir=desc
```

#### 3. 根据平台获取升级包
```http
GET /api/v1/updates/platform/windows-x64?page=0&size=20
```

#### 4. 更新升级包信息
```http
PUT /api/v1/updates/{id}
Content-Type: application/json

{
  "releaseNotes": "更新的发布说明",
  "isMandatory": false,
  "description": "更新的描述"
}
```

#### 5. 删除升级包
```http
DELETE /api/v1/updates/{id}
```

#### 6. 激活/停用升级包
```http
PATCH /api/v1/updates/{id}/active?isActive=true
```

#### 7. 获取支持平台
```http
GET /api/v1/updates/platforms
```

## 配置说明

### application.properties
```properties
# 升级系统基本配置
app.update.download-url-prefix=/api/v1/updates/download
app.update.hash-algorithm=SHA-256

# 文件存储配置 - 本地文件系统
app.file-storage.local.base-path=uploads
app.file-storage.local.base-url=http://localhost:8080/files
app.file-storage.local.max-file-size=104857600
app.file-storage.local.allowed-extensions=zip,exe,msi,dmg,pkg,deb,rpm,tar.gz

# 文件上传配置
spring.servlet.multipart.max-file-size=100MB
spring.servlet.multipart.max-request-size=100MB
spring.servlet.multipart.enabled=true

# 静态资源映射
spring.web.resources.static-locations=classpath:/static/,file:uploads/
spring.mvc.static-path-pattern=/files/**
```

### Nginx配置示例
```nginx
server {
    listen 80;
    server_name your-domain.com;
    
    # 静态文件服务
    location /files/ {
        alias /path/to/your/uploads/;
        expires 1d;
        add_header Cache-Control "public, immutable";
        
        # 安全头
        add_header X-Content-Type-Options nosniff;
        add_header X-Frame-Options DENY;
        add_header X-XSS-Protection "1; mode=block";
    }
    
    # API代理
    location /api/ {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

## 数据库设计

### update_packages表
```sql
CREATE TABLE update_packages (
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
    
    INDEX idx_platform_active (platform, is_active),
    INDEX idx_version_platform (version, platform),
    INDEX idx_release_date (release_date),
    CONSTRAINT uk_version_platform UNIQUE (version, platform)
);
```

## 客户端集成示例

### Java客户端
```java
@Service
public class UpdateClient {
    
    @Value("${app.update.server-url}")
    private String serverUrl;
    
    public UpdateCheckResponse checkForUpdate(String currentVersion, String platform) {
        String url = String.format("%s/api/v1/updates/latest?currentVersion=%s&platform=%s",
            serverUrl, currentVersion, platform);
        
        RestTemplate restTemplate = new RestTemplate();
        return restTemplate.getForObject(url, UpdateCheckResponse.class);
    }
    
    public void downloadUpdate(String version, String platform, String targetPath) {
        String downloadUrl = String.format("%s/api/v1/updates/download/%s?platform=%s",
            serverUrl, version, platform);
        
        // 下载文件并验证哈希值
        // ... 实现下载逻辑
    }
}
```

### JavaScript客户端
```javascript
class UpdateClient {
    constructor(serverUrl) {
        this.serverUrl = serverUrl;
    }
    
    async checkForUpdate(currentVersion, platform) {
        const response = await fetch(
            `${this.serverUrl}/api/v1/updates/latest?currentVersion=${currentVersion}&platform=${platform}`
        );
        return await response.json();
    }
    
    async downloadUpdate(version, platform) {
        const response = await fetch(
            `${this.serverUrl}/api/v1/updates/download/${version}?platform=${platform}`
        );
        
        if (response.redirected) {
            // 跟随重定向下载文件
            return await fetch(response.url);
        }
        
        return response;
    }
}
```

## 安全最佳实践

### 1. 文件安全
- 使用HTTPS传输
- 验证文件哈希值
- 限制文件类型和大小
- 扫描恶意软件

### 2. 访问控制
- 管理员权限验证
- API访问频率限制
- 请求日志记录
- 敏感操作审计

### 3. 数据保护
- 数据库连接加密
- 敏感信息脱敏
- 定期备份数据
- 访问日志保留

## 部署指南

### 1. 环境准备
```bash
# 创建上传目录
mkdir -p uploads/updates

# 设置权限
chmod 755 uploads
chown -R app:app uploads
```

### 2. 数据库初始化
```bash
# 执行SQL脚本
mysql -u root -p your_database < update_packages_table.sql
```

### 3. 应用启动
```bash
# 使用Maven启动
mvn spring-boot:run

# 或打包后启动
mvn clean package
java -jar target/registration-login-0.0.1-SNAPSHOT.jar
```

### 4. Nginx配置
```bash
# 复制配置文件
cp nginx.conf /etc/nginx/sites-available/update-server

# 启用站点
ln -s /etc/nginx/sites-available/update-server /etc/nginx/sites-enabled/

# 重启Nginx
systemctl restart nginx
```

## 测试

访问测试页面：`http://localhost:8080/update-test.html`

测试功能包括：
- 检查更新
- 版本比较
- 版本号验证
- 强制更新查询
- 升级包管理

## 故障排除

### 常见问题

1. **文件上传失败**
   - 检查文件大小限制
   - 验证文件类型
   - 确认目录权限

2. **下载链接失效**
   - 检查Nginx配置
   - 验证文件路径
   - 确认静态资源映射

3. **版本比较错误**
   - 检查版本号格式
   - 验证语义化版本规范
   - 查看错误日志

4. **权限访问被拒绝**
   - 确认用户角色
   - 检查Spring Security配置
   - 验证JWT令牌

### 日志查看
```bash
# 应用日志
tail -f logs/spring.log

# Nginx访问日志
tail -f /var/log/nginx/access.log

# Nginx错误日志
tail -f /var/log/nginx/error.log
```

## 扩展功能

### 1. 对象存储集成
支持AWS S3、阿里云OSS等对象存储服务

### 2. CDN加速
集成CDN服务提升下载速度

### 3. 增量更新
支持增量升级包，减少下载量

### 4. 自动更新
客户端自动检查和下载更新

### 5. 回滚功能
支持版本回滚到之前的版本

## 许可证

本项目采用MIT许可证，详见LICENSE文件。 