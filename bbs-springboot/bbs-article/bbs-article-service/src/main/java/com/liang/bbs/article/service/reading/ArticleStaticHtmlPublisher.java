package com.liang.bbs.article.service.reading;

import com.liang.bbs.article.facade.dto.ArticleMarkdownInfo;
import com.liang.bbs.article.persistence.entity.ArticlePo;
import com.liang.bbs.article.persistence.mapper.ArticlePoMapper;
import com.liang.bbs.article.service.config.ArticleReadingProperties;
import com.liang.bbs.common.enums.ArticleStateEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;
import org.springframework.web.util.HtmlUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

//将已发布文章写成独立HTML文件，供Nginx直接返回
@Slf4j
@Component
@RequiredArgsConstructor
public class ArticleStaticHtmlPublisher {

    private final ArticleReadingProperties articleReadingProperties;
    private final ArticlePoMapper articlePoMapper;
    private final MongoTemplate mongoTemplate;

    //对外浏览器路径，用于回填ArticleDTO.staticHtmlUrl
    public String buildPublicUrl(Integer articleId) {
        String prefix = articleReadingProperties.getStaticHtml().getPublicUrlPrefix();
        if (StringUtils.isBlank(prefix)) {
            prefix = "/static/articles";
        }
        if (!prefix.startsWith("/")) {
            prefix = "/" + prefix;
        }
        String normalized = prefix.endsWith("/") ? prefix.substring(0, prefix.length() - 1) : prefix;
        return normalized + "/article-" + articleId + ".html";
    }

    public Path resolveFilePath(Integer articleId) {
        String dir = articleReadingProperties.getStaticHtml().getDirectory();
        return Paths.get(dir).resolve("article-" + articleId + ".html").normalize();
    }

    //文章审核通过或内容更新后调用：生成/覆盖静态页
    public void publish(Integer articleId) {
        if (!articleReadingProperties.getStaticHtml().isEnabled() || articleId == null) {
            return;
        }
        ArticlePo po = articlePoMapper.selectByPrimaryKey(articleId);
        if (po == null || Boolean.TRUE.equals(po.getIsDeleted())) {
            unpublish(articleId);
            return;
        }
        if (!ArticleStateEnum.enable.getCode().equals(po.getState())) {
            unpublish(articleId);
            return;
        }
        ArticleMarkdownInfo body = getMarkdownByArticleId(articleId);
        if (body == null || StringUtils.isBlank(body.getArticleHtml())) {
            log.warn("静态页跳过：无正文articleId={}", articleId);
            return;
        }
        String title = StringUtils.defaultString(po.getTitle());
        String safeTitle = HtmlUtils.htmlEscape(title);
        String page = buildMinimalHtml(safeTitle, body.getArticleHtml());
        Path path = resolveFilePath(articleId);
        try {
            Files.createDirectories(path.getParent());
            Files.writeString(path, page, StandardCharsets.UTF_8);
            log.debug("已写入静态文章页 {}", path);
        } catch (IOException e) {
            log.error("写入静态文章页失败articleId={} path={}", articleId, path, e);
        }
    }

    //下架、删除或非启用状态时移除静态文件
    public void unpublish(Integer articleId) {
        if (!articleReadingProperties.getStaticHtml().isEnabled() || articleId == null) {
            return;
        }
        Path path = resolveFilePath(articleId);
        try {
            Files.deleteIfExists(path);
        } catch (IOException e) {
            log.warn("删除静态文章页失败articleId={} path={}", articleId, path, e);
        }
    }

    private ArticleMarkdownInfo getMarkdownByArticleId(Integer articleId) {
        Query query = new Query(Criteria.where("articleId").is(articleId));
        return mongoTemplate.findOne(query, ArticleMarkdownInfo.class);
    }

    private static String buildMinimalHtml(String escapedTitle, String articleHtmlBody) {
        return "<!DOCTYPE html>\n"
                + "<html lang=\"zh-CN\">\n<head>\n"
                + "<meta charset=\"UTF-8\"/>\n"
                + "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\"/>\n"
                + "<title>" + escapedTitle + "</title>\n"
                + "<style>body{font-family:sans-serif;max-width:900px;margin:24px auto;line-height:1.6;padding:0 12px}"
                + ".article-html img{max-width:100%;height:auto}</style>\n"
                + "</head>\n<body>\n<article>\n<h1>" + escapedTitle + "</h1>\n"
                + "<div class=\"article-html\">" + articleHtmlBody + "</div>\n"
                + "</article>\n</body>\n</html>\n";
    }
}
