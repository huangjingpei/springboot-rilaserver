# 本地开发环境指南

## 概述
本文档提供了在本地开发环境中运行登录注册系统的完整指南。

## 环境要求

### 必需软件
- Java 17+
- Maven 3.6+
- MySQL 8.0+
- Redis 6.0+

### 推荐开发工具
- IntelliJ IDEA 或 Eclipse
- Postman 或类似API测试工具
- MySQL Workbench 或 Navicat

## 快速开始

### 1. 克隆项目
```bash
git clone <your-repository-url>
cd rilaserver
```

### 2. 配置数据库
```sql
-- 创建数据库
CREATE DATABASE login_system CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 创建用户（可选）
CREATE USER 'dev_user'@'localhost' IDENTIFIED BY 'dev_password';
GRANT ALL PRIVILEGES ON login_system.* TO 'dev_user'@'localhost';
FLUSH PRIVILEGES;
```

### 3. 配置Redis
```bash
# 启动Redis服务
redis-server

# 测试Redis连接
redis-cli ping
```

### 4. 修改配置文件
编辑 `src/main/resources/application.properties`：
```properties
# 数据库配置
spring.datasource.url=jdbc:mysql://localhost:3306/login_system?serverTimezone=UTC
spring.datasource.username=your_username
spring.datasource.password=your_password

# Redis配置
spring.redis.host=localhost
spring.redis.port=6379
```

### 5. 启动应用
```bash
# 方式1：使用Maven
mvn spring-boot:run

# 方式2：使用IDE
# 直接运行 RilaServerApplication 类

# 方式3：打包后运行
mvn clean package
java -jar target/rilaserver-0.0.1-SNAPSHOT.jar
```

## 本地开发特性

### 1. 宽松的安全配置
- **登录失败限制**：10次（生产环境5次）
- **锁定时间**：5分钟（生产环境30分钟）
- **密码要求**：最低6位（生产环境8位）
- **速率限制**：每分钟100次（生产环境5次）

### 2. 开发友好的设置
- **SQL日志**：显示所有SQL语句
- **调试日志**：详细的调试信息
- **热重载**：支持代码热重载
- **Swagger UI**：API文档界面

### 3. 安全头配置
- **HSTS**：禁用（本地HTTP环境）
- **X-Frame-Options**：SAMEORIGIN（允许同源嵌入）
- **CSP**：宽松策略（允许内联脚本）

## 测试页面

### 1. 基础功能测试
- **注册页面**：http://localhost:8080/register-api.html
- **登录页面**：http://localhost:8080/login-api.html
- **用户类型测试**：http://localhost:8080/user-type-test.html

### 2. API测试
- **Swagger UI**：http://localhost:8080/swagger-ui.html
- **API文档**：http://localhost:8080/v3/api-docs

### 3. WebSocket测试
- **WebSocket测试**：http://localhost:8080/websocket-test.html
- **流测试**：http://localhost:8080/stream-test.html

## 开发工作流

### 1. 日常开发
```bash
# 1. 启动MySQL和Redis
# 2. 启动应用
mvn spring-boot:run

# 3. 访问测试页面
# 4. 修改代码（支持热重载）
# 5. 测试功能
```

### 2. 数据库变更
```bash
# 1. 修改实体类
# 2. 应用会自动更新数据库结构（ddl-auto=update）
# 3. 或者手动执行SQL脚本
```

### 3. 调试技巧
```bash
# 查看详细日志
tail -f logs/application.log

# 查看SQL日志
grep "Hibernate" logs/application.log

# 查看安全相关日志
grep "Security" logs/application.log
```

## 常见问题

### 1. 数据库连接失败
```bash
# 检查MySQL服务
sudo systemctl status mysql

# 检查连接配置
mysql -u your_username -p -h localhost
```

### 2. Redis连接失败
```bash
# 检查Redis服务
redis-cli ping

# 启动Redis服务
redis-server
```

### 3. 端口被占用
```bash
# 查看端口占用
netstat -tulpn | grep 8080

# 杀死进程
kill -9 <PID>
```

### 4. 编译错误
```bash
# 清理并重新编译
mvn clean compile

# 更新依赖
mvn dependency:resolve
```

## 性能优化

### 1. 开发环境优化
```properties
# 减少日志输出
logging.level.org.hibernate.SQL=WARN
logging.level.org.hibernate.type.descriptor.sql.BasicBinder=WARN

# 禁用不必要的功能
spring.jpa.open-in-view=false
```

### 2. 内存优化
```bash
# 设置JVM参数
java -Xms512m -Xmx1024m -jar target/rilaserver-0.0.1-SNAPSHOT.jar
```

## 下一步

当您准备部署到生产环境时：
1. 获取域名和SSL证书
2. 参考 `PRODUCTION_SECURITY.md` 文档
3. 配置生产环境参数
4. 进行安全测试
5. 部署到服务器

## 联系支持

如果在本地开发过程中遇到问题：
- 查看日志文件
- 检查配置文件
- 参考本文档
- 联系开发团队 