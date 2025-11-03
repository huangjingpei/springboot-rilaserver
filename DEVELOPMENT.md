# 开发文档

本文档旨在为开发者提供对该项目的全面理解，包括其架构、核心功能、开发环境设置和部署说明。

## 1. 项目概述

该项目是一个基于 Spring Boot 的微服务应用，旨在管理 ZLMediaKit 流媒体服务器，并提供用户注册、登录、直播流管理、推流/拉流等功能。项目还包含一个 Qt 客户端，用于与后端服务进行交互。

### 1.1. 核心功能

*   **用户管理**:
    *   用户注册和登录
    *   基于 JWT 的认证
    *   完善的安全机制 (登录尝试限制、审计日志、设备管理)
*   **流媒体管理**:
    *   通过 ZLMediaKit 的 hook 机制与流媒体服务器深度集成
    *   推流/播放鉴权
    *   实时监控流状态 (如在线人数)
    *   通过 WebSocket 向客户端实时推送流状态
*   **直播间管理**:
    *   配置直播间信息 (直播地址、推流时间等)
    *   监控直播间状态 (是否开播、主播信息等)
*   **客户端**:
    *   提供 Qt 客户端，通过 REST 和 WebSocket 与后端交互

### 1.2. 技术栈

*   **后端**:
    *   Spring Boot 3.2.4
    *   Java 17
    *   Maven
    *   MySQL
    *   Redis (用于会话管理和速率限制)
    *   Spring Data JPA
    *   Spring Security
    *   WebSocket
    *   ZLMediaKit
*   **前端 (Qt 客户端)**:
    *   Qt
    *   C++
    *   QWebEngineView + QWebChannel (混合开发)
    *   HTML, CSS, JavaScript

## 2. 项目结构

```
.
├── src/main/java/net/enjoy/springboot/registrationlogin/
│   ├── config/         # Spring Boot 配置
│   ├── controller/     # RESTful API 控制器
│   ├── dto/            # 数据传输对象
│   ├── entity/         # JPA 实体
│   ├── model/          # 数据模型
│   ├── repository/     # Spring Data JPA 仓库
│   ├── security/       # Spring Security 相关
│   ├── service/        # 业务逻辑
│   ├── utils/          # 工具类
│   └── zlmediakit/     # ZLMediaKit 集成
├── src/main/resources/ # 配置文件和静态资源
├── qtloginclient/      # Qt 客户端项目
├── *.sql               # 数据库脚本
└── pom.xml             # Maven 配置
```

## 3. 核心模块详解

### 3.1. Spring Boot 后端

#### 3.1.1. ZLMediaKit 集成

*   **`zlm-spring-boot-starter`**:
    *   通过该 starter，项目可以方便地与 ZLMediaKit 的 API 进行交互。
*   **`ZLMediaKitConfig.java`**:
    *   配置 ZLMediaKit 服务器信息和 URL 模板。
*   **`ExZlmHookController.java`**:
    *   处理 ZLMediaKit 的 hook 回调，是实现业务逻辑的关键。
    *   **`on_play`**: 播放鉴权。
    *   **`on_publish`**: 推流鉴权。
    *   **`on_stream_none_reader`**: 无人观看时触发。
    *   **`on_flow_report`**: 流量报告。
*   **`ZlmRestController.java`**:
    *   提供主动调用 ZLMediaKit API 的接口。

#### 3.1.2. 安全

*   **`SecurityConfig.java`**:
    *   配置 Spring Security，包括 HTTP 安全规则、密码编码器等。
*   **`JwtAuthenticationFilter.java`**:
    *   在每个请求中验证 JWT。
*   **`CustomUserDetailsService.java`**:
    *   从数据库加载用户信息。
*   **`RateLimitFilter.java`**:
    *   使用 Bucket4j 实现 API 速率限制。

#### 3.1.3. 数据库

*   **`entity`** 包下的类定义了数据库表结构。
*   **`.sql`** 文件用于数据库初始化和迁移。
*   核心表包括 `users`, `live_stream_configs`, `live_stream_status`。

### 3.2. Qt 客户端

*   **混合架构**:
    *   使用 `QWebEngineView` 加载网页作为 UI，实现了 UI 的灵活开发。
    *   通过 `QWebChannel` 在 C++ 和 JavaScript 之间进行双向通信。
*   **`LoginBridge.cpp`**:
    *   作为 C++ 和 JavaScript 之间的桥梁，处理登录、注册、WebSocket 连接等逻辑。
*   **`WebSocketClient.cpp`**:
    *   封装了 WebSocket 功能，用于与后端进行实时通信。

## 4. 开发环境设置

1.  **安装 Java 17 和 Maven**
2.  **安装 MySQL 和 Redis**
3.  **创建数据库并执行 `.sql` 脚本**
4.  **修改 `application.properties`**:
    *   配置数据库连接信息。
    *   配置 Redis 连接信息。
    *   配置 ZLMediaKit 服务器地址。
5.  **运行 Spring Boot 应用**:
    *   `mvn spring-boot:run`
6.  **安装 Qt 和相关依赖**
7.  **编译和运行 Qt 客户端**

## 5. 部署

*   **Docker**:
    *   项目提供了 `docker-compose.yml` 文件，可以使用 Docker Compose 进行一键部署。
*   **手动部署**:
    *   将 Spring Boot 应用打包成 JAR 文件，并在服务器上运行。
    *   部署 ZLMediaKit。
    *   确保 Spring Boot 应用可以访问 ZLMediaKit、MySQL 和 Redis。