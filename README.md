# 分布式博客系统扩充说明

本项目在原有博客系统基础上，扩充了“点赞高并发处理”能力。核心做法是将文章点赞和评论点赞统一接入 Redis 缓冲层，在高峰期先写 Redis，再由后台任务批量落库，从而减少 MySQL 的瞬时写压力。

## 点赞与 Redis 缓冲

本次扩充采用 **写先入 Redis，再异步落库** 的方案。

- 文章点赞最终落到 `fs_like`
- 评论点赞最终落到 `fs_comment_like`
- 点赞数与“当前用户是否已赞”优先从 Redis 读取
- Redis 不可用时，读请求回源 MySQL，写请求降级为同步写库
- 后台任务定时刷库，并带有重试、死信队列和对账逻辑

主要 Redis key：

- `ns:like:article:count:{articleId}`：文章点赞数
- `ns:like:article:state:{articleId}`：文章用户点赞状态
- `ns:like:comment:count:{commentId}`：评论点赞数
- `ns:like:comment:state:{commentId}`：评论用户点赞状态
- `...:dirty`：待落库脏数据
- `...:retry`：失败重试次数
- `...:dlq`：死信数据

为了保持原项目“记录存在，`state` 字段取反”的语义，写入时按“目标类型 + 目标 id + 用户 id”加分布式锁，避免多实例并发下重复点赞或状态乱序。

## 在原项目上的修改方式

本次扩充尽量沿用原有代码结构，没有大范围重写旧逻辑，而是在原有服务层外补充了可复用组件。

### 1. 保留原有服务入口

原来的：

- `LikeServiceImpl`
- `LikeCommentServiceImpl`

仍然作为业务入口使用，只把内部的：

- 点赞数查询
- 是否已赞查询
- 点赞状态切换

改为委托给统一的 Redis 协调组件处理。

### 2. 新增统一缓存协调层

新增了：

- `LikeCacheCoordinator`
- `LikeTargetType`
- `LikeStateRepository`
- `ArticleLikeStateRepository`
- `CommentLikeStateRepository`

其中：

- `LikeCacheCoordinator` 负责缓存读写、状态切换、脏数据记录、重试、死信和对账
- 两个 `Repository` 适配器分别处理文章点赞与评论点赞和数据库之间的映射
- `LikeTargetType` 用于区分文章与评论，并统一生成 key

这样做的好处是：

- 文章点赞和评论点赞可以复用同一套机制
- 对原有服务代码改动较小
- 后续若扩展收藏、关注等类似功能，也可以复用同类设计

### 3. 新增后台任务

新增 `LikeFlushWorker`：

- 每 10 秒将 Redis 中的脏数据批量刷入 MySQL
- 每 5 分钟做一次 Redis 与数据库对账
- 刷库失败时记录重试次数，超过阈值后写入死信队列

### 4. 补充数据库约束

为避免多实例并发下出现重复记录，在建表脚本中补充了唯一索引：

- `fs_like(article_id, like_user)`
- `fs_comment_like(comment_id, like_user)`

如果旧库中已经存在重复数据，需要先清理后再添加唯一约束。

## 本次功能扩充涉及的主要文件

- `bbs-springboot/bbs-user/bbs-user-service/src/main/java/com/liang/bbs/user/service/impl/LikeServiceImpl.java`
- `bbs-springboot/bbs-user/bbs-user-service/src/main/java/com/liang/bbs/user/service/impl/LikeCommentServiceImpl.java`
- `bbs-springboot/bbs-user/bbs-user-service/src/main/java/com/liang/bbs/user/service/like/`
- `bbs-springboot/bbs-user/bbs-user-service/src/main/java/com/liang/bbs/user/service/scheduler/LikeFlushWorker.java`
- `bbs-springboot/bbs-common/src/main/java/com/liang/bbs/common/constant/RedisConstants.java`
- `bbs-springboot/db/open_bbs.sql`

更详细的设计说明见：

- `bbs-springboot/docs/like-redis-buffer.md`

## 验证情况

已完成模块构建验证：

```bash
mvn -pl bbs-user/bbs-user-service -am test -DskipTests
```

构建结果为 `BUILD SUCCESS`。

后续建议继续补充：

- 多实例并发点赞测试
- Redis 故障降级测试
- MySQL 故障下的重试与死信验证
- Redis 与 MySQL 最终一致性压测
