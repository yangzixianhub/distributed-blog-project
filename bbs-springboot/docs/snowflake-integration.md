# 雪花 ID 接入说明

博客后端通过 HTTP 调用仓库根目录的 **DistributedIdService**（.NET），为评论、点赞记录分配全局唯一主键。文章 `fs_article.id` 仍为 MySQL 自增 `INT`，未接入雪花。

## 启动顺序

1. MySQL `open_bbs` 执行 `bbs-springboot/db/migrate_snowflake_ids.sql`（首次接入时）。
2. 启动 Redis（点赞缓冲等依赖）。
3. 启动 ID 服务：

```bash
cd src/DistributedIdService.Api
dotnet run
```

默认监听 `http://127.0.0.1:5000`，API Key 见 `appsettings.json`（与下方 YAML 一致）。

4. 启动 Dubbo 提供方：`bbs-user-service`、`bbs-article-service`。
5. 启动 `bbs-rest` 与前端。

## 配置

`bbs-user-service`、`bbs-article-service` 的 `application.yml`：

```yaml
distributed-id:
  enabled: true
  base-url: http://127.0.0.1:5000
  connect-timeout-ms: 5000
  read-timeout-ms: 5000
  api-keys:
    article: blog-article-service-key
    comment: blog-comment-service-key
    like: blog-like-service-key
```

- `enabled: false`：插入时不设主键，继续走表上的 `AUTO_INCREMENT`。
- ID 服务不可用且 `enabled: true` 时，会打 WARN 日志并同样回退自增。

## 写路径

| 业务 | 类 | Scope / Key |
|------|-----|-------------|
| 发表评论 | `CommentServiceImpl#create` | `COMMENT` / `blog-comment-service-key` |
| 文章点赞 | `ArticleLikeStateRepository` | `LIKE` / `blog-like-service-key` |
| 评论点赞 | `CommentLikeStateRepository` | `LIKE` / `blog-like-service-key` |

## 前端注意

评论 `id`、`preId` 在 JSON 中以**字符串**序列化（`ToStringSerializer`），避免 JavaScript 大整数精度丢失。点赞、删评论等接口的 `commentId` 请传该字符串或同等数值。

## 代码位置

- 客户端与自动配置：`bbs-common` → `com.liang.bbs.common.distributedid`
- 远程服务：`src/DistributedIdService.Api`
