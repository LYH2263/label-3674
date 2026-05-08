# 智能社区服务平台部署运维文档

## 1. 部署目标

通过 Docker Compose 快速完成前端、后端、数据库的统一部署，保证项目能够在答辩环境、演示环境和本地环境中稳定运行。

## 2. 部署架构

| 服务 | 容器 | 端口 | 说明 |
| --- | --- | --- | --- |
| 前端 | `smart-community-frontend-3674` | `3674:80` | Nginx 托管 Vue 构建产物 |
| 后端 | `smart-community-backend-3674` | `8674:8080` | Tomcat 运行 Servlet 应用 |
| 数据库 | `smart-community-db-3674` | `13674:3306` | MySQL 8.0 |

## 3. 运行环境要求

- Docker 29+
- Docker Compose 插件
- 可访问 Docker Hub、npm 镜像、Maven 镜像源
- 建议内存 4GB 及以上

## 4. 关键配置

### 4.1 Compose 文件

- 根目录文件：`docker-compose.yml`

### 4.2 环境变量

| 变量 | 默认值 | 说明 |
| --- | --- | --- |
| `MYSQL_ROOT_PASSWORD` | `root3674` | MySQL root 密码 |
| `MYSQL_DATABASE` | `smart_community` | 数据库名 |
| `MYSQL_USER` | `smart_user` | 业务账号 |
| `MYSQL_PASSWORD` | `smart_pass_3674` | 业务密码 |
| `JWT_SECRET` | `smart-community-secret-3674` | JWT 密钥 |
| `APP_CRYPTO_KEY` | `smart-community-crypto-key-3674` | 敏感信息加密密钥 |
| `APP_ACTIVITY_CHECKIN_SECRET` | `smart-community-checkin-secret-3674` | 活动签到口令密钥 |
| `APP_STORAGE_TYPE` | `LOCAL` | 存储类型 |
| `APP_UPLOAD_DIR` | `/opt/tomcat/uploads` | 上传目录 |
| `APP_OBJECT_STORAGE_DIR` | `/opt/tomcat/object-storage` | 对象存储目录 |

## 5. 构建与启动步骤

### 5.1 一键启动

```bash
docker compose up --build
```

### 5.2 分阶段执行

```bash
docker compose build
docker compose up -d
docker compose ps
```

### 5.3 访问地址

- 前端主页：`http://localhost:3674`
- API 文档页：`http://localhost:3674/api-docs`
- OpenAPI JSON：`http://localhost:8674/api/openapi.json`
- 健康检查：`http://localhost:8674/api/health`
- 数据库连接：`localhost:13674`

## 6. 数据持久化

项目使用以下 Docker Volume：

- `db_data_3674`
- `uploads_3674`
- `object_storage_3674`

说明：

- 数据库数据不会随容器重建而丢失。
- 上传图片与对象存储目录独立持久化。

## 7. 运维检查项

### 7.1 容器状态检查

```bash
docker compose ps
```

### 7.2 健康检查

```bash
curl http://localhost:8674/api/health
```

期望返回：

```json
{"code":0,"message":"success","data":{"status":"UP"}}
```

### 7.3 日志查看

```bash
docker compose logs -f backend
docker compose logs -f frontend
docker compose logs -f db
```

## 8. 常见运维问题

### 8.1 前端可打开但接口 401

- 检查是否已登录并携带 JWT。
- 检查浏览器请求头是否包含 `Authorization: Bearer <token>`。

### 8.2 数据库启动失败

- 检查端口 `13674` 是否被占用。
- 检查 Docker 卷中是否存在旧版本异常数据。

### 8.3 上传图片访问失败

- 检查 `APP_UPLOAD_DIR` 是否存在。
- 检查 `/api/files/{fileName}` 路径是否被正确代理。

### 8.4 WebSocket 通知不工作

- 确认 Nginx `/ws/` 已配置 `Upgrade` 和 `Connection` 头。
- 确认浏览器使用有效 JWT 建立连接。

## 9. 备份与恢复建议

### 9.1 数据库备份

```bash
docker exec smart-community-db-3674 mysqldump -uroot -proot3674 smart_community > backup.sql
```

### 9.2 数据库恢复

```bash
docker exec -i smart-community-db-3674 mysql -uroot -proot3674 smart_community < backup.sql
```

### 9.3 上传文件备份

- 备份 Docker Volume `uploads_3674` 与 `object_storage_3674`。

## 10. 本项目推荐运维策略

- 答辩或演示前执行 `docker compose up --build -d`。
- 演示前检查健康接口与登录流程。
- 使用演示账号进行完整业务回归。
- 若需重置数据，可先清理容器与卷后重新初始化。

## 11. 文档结论

当前项目已具备完整的容器化部署方案、数据持久化方案与常见问题排查路径，可满足课程项目“部署运维文档”交付要求。
