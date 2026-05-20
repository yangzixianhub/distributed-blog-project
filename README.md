# Elasticsearch 搜索改造 README

## 概述

本文档用于说明本项目 Elasticsearch 搜索改造的设计目标、当前实现、代码落点、数据链路、压测结果，以及与《分布式改造工作清单.md》中“三、搜索：Elasticsearch 改造”条目的对应关系。

本次 README 主要基于以下材料整理并扩写：

- [分布式改造工作清单.md](/E:/Blog/分布式改造工作清单.md:1)
- [ES分布式改造.md](/E:/Blog/ES分布式改造.md:1)
- [jmeter/reports/dashboard/statistics.json](/E:/Blog/jmeter/reports/dashboard/statistics.json:1)
- [jmeter/article-search-test.jmx](/E:/Blog/jmeter/article-search-test.jmx:1)
- [jmeter/search-data.csv](/E:/Blog/jmeter/search-data.csv:1)

当前这轮改造已经不再是“只做了一个 Demo 搜索框”，而是已经完成了 ES 主链路打通：

- 已接入 Elasticsearch 官方 Java Client
- 已完成文章索引 mapping 和 IK 分词器接入
- 已支持标题、正文、标签、作者名等字段参与检索
- 已支持文章创建、更新、审核、删除时的 ES 增量同步
- 已提供全量重建索引接口
- 已提供独立搜索接口和搜索健康检查接口
- 前端 `/search?query=...` 已接入新搜索链路
- 已支持标题高亮与正文片段高亮

但同时，这套能力距离“生产级最终一致性 + 完整运维化”仍有明显差距，尤其体现在 MQ 化、对账、索引别名切换、补偿机制等方面。

## 改造目标

根据《分布式改造工作清单.md》中“三、搜索：Elasticsearch 改造”的要求，本轮改造的核心目标包括：

1. 用 Elasticsearch 替代 MySQL 标题 `LIKE '%关键词%'` 搜索。
2. 支持正文全文检索，而不是只查标题。
3. 让搜索能力具备更好的扩展空间，包括相关性优化、字段加权、时间筛选、高亮等。
4. 与 MySQL、Mongo 中的文章主数据保持最终一致。
5. 为后续的分布式演进打基础，包括异步同步、索引重建、压测验证和运维观测。

## 改造范围

本次改造主要涉及以下模块：

- `bbs-rest`
- `bbs-article-service`
- `bbs-vue-ui`
- `jmeter`

关键代码入口包括：

- [ArticleController.java](/E:/Blog/bbs-springboot/bbs-rest/src/main/java/com/liang/bbs/rest/controller/ArticleController.java:1)
- [ArticleSearchDTO.java](/E:/Blog/bbs-springboot/bbs-article/bbs-article-facade/src/main/java/com/liang/bbs/article/facade/dto/ArticleSearchDTO.java:1)
- [ArticleDTO.java](/E:/Blog/bbs-springboot/bbs-article/bbs-article-facade/src/main/java/com/liang/bbs/article/facade/dto/ArticleDTO.java:1)
- [ArticleServiceImpl.java](/E:/Blog/bbs-springboot/bbs-article/bbs-article-service/src/main/java/com/liang/bbs/article/service/impl/ArticleServiceImpl.java:1)
- [ArticleSearchServiceImpl.java](/E:/Blog/bbs-springboot/bbs-article/bbs-article-service/src/main/java/com/liang/bbs/article/service/search/impl/ArticleSearchServiceImpl.java:1)
- [ArticleSearchPageResult.java](/E:/Blog/bbs-springboot/bbs-article/bbs-article-service/src/main/java/com/liang/bbs/article/service/search/ArticleSearchPageResult.java:1)
- [Index.vue](/E:/Blog/bbs-vue-ui/src/components/index/Index.vue:1)
- [FrontPageArticle.vue](/E:/Blog/bbs-vue-ui/src/components/article/FrontPageArticle.vue:1)

## 架构设计

### 总体思路

搜索改造采用“ES 负责召回与排序，MySQL/Mongo 负责主数据回源”的方式：

