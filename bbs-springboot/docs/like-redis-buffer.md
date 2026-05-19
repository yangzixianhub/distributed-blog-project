# 点赞 Redis 缓冲方案

## 方案口径

- 采用 **写先入 Redis，再定时批量落库**。
- 文章点赞与评论点赞采用同一套策略，分别使用不同 key 前缀，最终分别落到 `fs_like` 与 `fs_comment_like`。
- Redis 故障时，点赞写请求降级为同步写 MySQL；点赞数与是否已赞查询回源 MySQL。

## 键设计

- 文章点赞数：`ns:like:article:count:{articleId}`
- 文章用户状态：`ns:like:article:state:{articleId}`，Hash field 为 `userId`
- 文章脏数据：`ns:like:article:dirty`
- 评论点赞数：`ns:like:comment:count:{commentId}`
- 评论用户状态：`ns:like:comment:state:{commentId}`，Hash field 为 `userId`
- 评论脏数据：`ns:like:comment:dirty`
- 重试：`...:retry`
- 死信：`...:dlq`
- 最近变更目标集合：`...:recent`

## 一致性与并发

- 写入时按 `目标类型 + 目标 id + 用户 id` 使用分布式锁串行化，保持“行存在 + state 取反”的原语义。
- MySQL 表建议保留唯一约束：
  - `fs_like(article_id, like_user)`
  - `fs_comment_like(comment_id, like_user)`
- 缓存中的脏数据只保留最终状态，后台任务每 10 秒刷库一次。
- 刷库失败最多重试 3 次，之后写入死信列表，便于人工或后续任务补偿。
- 每 5 分钟执行一次对账；仅对已无待刷脏数据的目标比对 Redis 与 DB 点赞数，发现不一致时用 DB 修正缓存。

## 风险与测试建议

- 多实例压测：
  - 同一用户对同一文章并发连点，最终状态与点击次数奇偶一致。
  - 多用户并发点赞同一文章，缓存计数与最终 DB 行数一致。
- 故障演练：
  - 关闭 Redis 后，读请求可回源 DB，写请求改为同步落库。
  - 关闭 MySQL 后，脏数据应重试并最终进入 DLQ。
- 迁移提示：
  - 若线上库已有重复行，先清洗重复数据，再补唯一索引。
