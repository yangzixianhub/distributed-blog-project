# 社区论坛后端项目说明

## 项目概览

本项目是社区论坛系统的后端部分，采用多模块 Spring Boot 架构，当前仓库中的核心模块如下：

- `bbs-rest`：统一 REST 接口入口，负责前端 API 暴露、登录拦截、权限拦截与控制器层逻辑。
- `bbs-article`：文章域模块，包含文章、标签、评论、搜索等核心能力。
- `bbs-user`：用户域模块，包含点赞、关注、动态、等级等能力。
- `bbs-common`：公共配置、枚举、工具类与基础依赖封装。

当前项目除基础论坛能力外，还在持续推进《分布式改造工作清单.md》中规划的三类改造：

1. 文章阅读静态化：Nginx + Redis
2. 点赞链路缓冲化：Redis + 最终落库
3. 搜索能力升级：Elasticsearch 全文检索

本 README 重点补充第 3 项 Elasticsearch 改造的当前落地情况，并结合最新压测结果给出阶段性说明。

---

## 模块结构

```text
bbs-springboot
├─ bbs-rest
├─ bbs-article
│  ├─ bbs-article-facade
│  ├─ bbs-article-persistence
│  └─ bbs-article-service
├─ bbs-user
│  ├─ bbs-user-facade
│  ├─ bbs-user-persistence
│  └─ bbs-user-service
└─ bbs-common
```

---

## 技术栈

当前项目已经使用或接入的主要技术包括：

- Spring Boot
- Dubbo
- MyBatis
- MySQL
- MongoDB
- Redis
- Elasticsearch
- Vue + Axios（前端在 `bbs-vue-ui` 仓库目录）

搜索改造使用的是：

- `elasticsearch-java`
- Elasticsearch `IK` 中文分词

---

## Elasticsearch 改造背景

在原始实现中，文章搜索主要依赖 MySQL 标题条件：

```sql
title like '%keyword%'
```

这个实现存在几个明显问题：

- 只能搜标题，正文内容无法有效命中。
- 模糊匹配性能与扩展性有限。
- 后续很难支持高亮、相关性排序、联想搜索、拼写纠错等能力。
- 难以支撑更高并发的全文搜索场景。

因此，本轮改造目标是：

- 用 Elasticsearch 替代标题 `LIKE` 搜索。
- 支持正文全文检索。
- 在搜索接口层面与前端 `/search?query=...` 形成完整链路。
- 与 MySQL、Mongo 保持最终一致。

---

## 对照《分布式改造工作清单》的实现状态

以下内容基于：

- [分布式改造工作清单.md](../分布式改造工作清单.md)
- [ES分布式改造.md](../ES分布式改造.md)
- 2026-05-19 最新 JMeter 搜索压测结果

### 1. 基础设施

已完成：

- Elasticsearch 已部署并可访问。
- Spring 侧已接入官方 Java Client。
- 文章服务已完成 ES 连接与索引初始化。
- 已具备搜索健康检查能力。

当前状态：

- ES 索引名：`bbs_article`
- 文章服务配置：`search.elasticsearch.enabled=true`
- 当前索引已可正常创建、重建、查询。

完成度评估：

- `85%`

### 2. 索引与文档模型

已完成：

- 已定义文章搜索索引 mapping。
- 已接入 IK 分词器。
- 已支持的文档字段包括：
  - `articleId`
  - `title`
  - `content`
  - `labelNames`
  - `state`
  - `isDeleted`
  - `createUser`
  - `createUserName`
  - `createTime`

已支持的查询权重包括：

- `title^4`
- `content`
- `labelNames^2`
- `createUserName^1.5`

完成度评估：

- `75%`

### 3. 数据同步

已完成：

- 文章创建后触发 ES 写入。
- 文章更新后触发 ES 写入。
- 审核状态变更后触发 ES 同步。
- 删除文章后触发 ES 删除。
- 已提供全量重建接口。

