package com.liang.bbs.article.service.search;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * Search ids and pagination metadata returned by Elasticsearch.
 */
@Data
public class ArticleSearchPageResult {
    private long total;
    private int currentPage;
    private int pageSize;
    private List<Integer> articleIds = new ArrayList<>();
}