1. 前端发起搜索请求。
2. `bbs-rest` 暴露搜索 API。
3. `bbs-article-service` 调用 ES 完成全文查询。
4. ES 返回命中文档 ID、总数、排序结果和高亮片段。
5. 服务层再按 ID 回源 MySQL 与 Mongo，补全文章展示数据。
6. 最终将高亮标题、高亮正文片段和文章基础信息返回给前端。

这样做的好处是：

- 搜索能力不再受限于 MySQL `LIKE`
- 文章详情展示仍以主库为准
- 后续可以逐步增加更多搜索字段和排序策略
- 即使 ES 查询逻辑迭代，也不会直接破坏文章主数据模型

### 数据源职责划分

- MySQL：保存文章主记录、状态、标签关联、作者 ID 等结构化数据。
- Mongo：保存正文内容，作为全文索引的重要来源。
- Elasticsearch：保存面向检索的搜索文档，用于召回、排序和高亮。

### 发布时的数据落库顺序

当前文章发布链路是：

1. 先写 MySQL 文章主表
2. 再写文章标签关系
3. 再写 Mongo 正文
4. 最后同步 ES 文档

也就是说，从 “Mongo 和 ES 谁先写” 这个角度看，答案是：

`MySQL -> Mongo -> ES`

## 基础设施实现

### Elasticsearch 客户端接入

项目已接入 Elasticsearch 官方 Java Client，而不是继续停留在数据库模糊查询层。

当前基础设施能力包括：

- Elasticsearch 客户端配置
- 索引初始化
- 连接开关配置
- 文章索引名配置
- 搜索健康检查接口

关键配置位于：

- [bbs-article-service application.yml](/E:/Blog/bbs-springboot/bbs-article/bbs-article-service/src/main/resources/application.yml:1)
- [bbs-rest application.yml](/E:/Blog/bbs-springboot/bbs-rest/src/main/resources/application.yml:1)

当前常见本地端口为：

- `7010`：`bbs-rest`
- `7011`：`bbs-article-service`
- `9200`：Elasticsearch
- `8082`：前端开发服务

### 分词插件

当前实现已接入 IK 分词器，用于中文文本分词。它是这轮改造中“支持中文全文检索”的关键前置条件。

如果 ES 没有安装 IK，常见影响包括：

- 索引初始化失败
- 搜索行为异常
- 查询降级回 MySQL 标题模糊搜索

## 索引与文档模型

### 当前索引名

- `bbs_article`

### 当前索引字段

根据代码与阶段文档，当前搜索文档已覆盖以下核心字段：

- `articleId`
- `title`
- `content`
- `state`
- `isDeleted`
- `labelNames`
- `createUser`
- `createUserName`
- `createTime`

这说明当前实现已经明显超出最初“只索引 articleId/title/content/state/isDeleted”的雏形版本，开始向清单中的完整文档模型靠拢。

### 搜索权重设计

当前查询已引入基础字段加权：

- `title^4`
- `content`
- `labelNames^2`
- `createUserName^1.5`

这意味着：

- 标题命中优先级最高
- 正文命中负责保证全文可搜
- 标签可以增强分类召回
- 作者名具备一定弱召回能力

### 高亮字段

当前返回结果已支持：

- `highlightTitle`
- `highlightContent`

其中：

- `highlightTitle` 用于搜索结果标题高亮
- `highlightContent` 用于搜索结果摘要或正文片段高亮

## 数据同步方案

### 增量同步

当前文章相关操作已经会触发 ES 增量同步，主要包括：

- 文章创建
- 文章更新
- 审核状态变更
- 文章删除

本轮实现已经从“主流程同步直写 ES”提升为“应用内异步执行 + 简单重试”的阶段版本，但仍然不是最终的 MQ 方案。

这意味着当前状态是：

- 比最早的同步直写更稳
- 比生产级异步消息架构仍然简单

### 全量重建

项目已提供全量重建接口：

- `POST /api/bbs/article/rebuildSearchIndex`

重建流程主要用于：

