# 智能社区服务平台（Smart Community Service Platform）

## � 项目简介

智能社区服务平台是一个面向现代社区管理的综合性服务系统，旨在提升社区居民生活体验和物业管理效率。系统采用前后端分离架构，提供完整的用户认证、角色权限管理和多种社区服务功能。

### 核心功能模块

**🏠 社区活动管理**
- 活动发布与报名：物业管理员可发布社区活动，居民在线报名参与
- 签到打卡：支持二维码扫码签到，实时统计参与情况
- 活动评价：参与者可对活动进行评分和评论，上传活动照片

**🔧 报修服务**
- 在线报修：居民提交报修工单，支持图片上传
- 工单派发：物业管理员分配维修任务给服务商
- 进度跟踪：实时查看报修状态（待处理、进行中、已完成）
- 服务评价：完成后对服务质量进行评分和反馈

**💬 邻里社区**
- 社区动态：居民发布生活分享、互助信息
- 互动交流：点赞、评论功能促进邻里沟通

**💰 物业缴费**
- 在线缴费：支持物业费在线支付
- 缴费记录：查看历史缴费明细和状态

**📢 公告通知**
- 系统公告：物业发布社区通知和紧急公告
- 消息中心：个人消息推送和站内信管理

**👤 用户管理**
- 多角色支持：居民、物业管理员、服务商三种角色
- 个人中心：资料管理、实名认证、密码修改
- 访客管理：访客登记和二维码通行

### 技术特点

- **安全性**：JWT 身份认证、BCrypt 密码加密、敏感数据加密存储
- **高性能**：HikariCP 数据库连接池、热点数据缓存机制
- **可扩展**：Filter 链式处理、Servlet 模块化设计、RESTful API 规范
- **易部署**：Docker Compose 一键部署、多环境配置支持

## �🛠 技术栈
- Frontend: Vue 3 + Vite + Element Plus + Pinia + Vue Router + Zod
- Backend: Java Servlet + Filter + Listener + JDBC + HikariCP + JWT
- Database: MySQL 8.0

## 🚀 启动指南 (How to Run)
1. 确保 Docker Desktop 已启动。
2. 在根目录执行：`docker compose up --build`
3. 等待容器启动完成...

## 🔗 服务地址 (Services)
- Frontend: http://localhost:3674
- Backend Swagger UI: http://localhost:3674/api-docs （OpenAPI JSON: http://localhost:8674/api/openapi.json）
- Backend API Base: http://localhost:8674/api
- Health Check: http://localhost:8674/api/health
- Database: localhost:13674 (user: smart_user / pass: smart_pass_3674)

## 🧪 测试账号
- Resident: resident_demo / 123456
- Property Admin: property_admin / 123456
- Service Provider: service_pro / 123456

## 📚 项目文档
- 系统架构设计：`docs/01-system-architecture.md`
- 数据库设计：`docs/02-database-design.md`
- API 接口设计：`docs/03-api-interface-spec.md`
- 部署运维文档：`docs/04-deployment-operations.md`
- 测试报告：`docs/05-test-report.md`
- 团队报告：`docs/06-team-report.md`

## 📦 仓库协作核验说明
- 当前交付目录默认不包含 `.git` 元信息，因此无法直接核验远程仓库地址与完整提交历史。
- 如需核验 `gitee` 远程与提交记录，请使用带 `.git` 的源码包并执行：`git remote -v`、`git log --oneline --graph --decorate --all`。

## 🐳 Docker 镜像源配置 (Docker Registry Configuration)

### 推荐配置（基于实际项目验证）

#### 1. Docker 镜像源
**使用官方 Docker Hub 镜像**（已验证稳定可用）

```yaml
services:
  db:
    image: mysql:8.0

  backend:
    build: ./backend
    # Dockerfile:
    # - maven:3.9-eclipse-temurin-17 (build)
    # - tomcat:9.0-jdk17-temurin (runtime)

  frontend:
    build: ./frontend
    # Dockerfile:
    # - node:20-alpine (build)
    # - nginx:alpine (runtime)
```

#### 2. npm 依赖源
**使用淘宝镜像**（国内访问快）

已在 `frontend/Dockerfile` 中配置：
```dockerfile
RUN npm config set registry https://registry.npmmirror.com
```

#### 3. Maven 依赖源
**使用阿里云镜像**（解决 Java 构建慢的问题）

已在 `backend/settings.xml` 中配置并由 Dockerfile 使用。
