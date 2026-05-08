# 智能社区服务平台 API 接口设计

## 1. 接口规范

### 1.1 基础地址

- 前端代理访问：`/api`
- 本地直连后端：`http://localhost:8674/api`

### 1.2 统一响应格式

```json
{
  "code": 0,
  "message": "success",
  "data": {}
}
```

说明：

- `code = 0` 表示成功。
- 非 0 表示业务失败，错误文本由 `message` 提供。

### 1.3 鉴权方式

- 除登录、注册、健康检查、OpenAPI 文档外，其余接口均需在请求头中携带：

```http
Authorization: Bearer <JWT>
```

## 2. 认证与用户接口

### 2.1 用户注册

- 方法：`POST`
- 路径：`/api/auth/register`
- 鉴权：否

请求体：

```json
{
  "username": "resident_demo",
  "password": "123456",
  "role": "RESIDENT",
  "fullName": "张晨",
  "phone": "13800001111",
  "email": "resident@community.local",
  "idCardNo": "310xxxxxxxxxxxxxxx"
}
```

返回重点：

- `token`
- `user.id`
- `user.username`
- `user.role`
- `user.realNameVerified`

### 2.2 用户登录

- 方法：`POST`
- 路径：`/api/auth/login`
- 鉴权：否

请求字段：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `username` | string | 用户名 |
| `password` | string | 密码 |

### 2.3 退出登录

- 方法：`POST`
- 路径：`/api/auth/logout`
- 鉴权：是

### 2.4 当前用户信息

- 方法：`GET`
- 路径：`/api/auth/me`
- 鉴权：是

返回字段：

- `id`
- `username`
- `role`
- `fullName`
- `phone`
- `email`
- `realNameVerified`
- `idCardMasked`（居民可见）

### 2.5 个人资料查询

- 方法：`GET`
- 路径：`/api/profile`
- 鉴权：是

### 2.6 个人资料更新

- 方法：`PUT`
- 路径：`/api/profile`
- 鉴权：是

请求体：

```json
{
  "fullName": "张晨",
  "phone": "13800001111",
  "email": "resident@community.local"
}
```

## 3. 系统总览接口

### 3.1 个人中心总览

- 方法：`GET`
- 路径：`/api/system/overview`
- 鉴权：是

返回内容：

- `profile`
- `pendingRepairCount`
- `joinedActivityCount`
- `unreadMessageCount`
- `notices`
- `onlineUsers`
- `uptimeMinutes`

### 3.2 服务商列表

- 方法：`GET`
- 路径：`/api/system/providers`
- 鉴权：是

### 3.3 智能通知

- 方法：`GET`
- 路径：`/api/system/intelligent-notices`
- 鉴权：是

返回内容：

- `personalizedRecommendations`
- `weatherReminder`
- `generatedAt`

## 4. 报修模块接口

### 4.1 创建报修

- 方法：`POST`
- 路径：`/api/repairs`
- 鉴权：是

请求体：

```json
{
  "title": "楼道照明故障",
  "description": "2号楼3单元楼道灯不亮，请尽快处理。",
  "imageUrl": "/api/files/demo.jpg"
}
```

返回：

```json
{
  "repairId": 1
}
```

### 4.2 上传报修图片

- 方法：`POST`
- 路径：`/api/repairs/upload-image`
- 鉴权：是
- 类型：`multipart/form-data`

### 4.3 查询我的报修

- 方法：`GET`
- 路径：`/api/repairs/my`
- 鉴权：是

### 4.4 查询工单看板

- 方法：`GET`
- 路径：`/api/repairs/board`
- 鉴权：是
- 角色：物业管理员、服务商

### 4.5 更新工单状态

- 方法：`PATCH`
- 路径：`/api/repairs/{id}/status`
- 鉴权：是
- 角色：物业管理员、服务商

请求体：

```json
{
  "status": "IN_PROGRESS",
  "assignedProviderId": 3
}
```

### 4.6 报修评价

- 方法：`PATCH`
- 路径：`/api/repairs/{id}/rating`
- 鉴权：是

请求体：

```json
{
  "rating": 5,
  "comment": "处理及时，沟通顺畅"
}
```

## 5. 活动模块接口

### 5.1 活动列表

- 方法：`GET`
- 路径：`/api/activities`
- 鉴权：是

### 5.2 我的活动

