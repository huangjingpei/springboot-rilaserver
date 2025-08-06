# 应用商城新功能说明

## 新增功能

### 1. 统计卡片点击功能

#### 总应用数点击
- **功能**：点击总应用数卡片，显示所有应用的列表
- **API**：`GET /api/apps/all?page=0&size=20`
- **效果**：页面标题变为"所有应用"，显示所有活跃应用

#### 免费应用点击
- **功能**：点击免费应用卡片，显示所有免费应用的列表
- **API**：`GET /api/apps/free?page=0&size=20`
- **效果**：页面标题变为"免费应用"，只显示价格为0的应用

#### 推荐应用点击
- **功能**：点击推荐应用卡片，显示下载量最大的前10个应用
- **API**：`GET /api/apps/popular?page=0&size=10`
- **效果**：页面标题变为"下载量排行"，显示最热门的应用

#### 总下载量点击
- **功能**：点击总下载量卡片，显示下载量统计图表
- **API**：`GET /api/apps/top-downloads`
- **效果**：弹出模态框，显示柱状图和排行榜

### 2. 下载量图表功能

#### 图表特性
- **类型**：柱状图（使用Chart.js）
- **数据**：显示下载量前10的应用
- **交互**：鼠标悬停显示详细数据
- **响应式**：自适应容器大小

#### 排行榜特性
- **显示**：应用图标、名称、下载量
- **排名**：前三名高亮显示
- **格式**：下载量使用千分位分隔符

### 3. 新增API接口

#### 统计相关
```http
GET /api/apps/count/free          # 免费应用数量
GET /api/apps/count/featured      # 推荐应用数量
GET /api/apps/count/downloads     # 总下载次数
```

#### 应用列表相关
```http
GET /api/apps/all                 # 所有应用
GET /api/apps/top-downloads       # 下载量前10应用
```

### 4. 数据库优化

#### 下载量调整
- 普通应用：1,000 - 50,000次下载
- 热门应用：50,000 - 250,000次下载
- 新应用：100 - 1,000次下载

#### 执行脚本
```sql
-- 运行 fix_download_counts.sql 来调整下载量
```

## 技术实现

### 前端技术
- **Chart.js**：图表库，用于生成柱状图
- **Bootstrap 5**：UI框架，用于模态框和样式
- **Fetch API**：异步数据请求

### 后端技术
- **Spring Data JPA**：数据访问层
- **JPQL查询**：统计查询
- **分页支持**：支持大量数据的分页显示

### 数据库查询
```sql
-- 总下载量统计
SELECT COALESCE(SUM(download_count), 0) FROM apps WHERE is_active = true;

-- 免费应用数量
SELECT COUNT(*) FROM apps WHERE price = 0 AND is_active = true;

-- 推荐应用数量
SELECT COUNT(*) FROM apps WHERE is_featured = true AND is_active = true;
```

## 使用说明

### 1. 启动应用
```bash
mvn spring-boot:run
```

### 2. 访问应用商城
```
http://localhost:8080/app-store.html
```

### 3. 测试功能
- 点击各个统计卡片查看不同视图
- 点击总下载量查看图表
- 使用筛选功能过滤应用

### 4. 调整数据
```sql
-- 在MySQL中执行
source fix_download_counts.sql;
```

## 注意事项

1. **图表库依赖**：确保网络连接正常，Chart.js从CDN加载
2. **数据量**：大量数据时建议使用分页
3. **性能**：统计查询已优化，使用索引提高查询速度
4. **兼容性**：支持现代浏览器，IE不支持

## 扩展建议

1. **更多图表类型**：可添加饼图、折线图等
2. **时间维度**：可添加按时间统计的下载趋势
3. **用户行为**：可添加用户点击、收藏等统计
4. **实时更新**：可使用WebSocket实现实时数据更新 