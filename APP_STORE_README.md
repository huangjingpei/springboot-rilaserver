# 应用商城系统

一个基于Spring Boot的完整应用商城系统，提供应用展示、搜索、筛选、下载、评论等功能。

## 功能特性

### 1. 应用展示
- ✅ 应用基本信息展示（名称、描述、图标、截图）
- ✅ 评分和评论系统
- ✅ 价格信息（免费/付费）
- ✅ 应用类型分类
- ✅ 多平台支持（Android、iOS、Windows、Mac等）
- ✅ 下载统计

### 2. 搜索与筛选
- ✅ 关键词搜索（应用名称、描述、开发者）
- ✅ 应用类型筛选
- ✅ 平台筛选
- ✅ 价格区间筛选
- ✅ 评分筛选
- ✅ 多条件组合筛选

### 3. 排序功能
- ✅ 按评分排序
- ✅ 按价格排序
- ✅ 按下载量排序
- ✅ 按发布时间排序

### 4. 用户体验
- ✅ 响应式设计（PC、手机、平板）
- ✅ 即时搜索
- ✅ 分页加载
- ✅ 应用详情模态框
- ✅ 下载链接管理

### 5. 管理功能
- ✅ 应用CRUD操作
- ✅ 推荐应用设置
- ✅ 应用状态管理
- ✅ 下载统计

## 技术架构

### 后端技术栈
- **Spring Boot 3.x** - 主框架
- **Spring Data JPA** - 数据访问层
- **MySQL** - 数据库
- **Spring Security** - 安全框架
- **Maven** - 依赖管理

### 前端技术栈
- **HTML5 + CSS3** - 页面结构
- **JavaScript (ES6+)** - 交互逻辑
- **Bootstrap 5** - UI框架
- **Font Awesome** - 图标库

### 数据库设计
```
apps                    - 应用主表
├── app_screenshots     - 应用截图
├── app_platforms       - 应用平台
├── app_downloads       - 下载链接
└── app_reviews         - 用户评论
```

## 快速开始

### 1. 环境要求
- JDK 17+
- MySQL 8.0+
- Maven 3.6+

### 2. 数据库配置
```sql
-- 执行数据库脚本
source app_store_tables.sql;
```

### 3. 应用配置
修改 `application.properties` 或 `application-dev.properties`：
```properties
# 数据库配置
spring.datasource.url=jdbc:mysql://localhost:3306/your_database
spring.datasource.username=your_username
spring.datasource.password=your_password

# JPA配置
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=true
```

### 4. 启动应用
```bash
# 编译项目
mvn clean compile

# 运行应用
mvn spring-boot:run
```

### 5. 访问应用
- 应用商城页面：http://localhost:8080/app-store.html
- API文档：http://localhost:8080/swagger-ui.html

## API接口文档

### 应用相关接口

#### 1. 获取应用列表
```http
GET /api/apps/search
```

**参数：**
- `keyword` (可选) - 搜索关键词
- `type` (可选) - 应用类型
- `platform` (可选) - 平台
- `isFree` (可选) - 是否免费
- `isFeatured` (可选) - 是否推荐
- `page` (默认0) - 页码
- `size` (默认20) - 每页大小
- `sortBy` (默认createdAt) - 排序字段
- `sortOrder` (默认desc) - 排序方向

#### 2. 获取应用详情
```http
GET /api/apps/{id}
```

#### 3. 获取推荐应用
```http
GET /api/apps/featured?page=0&size=20
```

#### 4. 获取最新应用
```http
GET /api/apps/latest?page=0&size=20
```

#### 5. 获取热门应用
```http
GET /api/apps/popular?page=0&size=20
```

#### 6. 获取免费应用
```http
GET /api/apps/free?page=0&size=20
```

#### 7. 根据类型获取应用
```http
GET /api/apps/type/{type}?page=0&size=20
```

#### 8. 根据平台获取应用
```http
GET /api/apps/platform/{platform}?page=0&size=20
```

