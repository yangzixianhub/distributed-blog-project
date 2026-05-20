package com.liang.bbs.article.facade.server;

import com.liang.bbs.article.facade.dto.SlideshowDTO;

import java.util.List;

public interface SlideshowService {

    /**
     * 获取轮播图信息
     *
     * @return
     */
    List<SlideshowDTO> getList();

}
