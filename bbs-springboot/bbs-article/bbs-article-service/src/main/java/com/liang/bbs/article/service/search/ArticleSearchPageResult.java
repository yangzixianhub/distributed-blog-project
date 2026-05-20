package com.liang.bbs.article.service.search;

import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Search ids, pagination metadata, and highlight snippets returned by Elasticsearch.
 */
@Data
public class ArticleSearchPageResult {
    private long total;
    private int currentPage;
    private int pageSize;
    private List<Integer> articleIds = new ArrayList<>();
    private Map<Integer, String> highlightTitleMap = new HashMap<>();
    private Map<Integer, String> highlightContentMap = new HashMap<>();
}