- 方法：`GET`
- 路径：`/api/activities/my`
- 鉴权：是

### 5.3 创建活动

- 方法：`POST`
- 路径：`/api/activities`
- 鉴权：是

请求体：

```json
{
  "title": "周末亲子阅读会",
  "description": "欢迎居民携带孩子参与阅读分享。",
  "location": "社区活动室A",
  "startTime": "2026-03-20 10:00:00"
}
```

### 5.4 活动报名

- 方法：`POST`
- 路径：`/api/activities/{id}/signup`
- 鉴权：是

### 5.5 获取签到二维码

- 方法：`GET`
- 路径：`/api/activities/{id}/checkin-qrcode`
- 鉴权：是

返回字段：

- `activityId`
- `token`
- `signStatus`
- `checkinPayload`

### 5.6 确认签到

- 方法：`POST`
- 路径：`/api/activities/{id}/checkin`
- 鉴权：是

请求体：

```json
{
  "token": "签到动态口令"
}
```

### 5.7 获取活动回顾

- 方法：`GET`
- 路径：`/api/activities/{id}/reviews`
- 鉴权：是

### 5.8 发布活动回顾

- 方法：`POST`
- 路径：`/api/activities/{id}/reviews`
- 鉴权：是

请求体：

```json
{
  "rating": 5,
  "content": "活动组织有序，体验很好",
  "photoUrl": "/api/files/review.jpg"
}
```

## 6. 物业服务接口

说明：`/api/property/*` 仅居民角色可访问。

### 6.1 物业总览

- 方法：`GET`
- 路径：`/api/property/overview`

返回内容：

- `payments`
- `paymentTransactions`
- `parkingSlots`
- `visitors`

### 6.2 登记访客

- 方法：`POST`
- 路径：`/api/property/visitors`

请求体：

```json
{
  "visitorName": "赵磊",
  "visitTime": "2026-03-18 09:30:00"
}
```

### 6.3 生成支付单

- 方法：`POST`
- 路径：`/api/property/payments/{id}/pay-intent`

请求体：

```json
{
  "channel": "ALIPAY"
}
```

返回字段：

- `paymentId`
- `payNo`
- `channel`
- `amount`
- `status`
- `qrPayload`

### 6.4 确认支付

- 方法：`POST`
- 路径：`/api/property/payments/{id}/confirm`

请求体：

```json
{
  "payNo": "PAYXXXXXXXXXXXXXXXXXX"
}
```

## 7. 消息中心接口

### 7.1 消息列表

- 方法：`GET`
- 路径：`/api/messages`
- 鉴权：是

### 7.2 标记已读

- 方法：`PATCH`
- 路径：`/api/messages/{id}/read`
- 鉴权：是

## 8. 邻里互动接口

### 8.1 帖子列表

- 方法：`GET`
- 路径：`/api/neighborhood/posts`
- 鉴权：是

查询参数：

| 参数 | 说明 |
| --- | --- |
| `category` | 分类过滤 |
| `page` | 页码 |
| `pageSize` | 每页数量 |

### 8.2 创建帖子

- 方法：`POST`
- 路径：`/api/neighborhood/posts`
- 鉴权：是

请求体：

```json
{
  "category": "SECOND_HAND",
  "title": "转让九成新婴儿推车",
  "content": "仅使用三个月，支持当面验货。",
  "price": 280,
  "contact": "resident_demo"
}
```

### 8.3 关闭帖子

- 方法：`PATCH`
- 路径：`/api/neighborhood/{id}/close`
- 鉴权：是

## 9. 公共接口

### 9.1 健康检查

- 方法：`GET`
- 路径：`/api/health`
- 鉴权：否

### 9.2 OpenAPI 文档

- 方法：`GET`
- 路径：`/api/openapi.json`
- 鉴权：否

### 9.3 文件访问

- 方法：`GET`
- 路径：`/api/files/{fileName}`
- 鉴权：否

## 10. WebSocket 实时通知

- 连接地址：`/ws/notifications?token=<JWT>`
- 主要事件：
  - `connected`
  - `message_created`
  - `message_read`
  - `pong`

## 11. 接口设计结论

当前 API 已覆盖用户认证、个人中心、报修、活动、物业、消息、邻里互动、实时通知等主要业务模块，并已提供 OpenAPI 在线查看入口，具备课程项目展示与二次开发基础。