- 首次接入 ES 后补建历史文章索引
- 索引结构升级后的全量修复
- 增量同步失败后的人工恢复

重建成功后通常会返回：

- `indexedCount`
- `costMillis`

### 当前同步能力评价

当前同步链路已经具备“能用、可恢复、可观测到部分问题”的基础能力，但仍缺少完整闭环：

- 不是 MQ 驱动
- 没有死信队列
- 没有失败补偿任务
- 没有 DB/ES 定期对账任务
- 没有全量抽样校验机制

## API 设计

### 独立搜索接口

当前已新增独立搜索接口：

- `GET /api/bbs/article/search`

这比“在 `getList` 里偷偷分流”更符合清单要求，也更便于后续维护和压测。

### 搜索健康检查接口

当前还提供搜索健康检查接口：

- `GET /api/bbs/article/searchHealth`

它主要用于：

- 管理员排查 ES 是否可用
- 本地联调排查连接问题
- 区分“接口问题”和“ES 基础设施问题”

### 当前支持的查询参数

搜索接口目前支持：

- `title`：搜索关键词
- `currentPage`：当前页码
- `pageSize`：分页大小
- `timeRange`：时间范围筛选
- `articleStateEnum`：文章状态，可选

其中 `timeRange` 当前支持：

- `day`
- `week`
- `month`
- `year`
- `older`

### 回源方式

当前搜索链路不是“ES 中直接保存完整展示页数据”，而是：

1. ES 返回命中文档 ID 与排序结果
2. 服务层再回源 MySQL / Mongo
3. 再将高亮片段挂回 DTO 返回前端

这种实现方式满足了清单中“ES 查询 -> 返回 id 列表与排序 -> 按需回源 MySQL/Mongo”的设计方向。

## 前端接入

### 搜索入口

前端当前仍保留：

- `/search?query=...`

但在内部调用链上，已经切换到新的 ES 搜索接口，而不是继续依赖旧的文章列表模糊查询。

### 搜索结果展示

当前前端搜索结果页已支持：

- 标题高亮
- 正文片段高亮
- 若某条结果未带高亮片段，则前端按查询词做本地兜底高亮

因此，这轮改造不仅解决了“正文明明有关键词但搜不到”的问题，也提升了结果可读性。

## 当前完成度评估

结合 `ES分布式改造.md` 的阶段结论，当前完成度可以整理为：

- 基础设施：`85%`
- 索引与文档模型：`75%`
- 数据同步：`70%`
- API：`85%`
- 前端接入：`80%`
- 对账与运维化：`10%`

整体判断为：

> ES 搜索已经具备“可搜索、可高亮、可重建、可增量同步、可前端接入、可压测”的阶段性交付能力，但距离生产级最终一致性和运维闭环仍有明显差距。

## 与改造清单的对应关系

### 已满足或基本满足的要求

对照《分布式改造工作清单.md》中“三、搜索：Elasticsearch 改造”，当前已经满足或基本满足的内容包括：

- 已部署并接入 ES
- 已在 Spring 侧接入官方 Java API
- 已定义文章搜索索引 mapping
- 已接入 IK 中文分词
- 已支持正文从 Mongo 同步到 ES
- 已支持文章创建、更新、审核、删除的增量同步
- 已支持全量重建索引
- 已新增独立搜索接口
- 前端 `/search + query` 已切到新搜索链路
- 已验证相关性、分页、降级和高亮
- 已开展 JMeter 压测并沉淀报告

### 仍未满足或仅部分满足的要求

目前仍未完全达到清单要求的部分包括：

- 增量同步还不是 MQ 方案
- 没有死信、补偿和持久化失败处理
- 没有 DB/ES 定期对账机制
- 没有全量校验与抽样校验任务
- 没有索引别名切换方案
- 没有零停机重建方案
- 没有统一监控接入
- 搜索相关性仍较基础，缺少更复杂的时间衰减、联想词、拼写纠错、热门推荐等能力

## 本轮关键问题与修复记录

本轮改造过程中，除了实现功能，也修复了一批影响联调与验证的问题。

