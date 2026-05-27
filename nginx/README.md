# Nginx 文章正文静态化

本机 Nginx 配置目录：`D:\D\nginx\nginx-1.30.0\conf\nginx.conf`（**不在 Git 仓库内**，每人本机自行配置）

## 1. 后端（可提交 Git）

`bbs-article-service` 的 `application.yml` 使用**相对路径**，协作时无需改仓库：

```yaml
article.reading.static-html.enabled: true
article.reading.static-html.directory: ${ARTICLE_STATIC_HTML_DIR:./article-static-html}
article.reading.static-html.public-url-prefix: /static/articles
```

- 默认在 **`bbs-article-service` 模块根目录** 下生成 `article-static-html/article-{id}.html`（IDE 直接运行 `ArticleApplication` 时即如此）
- 启动日志会打印实际解析后的绝对路径，便于核对
- 若工作目录不是模块根，可设环境变量（**不要写进 application.yml**）：

```bat
set ARTICLE_STATIC_HTML_DIR=D:\your\clone\bbs-springboot\bbs-article\bbs-article-service\article-static-html
```

**批量重建已有文章**（需超级管理员登录）：

```http
POST /api/bbs/article/rebuildStaticHtml
```

## 2. Nginx（每人本机配置）

Nginx 的 `alias` **必须是本机绝对路径**，指向你 clone 仓库里的同一目录，例如：

```nginx
location /static/articles/ {
    alias D:/你的路径/Blog/bbs-springboot/bbs-article/bbs-article-service/article-static-html/;
    add_header Cache-Control "public, max-age=3600";
    access_log off;
    gzip on;
}
```

将 `D:/你的路径/...` 换成你本机 clone 路径；与后端日志里打印的目录一致即可。

修改后执行：

```bat
cd /d D:\D\nginx\nginx-1.30.0
nginx -t
nginx -s reload
```

## 3. 验证

`http://bbs.localhost.com/static/articles/article-1.html`（将 `1` 换成真实已发布文章 id）

应直接返回 HTML，且 **不经过** `7010` 后端。

## 4. 压测

高并发「读正文」应打静态 URL，而不是 `GET /api/bbs/article/getById`。

示例：`GET http://bbs.localhost.com/static/articles/article-1.html`

## 5. 协作小结

| 配置 | 是否进 Git | 说明 |
|------|-----------|------|
| `application.yml` 相对路径 | 是 | 全队共用 |
| `ARTICLE_STATIC_HTML_DIR` | 否 | 本机环境变量，可选 |
| Nginx `alias` | 否 | 本机 nginx.conf，每人路径不同 |
| `article-static-html/*.html` | 否 | 运行时生成，已加入 `.gitignore` |