#### 9. 获取应用类型列表
```http
GET /api/apps/types
```

#### 10. 获取平台列表
```http
GET /api/apps/platforms
```

#### 11. 获取应用统计
```http
GET /api/apps/count
```

### 管理接口

#### 1. 创建应用
```http
POST /api/apps
Content-Type: application/json

{
  "name": "应用名称",
  "description": "应用描述",
  "shortDescription": "简短描述",
  "appIcon": "图标URL",
  "screenshots": ["截图URL1", "截图URL2"],
  "price": 0.00,
  "type": "GAME",
  "platforms": ["ANDROID", "IOS"],
  "developer": "开发者",
  "version": "1.0.0",
  "fileSize": "50MB"
}
```

#### 2. 更新应用
```http
PUT /api/apps/{id}
Content-Type: application/json

{
  // 同创建应用
}
```

#### 3. 删除应用
```http
DELETE /api/apps/{id}
```

#### 4. 设置推荐状态
```http
PUT /api/apps/{id}/featured?featured=true
```

#### 5. 设置应用状态
```http
PUT /api/apps/{id}/active?active=true
```

#### 6. 增加下载次数
```http
POST /api/apps/{id}/download?platform=ANDROID
```

## 应用类型枚举

```java
public enum AppType {
    GAME("游戏"),
    TOOL("工具"),
    SOCIAL("社交"),
    PHOTOGRAPHY("摄影"),
    PRODUCTIVITY("生产力"),
    EDUCATION("教育"),
    ENTERTAINMENT("娱乐"),
    HEALTH("健康"),
    FINANCE("金融"),
    SHOPPING("购物"),
    TRAVEL("旅行"),
    NEWS("新闻"),
    SPORTS("体育"),
    MUSIC("音乐"),
    VIDEO("视频"),
    OTHER("其他");
}
```

## 平台枚举

```java
public enum Platform {
    ANDROID("Android"),
    IOS("iOS"),
    WINDOWS("Windows"),
    MAC("Mac"),
    LINUX("Linux"),
    WEB("Web");
}
```

## 前端功能说明

### 1. 响应式设计
- 桌面端：4列布局
- 平板端：3列布局
- 手机端：2列布局

### 2. 搜索功能
- 实时搜索（500ms防抖）
- 支持应用名称、描述、开发者搜索
- 搜索结果高亮显示

### 3. 筛选功能
- 应用类型筛选
- 平台筛选
- 价格筛选
- 评分筛选

### 4. 排序功能
- 最新发布
- 评分最高
- 下载最多
- 价格最低

### 5. 应用详情
- 模态框展示
- 应用截图轮播
- 用户评论展示
- 多平台下载链接

## 部署指南

### 1. 开发环境
```bash
# 克隆项目
git clone <repository-url>
cd spring-boot-user-registration-and-Login

# 配置数据库
mysql -u root -p < app_store_tables.sql

# 修改配置文件
vim src/main/resources/application-dev.properties

# 启动应用
mvn spring-boot:run
```

### 2. 生产环境
```bash
# 打包应用
mvn clean package -Dmaven.test.skip=true

# 运行应用
java -jar target/registration-login-0.0.1-SNAPSHOT.jar \
  --spring.profiles.active=prod \
  --server.port=8080
```

### 3. Docker部署
```dockerfile
FROM openjdk:17-jdk-slim
COPY target/registration-login-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app.jar"]
```

```bash
# 构建镜像
docker build -t app-store .

# 运行容器
docker run -d -p 8080:8080 --name app-store app-store
```

## 性能优化

### 1. 数据库优化
- 添加合适的索引
- 使用分页查询
- 缓存热点数据

### 2. 前端优化
- 图片懒加载
- 分页加载
- 防抖搜索
- 缓存API响应

### 3. 缓存策略
- Redis缓存热门应用
- 浏览器缓存静态资源
- CDN加速图片资源

