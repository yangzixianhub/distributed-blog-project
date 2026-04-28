# VBlog 分布式认证拆分说明

本项目已在原单体 `blogserver` 的基础上拆出独立的 `user-service`，用于承接用户、角色、登录认证和 JWT 校验能力。当前阶段的目标是让旧前端和旧业务接口尽量保持可用，同时让登录状态从 Session 迁移到可跨服务使用的 JWT。

## 模块结构

```text
VBlog
├─ blogserver      原博客业务服务，默认端口 8081
├─ user-service    用户与认证服务，默认端口 8082
├─ vueblog         Vue 前端
└─ doc             项目文档与图片
```

### user-service 职责

- 用户登录、注册
- JWT 签发、解析、校验
- 当前用户信息查询
- 用户邮箱修改
- 管理员用户管理
- 角色查询与用户角色修改
- 统一认证异常返回

### blogserver 职责

- 保留文章、分类、标签、统计等博客业务
- 兼容旧前端接口路径
- 从请求头读取 `Authorization: Bearer <token>`
- 调用 `user-service /auth/verify` 校验 token
- 校验成功后把用户身份写入 Spring Security 上下文，兼容旧代码中的 `Util.getCurrentUser()`

## 启动顺序

先初始化用户库：

```bash
mysql -u root -p < user-service/src/main/resources/user-db.sql
```

启动用户服务：

```bash
cd user-service
mvn spring-boot:run
```

启动博客业务服务：

```bash
cd blogserver
mvn spring-boot:run
```

启动前端：

```bash
cd vueblog
npm install
npm run dev
```

## 服务配置

`user-service` 默认数据库为：

```properties
spring.datasource.url=jdbc:mysql:///user_db?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai
server.port=8082
```

`blogserver` 调用用户服务的地址：

```properties
user.service.base-url=http://localhost:8082
server.port=8081
```

## 数据库拆分

用户相关表迁移到 `user_db`：

```text
user
roles
roles_user
```

初始化脚本：

```text
user-service/src/main/resources/user-db.sql
```

默认账号：

```text
username: admin
password: 123
roles: ADMIN, USER
```

密码加密暂时沿用原 VBlog 的 MD5 方式，便于兼容旧数据。后续如果升级 BCrypt，需要设计老密码平滑迁移策略。

## 鉴权契约

所有需要登录的请求统一携带：

```text
Authorization: Bearer <jwt>
```

JWT 中包含：

```json
{
  "userId": 1,
  "username": "admin",
  "roles": ["ROLE_ADMIN", "ROLE_USER"],
  "iat": 1710000000,
  "exp": 1710007200
}
```

统一响应格式：

```json
{
  "code": 200,
  "message": "success",
  "data": {}
}
```

未登录、token 无效或 token 过期：

```json
{
  "code": 401,
  "message": "未登录或 token 无效",
  "data": null
}
```

权限不足：

```json
{
  "code": 403,
  "message": "权限不足",
  "data": null
}
```

## user-service 接口

### 认证接口

```text
POST /auth/login
POST /auth/register
POST /auth/verify
POST /auth/logout
```

登录请求：

```json
{
  "username": "admin",
  "password": "123"
}
```

登录响应：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "token": "jwt-token",
    "tokenType": "Bearer",
    "expiresIn": 7200,
    "user": {
      "id": 1,
      "username": "admin",
      "nickname": "admin",
      "enabled": true,
      "email": "admin@vblog.local",
      "roles": ["ROLE_ADMIN", "ROLE_USER"]
    }
  }
}
```

注册请求：

```json
{
  "username": "newuser",
  "password": "123",
  "nickname": "new user",
  "email": "newuser@vblog.local"
}
```

注册成功后默认绑定 `USER` 角色。

### 当前用户接口

```text
GET /users/me
GET /users/me/roles
PUT /users/me/email
GET /users/{id}
GET /users/by-username/{username}
```

修改邮箱请求：

```json
{
  "email": "new-email@vblog.local"
}
```

### 管理员接口

以下接口需要 `ROLE_ADMIN`：

```text
GET    /admin/users
GET    /admin/users/{id}
PUT    /admin/users/{id}/enabled
PUT    /admin/users/{id}/roles
DELETE /admin/users/{id}
GET    /admin/roles
```

禁用或启用用户：

```json
{
  "enabled": false
}
```

修改用户角色：

```json
{
  "roleIds": [2]
}
```

## blogserver 兼容接口

为了减少前端改动，`blogserver` 仍保留旧路径，并在内部转发到 `user-service`：

```text
POST /login                         -> user-service /auth/login
POST /reg                           -> user-service /auth/register
GET  /currentUserName               -> user-service /users/me
GET  /currentUserEmail              -> user-service /users/me
PUT  /updateUserEmail               -> user-service /users/me/email
GET  /admin/user                    -> user-service /admin/users
GET  /admin/user/{id}               -> user-service /admin/users/{id}
PUT  /admin/user/enabled            -> user-service /admin/users/{id}/enabled
PUT  /admin/user/role               -> user-service /admin/users/{id}/roles
DELETE /admin/user/{id}             -> user-service /admin/users/{id}
GET  /admin/roles                   -> user-service /admin/roles
```

文章、分类、统计等旧业务接口会通过 `JwtAuthenticationFilter` 校验 token，并继续使用 `Util.getCurrentUser().getId()` 获取当前用户 ID。

## 前端对接

`vueblog/src/utils/api.js` 已统一追加认证头：

```text
Authorization: Bearer <token>
```

登录成功后，`Login.vue` 会从 `resp.data.data.token` 读取 JWT 并保存到：

```text
localStorage.token
```

退出登录时删除本地 token。当前 JWT 是无状态方案，服务端不保存 Session。

## 后续开发建议

1. 增加网关服务，统一处理白名单、JWT 校验和用户身份透传。
2. 将 `blogserver` 中旧的用户表访问逻辑彻底删除，避免双写或读错库。
3. 业务服务逐步改为读取网关透传的 `X-User-Id`、`X-Username`、`X-Roles`。
4. 将 MD5 密码加密升级为 BCrypt，并提供旧密码迁移方案。
5. 为 `user-service` 增加接口测试和异常场景测试。
