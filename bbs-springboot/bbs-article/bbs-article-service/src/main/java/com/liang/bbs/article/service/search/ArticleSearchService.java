package com.liang.bbs.article.service.search;

import com.liang.bbs.article.facade.dto.ArticleDTO;
import com.liang.bbs.article.facade.dto.ArticleSearchHealthDTO;
import com.liang.bbs.article.facade.dto.ArticleSearchDTO;
import com.liang.bbs.common.enums.ArticleStateEnum;

import java.util.List;

/**
 * Article search abstraction for Elasticsearch integration.
 */
public interface ArticleSearchService {

    boolean isEnabled();

    boolean supports(ArticleSearchDTO articleSearchDTO);

    ArticleSearchPageResult search(ArticleSearchDTO articleSearchDTO, ArticleStateEnum articleStateEnum);

    boolean save(ArticleDTO articleDTO);

    boolean delete(Integer articleId);

    int rebuild(List<ArticleDTO> articleDTOS);

    ArticleSearchHealthDTO health();
}
