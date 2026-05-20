package com.liang.bbs.user.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.liang.bbs.article.facade.dto.ArticleDTO;
import com.liang.bbs.article.facade.server.ArticleService;
import com.liang.bbs.common.enums.ArticleStateEnum;
import com.liang.bbs.user.facade.dto.LikeDTO;
import com.liang.bbs.user.facade.dto.LikeSearchDTO;
import com.liang.bbs.user.facade.server.LikeService;
import com.liang.bbs.user.persistence.entity.LikePo;
import com.liang.bbs.user.persistence.entity.LikePoExample;
import com.liang.bbs.user.persistence.mapper.LikePoExMapper;
import com.liang.bbs.user.persistence.mapper.LikePoMapper;
import com.liang.bbs.user.service.like.ArticleLikeStateRepository;
import com.liang.bbs.user.service.like.LikeCacheCoordinator;
import com.liang.bbs.user.service.like.LikeTargetType;
import com.liang.bbs.user.service.mapstruct.LikeMS;
import com.liang.nansheng.common.auth.UserSsoDTO;
import com.liang.nansheng.common.enums.ResponseCode;
import com.liang.nansheng.common.web.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@Service
public class LikeServiceImpl implements LikeService {
    @Autowired
    private LikePoMapper likePoMapper;

    @Autowired
    private LikePoExMapper likePoExMapper;

    @Autowired
    private LikeCacheCoordinator likeCacheCoordinator;

    @Autowired
    private ArticleLikeStateRepository articleLikeStateRepository;

    @DubboReference
    private ArticleService articleService;

    @Override
    public List<LikeDTO> getPaasAll(LocalDateTime startTime, LocalDateTime endTime) {
        return LikeMS.INSTANCE.toDTO(likePoExMapper.selectAllArticle(startTime, endTime));
    }

    @Override
    public PageInfo<LikeDTO> getArticleByUserId(LikeSearchDTO likeSearchDTO) {
        if (likeSearchDTO.getLikeUser() == null) {
            throw BusinessException.build(ResponseCode.NOT_EXISTS, "parameter invalid");
        }
        PageHelper.startPage(likeSearchDTO.getCurrentPage(), likeSearchDTO.getPageSize());
        List<LikePo> likePos = likePoExMapper.selectArticleByUserId(likeSearchDTO.getLikeUser());
        return LikeMS.INSTANCE.toPage(new PageInfo<>(likePos));
    }

    @Override
    public PageInfo<LikeDTO> getUserByArticleId(LikeSearchDTO likeSearchDTO) {
        if (likeSearchDTO.getArticleId() == null) {
            throw BusinessException.build(ResponseCode.NOT_EXISTS, "parameter invalid");
        }
        LikePoExample example = new LikePoExample();
        LikePoExample.Criteria criteria = example.createCriteria().andStateEqualTo(true);
        criteria.andArticleIdEqualTo(likeSearchDTO.getArticleId());
        example.setOrderByClause("`id` desc");
        PageHelper.startPage(likeSearchDTO.getCurrentPage(), likeSearchDTO.getPageSize());
        List<LikePo> likePos = likePoMapper.selectByExample(example);
        return LikeMS.INSTANCE.toPage(new PageInfo<>(likePos));
    }

    @Override
    public LikeDTO getById(Long id) {
        return LikeMS.INSTANCE.toDTO(likePoMapper.selectByPrimaryKey(id));
    }

    @Override
    public LikeDTO getByArticleIdUserId(Integer articleId, Long userId) {
        LikePoExample example = new LikePoExample();
        example.createCriteria().andArticleIdEqualTo(articleId).andLikeUserEqualTo(userId);
        List<LikeDTO> likeDTOS = LikeMS.INSTANCE.toDTO(likePoMapper.selectByExample(example));
        return CollectionUtils.isEmpty(likeDTOS) ? null : likeDTOS.get(0);
    }

    @Override
    public Long getLikeCountArticle(List<Integer> articleIds) {
        long count = 0L;
        for (Integer articleId : articleIds) {
            count += likeCacheCoordinator.getCount(LikeTargetType.ARTICLE, articleId.longValue(), articleLikeStateRepository);
        }
        return count;
    }

    @Override
    public Boolean isLike(Integer articleId, Long userId) {
        return likeCacheCoordinator.isLiked(LikeTargetType.ARTICLE, articleId.longValue(), userId, articleLikeStateRepository);
    }

    @Override
    public Boolean updateLikeState(Integer articleId, UserSsoDTO currentUser) {
        return likeCacheCoordinator.toggle(LikeTargetType.ARTICLE, articleId.longValue(), currentUser.getUserId(), articleLikeStateRepository);
    }

    @Override
    public Long getUserLikeCount(Long userId) {
        List<ArticleDTO> articleDTOS = articleService.getByUserId(userId);
        if (CollectionUtils.isNotEmpty(articleDTOS)) {
            List<Integer> articleIds = articleDTOS.stream().map(ArticleDTO::getId).collect(Collectors.toList());
            return this.getLikeCountArticle(articleIds);
        }
        return 0L;
    }

    @Override
    public Long getUserTheLikeCount(Long userId) {
        LikePoExample example = new LikePoExample();
        example.createCriteria().andStateEqualTo(true).andLikeUserEqualTo(userId);
        List<LikePo> likePos = likePoMapper.selectByExample(example);
        if (CollectionUtils.isNotEmpty(likePos)) {
            List<Integer> articleIds = likePos.stream().map(LikePo::getArticleId).collect(Collectors.toList());
            List<ArticleDTO> articleDTOS = articleService.getBaseByIds(articleIds, ArticleStateEnum.enable);
            return CollectionUtils.isNotEmpty(articleDTOS) ? articleDTOS.size() : 0L;
        }
        return 0L;
    }
}