本轮相较于第一版的提升：

- 已从同步直写 ES 升级为“异步执行 + 简单重试”。
- 已补充重建索引过程中的异常排查与兼容修复。

尚未完成：

- 还未接入 MQ。
- 没有死信队列。
- 没有统一补偿机制。
- 没有 DB/ES 定期对账任务。

完成度评估：

- `70%`

### 4. API 与前端链路

已完成：

- 新增独立搜索接口：`GET /api/bbs/article/search`
- 保留原列表接口：`GET /api/bbs/article/getList`
- 前端 `/search?query=...` 已切换到 ES 搜索接口
- 搜索接口已支持高亮标题与高亮正文片段

当前链路为：

```text
前端 /search?query=keyword
        ↓
/api/bbs/article/search
        ↓
ArticleService.searchArticles(...)
        ↓
ArticleSearchService.search(...)
        ↓
Elasticsearch 查询
        ↓
返回 articleId + 排序 + 高亮
        ↓
按需回源 MySQL / Mongo 补齐文章展示信息
```

完成度评估：

- `85%`

### 5. 验证与回归

已完成：

- 搜索接口实测可返回命中结果。
- 搜索 `node` 已能命中正文包含该词的文章。
- 高亮内容已正确返回到接口层。
- 索引已支持全量重建。

尚未完成：

- ES 不可用时的系统性降级验证仍不完整。
- DB/ES 对账与抽样校验未实现。

---

## 当前 ES 改造完成度总结

按阶段评估如下：

- 基础设施：`85%`
- 索引与文档模型：`75%`
- 数据同步：`70%`
- API：`85%`
- 前端接入：`80%`
- 对账与运维化：`10%`

阶段性结论：

> Elasticsearch 改造已经完成了第一阶段主链路打通，具备“可搜索、可重建、可增量同步、前端可接入”的能力，但距离生产级最终一致和运维化仍有明显差距。

---

## 本轮关键问题与处理记录

本轮实际落地过程中，搜索链路先后暴露出多类问题，已逐步修复：

### 1. 搜索接口被误拦截

问题：

- 搜索接口虽然标记了 `@NoNeedLogin`
- 但登录态请求仍会被权限拦截器继续校验

处理：

- 在 `UrlAccessCheckInterceptor` 中补齐了 `@NoNeedLogin` 判断逻辑

### 2. 搜索接口参数绑定异常

问题：

- 控制器中部分 `@RequestParam` 未显式命名
- 工程未开启 `-parameters`
- Spring MVC 运行时无法解析参数名

处理：

- 为 `search`、`getPersonalArticles`、`getArticleCheckCount` 等接口参数补齐显式 `value`

### 3. 搜索结果为空

问题：

- 旧索引 mapping 与新查询字段不一致
- 查询失败后回退到 MySQL 标题 `LIKE`
- 搜索词在正文存在但标题不存在时，结果表现为空

处理：

- 去除对旧索引不兼容的排序依赖
- 完成索引重建

### 4. 重建接口“表面成功”

问题：

- 原 `rebuildSearchIndex` 返回的 `indexedCount` 只是准备写入数量
- 没有检查 ES bulk 响应是否逐条成功

处理：

- 补充 bulk 错误检测
- 若存在单条失败，将直接抛出异常而不是继续返回成功

### 5. 时间字段兼容问题

问题：

- 索引文档新增 `createTime`
- 时间字段写入 ES 时存在序列化兼容风险

处理：

- 调整写入格式，提升日期字段兼容性

---

## 当前已验证的实际效果

截至 2026-05-19，已确认：

- `bbs_article` 索引可正常存在并返回真实文档数。
- 搜索接口可返回命中结果。
- 搜索 `node` 可命中 2 篇文章。
- 返回结果中包含：
  - `highlightTitle`
  - `highlightContent`
- 前端搜索页已切到新搜索接口。

这意味着：

