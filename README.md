# RilaServer（Spring Boot 后端服务）

一个围绕“账户与安全、流媒体、智能局域网加速、应用发布与升级”的一体化服务端工程。它既可以作为个人/团队的直播与应用分发后端，也可以扩展为企业内部的多端统一服务中心。

## 项目概览
- 后端框架：Spring Boot 3.x（JDK 17），Spring Security、JPA、WebSocket、Redis、Bucket4j 限流
- 流媒体集成：ZLMediaKit（推/拉流地址生成、事件上报、状态同步）
- 智能加速：SmartCDN（基于 LAN 的中继优化，降低公网带宽与延迟）
- 应用能力：应用商城（App 列表、搜索、筛选、统计）、软件升级（多平台包、完整性校验、强更）
- 管理界面：admin-ui（基于 Vue3 + Vite 的管理后台）
- 用户端界面：my-ui（单用户控制台，个人流与日志）
- 客户端配套：qtloginclient、SmartCDNQt（LAN/mDNS 能力、边缘中继）

目录结构要点：
- 后端源码：src/main/java/com/graddu/rilaserver
- 管理后台：admin-ui
- 用户端：my-ui
- 智能加速与客户端：SmartCDNQt、qtloginclient
- 数据库脚本与文档：*.sql / 各种 *.md 指南

## 职责划分
- admin-ui（全局运营/运维后台）
  - 用户与角色：创建/编辑/禁用用户，角色与权限管理
  - 套餐与配额：用户套餐分配与调整、推流上限、设备上限
  - 应用商城：应用上架、推荐、分类与统计
  - 升级中心：多平台升级包上传，强制更新与发布说明
  - 流媒体管理：流列表、状态监控、密钥/地址查看与控制
  - 系统与安全：安全策略、流媒体端口、录制与日志、网络诊断
- my-ui（单用户个人控制台）
  - 我的流：当前/历史流信息、播放地址与鉴权结果
  - 推流会话：每次推流的开始/结束时间与时长统计
  - 个人日志：与当前账户相关的操作记录与系统通知
  - 实时互动：基于 WebSocket 的在线人数/弹幕视图（按业务接入播放器）

## 典型使用场景
- 个人/团队直播平台
  - 为主播生成推流地址、观众播放地址，WebSocket 推送流状态，按用户类型/套餐进行推流数限制与播放鉴权
- 企业内训/会议
  - 在同一局域网内通过 SmartCDN 自动就近拉流，减少公网带宽消耗，降低延迟，保障大场景稳定性
- 应用分发与升级
  - 通过应用商城发布软件下载，收集下载与评论数据；通过升级系统安全下发多平台安装包并校验完整性
- 统一账户与安全
  - 注册/登录、JWT、Redis 会话、设备上限、IP/速率限制、CSP 等生产级安全实践

## 特色功能
- 流媒体与状态同步
  - ZLMediaKit 集成：推/拉流 URL 生成、webhook 事件处理
  - WebSocket 实时事件：推流开始/结束、状态变更
  - 新客户端“即时状态同步”（无需等待下一次事件）（详见 STREAM_SYNC_README.md）
- SmartCDN 智能局域网加速
  - 基于 LAN 的中继树（最大三层）、节点 fan-out 控制
  - 优先返回本地 LAN 的 mediamtx 节点播放地址，无感切换至最优源（详见 smartcdn-design.md）
  - 客户端能力上报、局域网标识 lanId、mDNS/局域网发现支持（SmartCDNQt）
- 播放鉴权与限流
  - PlayAuth 服务、用户推流上限与套餐限制、按用户/设备统计
  - 接口级限流（Bucket4j）、敏感接口保护
- 应用商城
  - 应用展示、搜索和筛选、下载统计、评论与评分、推荐/上架管理（详见 APP_STORE_README.md）
- 软件升级系统
  - 多平台升级包（Windows/macOS/Linux/ARM）、SHA-256 完整性校验、强制更新、版本比较与查询（详见 UPDATE_SYSTEM_README.md）
- 安全与合规
  - JWT、BCrypt、登录失败锁定、会话管理（Redis）、CSP/HSTS/X-Frame-Options 等安全头（详见 PRODUCTION_SECURITY.md）

## 核心模块
- 账户与安全
  - Auth/Login/Register/SecurityController、Spring Security 配置、会话与设备管理、审计日志
- 流媒体与事件总线
  - StreamService、PlayAuthService、WebSocketController、StreamStatusSyncService（新客户端状态同步）
- SmartCDN
  - SmartCdnController/Service：客户端注册、局域网中继注册、最优播放地址选择
- 应用商城与升级
  - AppController、UpdateController：应用 CRUD/检索与升级包上传、版本查询、文件存储映射
- 管理后台（admin-ui）
  - 登录、仪表盘、用户管理、流媒体管理、应用与升级管理、网络诊断工具
- 用户端（my-ui）
  - 登录、个人控制台、流信息与直播时长统计、个人日志与状态视图

## 快速上手
后端（Spring Boot）：
1. 准备环境：JDK 17、Maven、MySQL 8、Redis（可选，用于会话与限流）
2. 初始化数据库：执行根目录下相关 *.sql（如 security_tables.sql、app_store_tables.sql、live_stream_tables.sql 等）
3. 配置 application-*.properties（数据库、Redis、ZLMediaKit、SmartCDN 开关等）
4. 启动
   - 编译：mvn clean package
   - 运行：mvn spring-boot:run 或 java -jar target/rilaserver-0.0.1-SNAPSHOT.jar
5. API 文档：默认 http://localhost:8080/swagger-ui/index.html

管理后台（admin-ui）：
1. 进入 admin-ui 目录，安装依赖：npm install
2. 设置开发环境变量：.env.development 中设置 VITE_APP_BASE_API=/api
3. 启动前端：npm run dev
4. 访问：http://localhost:5173 登录后台（详见 admin-ui/ADMIN_GUIDE.md）

用户端（my-ui）：
1. 进入 my-ui 目录，安装依赖：npm install
2. 开发代理：vite.config.js 已将 /api 与 /ws 代理至 http://localhost:8080
3. 启动前端：npm run dev
4. 访问：http://localhost:5174 使用个人控制台

## 直播/流媒体要点
- 推/拉流地址由 ZLMediaKitConfig 与 StreamService 统一生成与管理
- WebSocket 推送实时事件；新连接可自动同步当前正在推送的流状态
- SmartCDN 开启后，播放地址选择将优先本地 LAN 中继，减少公网拉流压力
- 支持用户推流上限、设备上限与套餐化限制，保障公平与稳定

## 运维与安全
- 生产安全清单：证书/HTTPS、强密码与密钥、CSP/HSTS、安全头、限流与日志（见 PRODUCTION_SECURITY.md）
- 反向代理与静态资源：Nginx 示例与文件存储映射（详见文档）
- Docker：提供 Redis docker-compose 示例

## 深入了解
- SmartCDN 设计与接口：smartcdn-design.md
- 流状态同步说明：STREAM_SYNC_README.md
- 应用商城说明：APP_STORE_README.md
- 升级系统说明：UPDATE_SYSTEM_README.md
- 部署与环境：LOCAL_DEVELOPMENT.md、CLOUD_DEPLOYMENT*.md、PRODUCTION_SECURITY.md

如需更细致的业务或二次开发指南，可直接阅读各模块下的文档与源码。欢迎在此基础上扩展更多场景与能力。 