### 1. 搜索接口权限误拦截

出现过公开搜索接口被权限拦截器误伤的问题。原因是：

- 接口虽然标了免登录注解
- 但登录态下请求仍会经过 URL 权限检查

修复后，公开搜索能力可正常使用。

### 2. 控制器参数绑定异常

在 Spring Boot 3 / Spring MVC 新版本下，若 `@RequestParam` 未显式写参数名，而编译结果又没带 `-parameters`，会出现参数绑定失败。

这类问题会导致：

- 点赞失败
- 用户主页查询失败
- 部分搜索相关接口抛出“服务器异常”

本轮已通过显式补齐 `@RequestParam("xxx")` 的方式修复。

### 3. 搜索为空但正文明明有词

出现过“正文明明有 `node`，但搜索为空”的情况，根因主要有：

- ES 不可用时回退到 MySQL 标题 `LIKE`
- 旧索引结构与新查询字段不一致
- 历史文章未执行重建，导致正文未真正写入 ES

修复和处理方式包括：

- 新增独立搜索接口
- 兼容旧索引行为
- 补充索引重建能力
- 接入高亮与更完整的字段集合

### 4. 重建接口权限与联调问题

在 Swagger/Knife4j 联调阶段，重建接口还暴露出：

- 登录态依赖 Cookie
- 本地域名与 Cookie 域名不一致时无法带上登录态
- 新接口未进入路径权限表时会返回“无权访问接口”

因此，本轮也补充了这部分兼容处理与联调说明。

## JMeter 压测说明

### 压测文件

JMeter 相关文件位于：

- [jmeter/article-search-test.jmx](/E:/Blog/jmeter/article-search-test.jmx:1)
- [jmeter/search-data.csv](/E:/Blog/jmeter/search-data.csv:1)
- [jmeter/README.md](/E:/Blog/jmeter/README.md:1)
- [jmeter/reports/article-search-results.jtl](/E:/Blog/jmeter/reports/article-search-results.jtl:1)
- [jmeter/reports/dashboard/index.html](/E:/Blog/jmeter/reports/dashboard/index.html:1)
- [jmeter/reports/dashboard/statistics.json](/E:/Blog/jmeter/reports/dashboard/statistics.json:1)

### 压测接口

本轮压测接口为：

- `GET /api/bbs/article/search`

压测计划文件中的描述也明确写明，该方案用于覆盖：

- 关键词搜索
- 关键词 + 时间筛选
- 仅时间筛选

### 压测参数来源

CSV 数据源来自：

- [jmeter/search-data.csv](/E:/Blog/jmeter/search-data.csv:1)

当前数据样例包括：

- `node,,1,10`
- `java,week,1,10`
- `spring,month,1,10`
- `redis,year,1,10`
- `,day,1,10`
- `,month,1,10`
- `mysql,older,1,10`

这意味着本轮压测覆盖了三类典型搜索模式：

- 有关键词、无时间筛选
- 有关键词、有时间筛选
- 无关键词、仅时间筛选

### 最新 JMeter 场景配置

根据 [jmeter/article-search-test.jmx](/E:/Blog/jmeter/article-search-test.jmx:82)，最新压测配置为：

- 线程数：`1000`
- Ramp-Up：`10` 秒
- 循环次数：`20`

理论请求总数为：

- `1000 * 20 = 20000`

这与最新统计结果中的 `sampleCount = 20000` 完全一致。

### 最新 JMeter 统计结果

根据最新 [statistics.json](/E:/Blog/jmeter/reports/dashboard/statistics.json:1)，本轮 `GET /api/bbs/article/search` 的结果如下：

- 总请求数：`20000`
- 错误数：`0`
- 错误率：`0.0%`
- 平均响应时间：`922.37 ms`
- 中位数响应时间：`1001 ms`
- 最小响应时间：`7 ms`
- 最大响应时间：`1999 ms`
- 吞吐量：`686.84 req/s`
- 接收吞吐：`440.38 KB/s`
- 发送吞吐：`136.26 KB/s`