- 后端 ES 搜索主链路已经打通。
- 从“功能不可用”推进到了“功能已可用且已验证”。

---

## 最新 JMeter 压测结果

### 压测文件

本次文档使用的最新压测文件来自：

- `jmeter/reports/article-search-results.jtl`
- `jmeter/reports/dashboard/statistics.json`
- `jmeter/search-data.csv`

时间基准：

- `2026-05-19`

### 测试目标

压测对象：

- `GET /api/bbs/article/search`

典型请求数据覆盖：

- `node`
- `java`
- `spring`
- `redis`
- `mysql`
- 空关键词
- 不同 `timeRange` 参数组合

### 核心结果

根据 `statistics.json`，本次压测汇总结果如下：

- 总请求数：`20000`
- 错误数：`0`
- 错误率：`0.0%`
- 平均响应时间：`922.37 ms`
- 中位数响应时间：`1001 ms`
- 最小响应时间：`7 ms`
- 最大响应时间：`1999 ms`
- P90（JMeter pct1）：`1410 ms`
- P95（JMeter pct2）：`1493 ms`
- P99（JMeter pct3）：`1590 ms`
- 吞吐量：`686.84 req/s`
- 接收吞吐：`440.38 KB/s`
- 发送吞吐：`136.26 KB/s`

### 结果解读

从当前数据看：

- 搜索接口在本轮压测中没有出现错误样本。
- 在当前环境下，接口具备较稳定的并发处理能力。
- P95 仍在 `1.5s` 左右，说明主链路虽然可用，但仍有进一步优化空间。

优化方向主要包括：

- 查询条件裁剪
- 结果回源字段收缩
- ES 查询与 MySQL 回源链路进一步解耦
- 热查询缓存
- 更细粒度的索引字段设计

---

## 已实现的搜索接口能力

当前已具备以下能力：

- 标题搜索
- 正文全文搜索
- 标签字段参与检索
- 作者展示名参与检索
- 高亮标题返回
- 高亮正文片段返回
- 分页返回
- 全量索引重建
- 增量异步同步

---

## 尚未完成的能力

当前仍未完成或仅完成雏形的内容包括：

- MQ 化增量同步
- 同步失败补偿
- DB/ES 对账任务
- 零停机索引重建
- alias 切换
- 更复杂的相关性排序
- 时间衰减
- 联想搜索
- 拼写纠错
- 热门查询推荐
- 统一监控与告警接入

---

## 后续建议

建议后续按以下顺序推进：

### 第一优先级

- 增加 DB 与 ES 对账任务
- 增加重建后的数量校验与抽样校验
- 增加同步失败告警与监控

### 第二优先级

- 将应用内异步同步升级为 MQ 驱动
- 增加失败重试、死信与补偿机制

### 第三优先级

- 为索引重建引入 alias 切换
- 支持无损重建与平滑切流

### 第四优先级

- 优化搜索排序策略
- 优化高亮展示
- 引入更多搜索体验增强能力

---

## 构建与运行

在仓库根目录下执行：

```bash
mvn clean install
```

按需编译指定模块：

```bash
mvn -pl bbs-rest -am -DskipTests compile
mvn -pl bbs-article/bbs-article-service -am -DskipTests compile
```

---

## 阶段性结论

当前项目的 Elasticsearch 改造已经不再停留在“原型验证”阶段，而是进入了“可交付的阶段性版本”：

- 搜索链路已贯通
- 索引可重建
- 前端已接入
- 实际搜索结果已验证
- 压测结果已具备基础支撑数据

但从严格的分布式工程视角看，仍需继续完善：

- 最终一致性闭环
- 运维可观测性
- 生产级重建策略
- 对账与补偿体系

因此，当前最准确的评价是：

> ES 搜索改造已完成第一阶段可用版本，适合阶段性汇报、课程验收和后续持续演进，但尚未达到完整生产级分布式搜索方案的终态。