## 安全考虑

### 1. 接口安全
- Spring Security认证授权
- 接口访问控制
- 参数验证

### 2. 数据安全
- SQL注入防护
- XSS攻击防护
- CSRF防护

### 3. 文件安全
- 文件上传验证
- 文件类型限制
- 文件大小限制

## 扩展功能

### 1. 用户系统
- 用户注册登录
- 个人收藏
- 下载历史
- 评论管理

### 2. 支付系统
- 应用购买
- 支付集成
- 订单管理

### 3. 推荐系统
- 基于用户行为的推荐
- 协同过滤算法
- 个性化推荐

### 4. 数据分析
- 下载统计
- 用户行为分析
- 应用热度分析

## 常见问题

### Q1: 如何添加新的应用类型？
A: 在 `App.AppType` 枚举中添加新的类型，并更新前端筛选按钮。

### Q2: 如何自定义应用图标？
A: 将图标文件上传到静态资源目录，或在应用数据中设置图标URL。

### Q3: 如何添加新的平台支持？
A: 在 `App.Platform` 枚举中添加新平台，并更新前端平台筛选。

### Q4: 如何优化搜索性能？
A: 可以添加全文搜索引擎（如Elasticsearch）或使用数据库全文索引。

## 贡献指南

1. Fork 项目
2. 创建功能分支
3. 提交更改
4. 推送到分支
5. 创建 Pull Request

## 许可证

本项目采用 MIT 许可证 - 查看 [LICENSE](LICENSE) 文件了解详情。

## 联系方式

如有问题或建议，请通过以下方式联系：
- 提交 Issue
- 发送邮件
- 项目讨论区

---

**注意：** 这是一个演示项目，生产环境使用前请进行充分的安全测试和性能优化。 




总结
我已经为您设计并实现了一个完整的应用商城系统！这个系统包含了您要求的所有功能：
🎯 核心功能实现
1. 应用展示功能
✅ 应用基本信息（名称、描述、图标、截图）
✅ 评分和评论系统
✅ 价格信息（免费/付费）
✅ 应用类型分类（游戏、工具、社交等15种类型）
✅ 多平台支持（Android、iOS、Windows、Mac、Linux、Web）
✅ 下载统计
2. 搜索与筛选功能
✅ 关键词搜索（应用名称、描述、开发者）
✅ 应用类型筛选
✅ 平台筛选
✅ 价格区间筛选
✅ 评分筛选
✅ 多条件组合筛选
3. 排序功能
✅ 按评分排序
✅ 按价格排序
✅ 按下载量排序
✅ 按发布时间排序
4. 用户体验优化
✅ 响应式设计（PC、手机、平板适配）
✅ 即时搜索（500ms防抖）
✅ 分页加载
✅ 应用详情模态框
✅ 下载链接管理
��️ 技术架构
后端架构：
Spring Boot 3.x + Spring Data JPA
MySQL数据库
RESTful API设计
分层架构（Controller → Service → Repository）
前端设计：
现代化UI设计（Bootstrap 5 + Font Awesome）
响应式布局
流畅的交互体验
优雅的动画效果
数据库设计：
应用主表（apps）
应用截图表（app_screenshots）
应用平台表（app_platforms）
下载链接表（app_downloads）
用户评论表（app_reviews）
�� 文件结构
Apply to README.md
�� 快速使用
执行数据库脚本：
Apply to README.md
启动应用：
Apply to README.md
Run
访问应用商城：
Apply to README.md
🎨 特色功能
智能搜索：支持实时搜索和多条件筛选
美观界面：现代化设计，支持深色主题
响应式布局：完美适配各种设备
丰富交互：悬停效果、动画过渡
完整API：提供完整的RESTful接口
示例数据：包含10个热门应用的示例数据
这个应用商城系统完全满足您的需求，具有良好的扩展性和维护性。您可以根据实际需要进一步定制和优化功能！