在当前 dashboard 默认分位点统计下：

- `pct1ResTime = 1410 ms`
- `pct2ResTime = 1493 ms`
- `pct3ResTime = 1590 ms`

如果沿用常见的 dashboard 解释方式，可以把这组数据理解为当前高并发压测下的主要尾延迟分位点区间。

### 对最新压测结果的解读

这组结果相比早期基础压测结果，更接近“高并发下的真实上限压力”：

- 优点是：`20000` 次请求全部成功，错误率为 `0%`
- 优点是：接口在 `1000` 线程级别压力下仍然保持可用
- 优点是：吞吐量达到了约 `686.84 req/s`
- 需要注意的是：平均耗时和中位数已经接近 `1s`
- 需要注意的是：尾延迟明显上升，说明高并发下仍存在性能优化空间

因此，这轮压测更适合得出这样的阶段性结论：

> 当前搜索接口已经具备较好的稳定性和可用性，但在高并发场景下，响应时间仍偏高，系统已经从“功能正确性验证”进入“性能优化阶段”。

### 当前压测结论

综合来看：

- 功能正确性：已验证
- 搜索链路稳定性：已验证
- 高并发可用性：已初步验证
- 高并发低延迟能力：仍需继续优化

## 运行与验证方式

### 搜索接口调用示例

关键词搜索：

```text
http://127.0.0.1:7010/api/bbs/article/search?title=node&currentPage=1&pageSize=10
```

关键词 + 时间筛选：

```text
http://127.0.0.1:7010/api/bbs/article/search?title=node&timeRange=week&currentPage=1&pageSize=10
```

仅时间筛选：

```text
http://127.0.0.1:7010/api/bbs/article/search?timeRange=month&currentPage=1&pageSize=10
```

### 搜索健康检查

```text
GET /api/bbs/article/searchHealth
```

### 索引重建

```text
POST /api/bbs/article/rebuildSearchIndex
```

### 生成 JMeter Dashboard

如果本机已配置 `jmeter` 到 `PATH`：

```powershell
.\generate-dashboard.ps1
```

如果未配置，可显式指定：

```powershell
.\generate-dashboard.ps1 -JMeterBin "D:\apache-jmeter-5.6.3\bin\jmeter.bat"
```

报告入口位于：

```text
E:/Blog/jmeter/reports/dashboard/index.html
```

## 当前风险

结合清单、阶段文档和代码现状，当前主要风险包括：

- 增量同步仍是应用内异步线程方案，不是 MQ 驱动
- ES 与主库的一致性仍是“最终一致性雏形”，不是严格工程闭环
- 重建索引没有 alias 切换，重建过程的线上平滑切流能力不足
- 失败重试、死信、补偿和对账能力不足
- 搜索排序还较基础，缺少更复杂的相关性优化策略
- 高并发压测下延迟偏高，说明性能还有明显优化空间

## 后续建议

建议按以下优先级继续推进：

### 第一优先级

- 增加 DB 与 ES 对账任务
- 增加重建后的数量校验与抽样校验
- 增强增量同步失败日志与告警

### 第二优先级

- 将增量同步升级为 MQ 驱动
- 引入失败重试、死信和补偿机制

### 第三优先级

- 为索引重建增加 alias 切换
- 支持无损重建与平滑切流

### 第四优先级

- 优化搜索相关性排序
- 增强高亮策略
- 支持联想搜索、拼写纠错、热门搜索推荐等扩展能力
- 针对高并发场景继续做性能优化，降低平均响应时间与尾延迟

## 结论

截至当前，这套 Elasticsearch 搜索改造已经完成了“第一阶段可交付版本”的核心目标：

- 能搜标题
- 能搜正文
- 能高亮
- 能分页
- 能按时间筛选
- 能增量同步
- 能重建索引
- 能前端接入
- 能压测验证

因此，这轮改造可以定义为：

> Elasticsearch 搜索主链路已经打通，并具备阶段性交付价值；下一阶段的重点不再是“能不能搜”，而是“是否更稳、更快、更一致、更易运维”。
