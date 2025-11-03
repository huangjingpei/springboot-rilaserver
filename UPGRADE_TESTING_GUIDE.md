# 🧪 升级系统测试指南

## 📋 测试概述

本指南将帮助您全面测试多应用升级系统的所有功能。

## 🚀 快速开始

### 1. 环境准备

```bash
# 1. 启动应用
mvn spring-boot:run

# 2. 安装测试依赖
npm install axios form-data

# 3. 执行数据库初始化
mysql -u your_username -p your_database < test_upgrade_system.sql
```

### 2. 运行测试

```bash
# 运行全面测试
node test_upgrade_system.js
```

## 📊 测试项目

### 🔍 核心功能测试

| 测试项目 | API 端点 | 预期结果 |
|---------|---------|---------|
| 检查应用更新 | `GET /api/v1/updates/latest` | 返回最新版本信息 |
| 获取应用列表 | `GET /api/v1/updates/apps` | 返回分页应用列表 |
| 获取激活应用 | `GET /api/v1/updates/apps/active` | 返回激活的应用 |
| 获取强制更新 | `GET /api/v1/updates/mandatory/{appId}/{platform}` | 返回强制更新列表 |
| 获取升级包列表 | `GET /api/v1/updates` | 返回分页升级包列表 |

### 🔧 工具功能测试

| 测试项目 | API 端点 | 预期结果 |
|---------|---------|---------|
| 版本比较 | `GET /api/v1/updates/compare-versions` | 返回版本比较结果 |
| 版本验证 | `GET /api/v1/updates/validate-version` | 返回版本有效性 |
| 应用搜索 | `GET /api/v1/updates/apps/search` | 返回搜索结果 |
| 获取分类 | `GET /api/v1/updates/apps/categories` | 返回应用分类 |
| 获取标签 | `GET /api/v1/updates/apps/tags` | 返回应用标签 |
| 获取平台 | `GET /api/v1/updates/platforms` | 返回支持平台 |

### 📥 下载功能测试

| 测试项目 | API 端点 | 预期结果 |
|---------|---------|---------|
| 下载链接 | `GET /api/v1/updates/download/{version}` | 返回文件下载 |

## 🧪 手动测试步骤

### 1. 检查应用更新

```bash
curl "http://localhost:8080/api/v1/updates/latest?appId=test-app-1&currentVersion=1.0.0&platform=WINDOWS"
```

**预期响应：**
```json
{
  "hasUpdate": true,
  "latestVersion": "1.2.0",
  "releaseNotes": "新增功能A和B",
  "isMandatory": false,
  "downloadUrl": "/api/v1/updates/file/1.2.0?platform=WINDOWS",
  "fileSize": 1050000,
  "fileHash": "def456ghi789"
}
```

### 2. 获取应用列表

```bash
curl "http://localhost:8080/api/v1/updates/apps?page=0&size=10"
```

**预期响应：**
```json
{
  "content": [
    {
      "appId": "test-app-1",
      "name": "测试应用1",
      "currentVersion": "1.0.0",
      "isActive": true,
      "platform": "WINDOWS"
    }
  ],
  "totalElements": 3,
  "totalPages": 1
}
```

### 3. 获取强制更新

```bash
curl "http://localhost:8080/api/v1/updates/mandatory/test-app-2/WINDOWS"
```

**预期响应：**
```json
[
  {
    "version": "2.1.0",
    "isMandatory": true,
    "releaseNotes": "重要安全更新",
    "fileSize": 2048000
  }
]
```

## 🔍 测试场景

### 场景1：正常更新流程

1. **客户端检查更新**
   - 应用版本：1.0.0
   - 服务器版本：1.2.0
   - 结果：有可用更新

2. **下载更新**
   - 获取下载链接
   - 验证文件完整性
   - 执行更新

### 场景2：强制更新

1. **检查强制更新**
   - 应用：test-app-2
   - 版本：2.0.0
   - 结果：有强制更新

2. **强制更新流程**
   - 阻止应用继续使用
   - 强制下载更新
   - 重启应用

### 场景3：多平台支持

1. **Windows 平台**
   - 应用：test-app-1
   - 平台：WINDOWS
   - 文件：.exe

2. **Android 平台**
   - 应用：mobile-app-1
   - 平台：ANDROID
   - 文件：.apk

## 🐛 常见问题排查

### 问题1：应用不存在

**错误信息：** `应用不存在: test-app-1`

**解决方案：**
1. 检查数据库中的 `apps` 表
2. 确认 `app_id` 字段值正确
3. 确认应用状态为激活

```sql
SELECT * FROM apps WHERE app_id = 'test-app-1';
```

### 问题2：没有可用更新

**错误信息：** `hasUpdate: false`

**解决方案：**
1. 检查当前版本是否已是最新
2. 确认升级包状态为激活
3. 检查平台匹配

```sql
SELECT * FROM update_packages 
WHERE app_id = 'test-app-1' 
AND platform = 'WINDOWS' 
AND is_active = true 
ORDER BY release_date DESC;
```

### 问题3：下载失败

**错误信息：** `404 文件未找到`

**解决方案：**
1. 检查文件路径是否正确
2. 确认文件实际存在
3. 检查文件权限

```bash
ls -la uploads/test-app-1-1.2.0.exe
```

## 📈 性能测试

### 并发测试

```bash
# 使用 Apache Bench 进行并发测试
ab -n 1000 -c 10 "http://localhost:8080/api/v1/updates/latest?appId=test-app-1&currentVersion=1.0.0&platform=WINDOWS"
```

### 压力测试

```bash
# 使用 wrk 进行压力测试
wrk -t12 -c400 -d30s "http://localhost:8080/api/v1/updates/latest?appId=test-app-1&currentVersion=1.0.0&platform=WINDOWS"
```

## 📝 测试报告模板

```markdown
# 升级系统测试报告

## 测试环境
- 应用版本：1.0.0
- 测试时间：2024-01-XX
- 测试人员：XXX

## 测试结果
- ✅ 核心功能：通过
- ✅ 下载功能：通过
- ✅ 版本管理：通过
- ✅ 多平台支持：通过

## 发现的问题
1. 问题描述
2. 严重程度：高/中/低
3. 解决方案

## 建议改进
1. 性能优化建议
2. 功能增强建议
3. 用户体验改进
```

## 🎯 测试检查清单

- [ ] 应用更新检查功能正常
- [ ] 强制更新功能正常
- [ ] 多平台支持正常
- [ ] 文件下载功能正常
- [ ] 版本比较功能正常
- [ ] 应用搜索功能正常
- [ ] 分页查询功能正常
- [ ] 错误处理机制正常
- [ ] 安全验证功能正常
- [ ] 性能表现符合预期

## 📞 技术支持

如果在测试过程中遇到问题，请：

1. 检查应用日志：`tail -f logs/application.log`
2. 检查数据库连接
3. 验证配置文件设置
4. 联系技术支持团队

---

**祝您测试顺利！** 🚀 