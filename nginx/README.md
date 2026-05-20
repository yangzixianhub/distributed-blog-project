# Nginx 文章正文静态化

本机 Nginx 配置目录：`D:\D\nginx\nginx-1.30.0\conf\nginx.conf`

## 1. 后端

`bbs-article-service` 的 `application.yml`：

```yaml
article.reading.static-html.enabled: true
article.reading.static-html.directory: ./article-static-html
article.reading.static-html.public-url-prefix: /static/articles
```

审核通过或编辑已发布文章后，会在 `bbs-article-service/article-static-html/` 生成 `article-{id}.html`。

已有上线文章需触发一次同步（重新保存/审核通过，或调用会走 `syncStaticHtmlAndRedisCache` 的更新接口）。

## 2. Nginx（bbs.localhost.com）

在 `location /api` **之前**增加：

```nginx
location /static/articles/ {
    alias D:/D/大学学习/大三下/分布式/大作业/Blog/bbs-springboot/bbs-article/bbs-article-service/article-static-html/;
    add_header Cache-Control "public, max-age=3600";
    gzip on;
    gzip_types text/html;
}
```

修改后执行：

```bat
cd /d D:\D\nginx\nginx-1.30.0
nginx -t
nginx -s reload
```

## 3. 验证

浏览器访问（将 `1` 换成真实已发布文章 id）：

`http://bbs.localhost.com/static/articles/article-1.html`

应直接返回 HTML，且 **不经过** `7010` 后端。

## 4. 压测

高并发「读正文」应打静态 URL，而不是 `GET /api/bbs/article/getById`。

示例：`GET http://bbs.localhost.com/static/articles/article-1.html`

## 5. 路径不一致时

若 `bbs-article-service` 工作目录不是模块根目录，把 `application.yml` 的 `directory` 改成绝对路径，并与 Nginx `alias` 指向同一文件夹。
