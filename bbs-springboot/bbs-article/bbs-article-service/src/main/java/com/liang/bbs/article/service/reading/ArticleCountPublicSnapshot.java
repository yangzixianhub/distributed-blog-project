package com.liang.bbs.article.service.reading;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

//文章与当前浏览者无关的统计快照，可放入Redis供多用户共享
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ArticleCountPublicSnapshot implements Serializable {
    private Long likeCount;
    private Long commentCount;
    private String level;
}
