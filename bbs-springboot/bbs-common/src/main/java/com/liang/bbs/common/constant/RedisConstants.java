package com.liang.bbs.common.constant;

/**
 * @date 2021/4/21 18:55
 */
public interface RedisConstants {
    /**
     * 公共的
     */
    String BASE_KEY = "ns:";

    /**
     * 权限
     */
    String RIGHTS_KEY = "rights:";
    String API_KEY = BASE_KEY + RIGHTS_KEY + "api:";

    String LIKE_KEY = BASE_KEY + "like:";
    String ARTICLE_LIKE_KEY = LIKE_KEY + "article:";
    String COMMENT_LIKE_KEY = LIKE_KEY + "comment:";

}
