# 产品静态读正文链路压测

模拟前端 `ArticleDetail.vue` 在 **staticRead=true** 时的正文加载路径（不含评论、点赞等侧栏请求）。

## 链路

```text
Transaction: Product static read body
  ① GET /api/bbs/article/getReadMeta?id={ARTICLE_ID}
  ② GET ${staticHtmlUrl}          （从 ① 的 JSON 提取，Nginx 直出静态 HTML）
  ③ POST /api/bbs/article/recordPv?id={ARTICLE_ID}
```

## 前置条件

1. `bbs-rest`、`bbs-article-service` 及依赖已启动（含新接口 `getReadMeta`、`recordPv`）。
2. Nginx 监听 80，`bbs.localhosts.com` 已配置：
   - `/api` → `7010`
   - `/static/articles/` → `article-static-html/` 目录
3. `hosts`：`127.0.0.1 bbs.localhost.com`
4. 测试文章 **已发布**，且存在 `article-{id}.html`（可先 `rebuildStaticHtml`）。
5. **不要**在 JMeter 里带作者登录 Cookie（匿名访问才会 `staticRead=true`）。
6. 在测试计划「用户定义变量」里把 `ARTICLE_ID` 改成真实 id（默认 `42`）。

## 参数（与课程基线一致）

| 项 | 值 |
|----|-----|
| 线程 | 500 |
| Ramp-Up | 10s |
| 持续时间 | 300s |
| 限流 | Constant Throughput Timer 12000/min（全组共享，约 200 req/s） |

## 运行

### GUI

打开 `ArticleProductStaticRead.jmx` → 确认 `ARTICLE_ID` → 运行。

### 命令行 + Dashboard

```powershell
cd "d:\D\大学学习\大三下\分布式\大作业\Blog\测试文件_yzx\product-static-read"

jmeter -n -t "ArticleProductStaticRead.jmx" -l report\results.jtl -e -o report\dashboard
```

报告：`report/dashboard/index.html`

## 看结果

- 重点看事务 **`Product static read body`** 的 RT 分位与错误率。
- 子步骤 **`1 getReadMeta` / `2 fetch static HTML` / `3 recordPv`** 可拆分瓶颈。
- 若大量失败且断言提示 `staticRead:false`，检查静态文件是否存在、是否用了作者账号 Cookie。

## 与旧 T1/T2 的区别

| 计划 | 测什么 |
|------|--------|
| T1 getById | 纯动态 API（优化前基线） |
| T2 GETarticle | 纯静态 URL |
| **本计划** | **产品接入后的正文三步链路** |

对比优化效果：仍可用 **T1 vs 本计划** 的 `Product static read body` 事务 RT。
