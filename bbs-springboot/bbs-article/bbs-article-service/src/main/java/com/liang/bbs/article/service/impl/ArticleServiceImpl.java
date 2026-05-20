package com.liang.bbs.article.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.liang.bbs.article.facade.dto.*;
import com.liang.bbs.article.facade.server.ArticleLabelService;
import com.liang.bbs.article.facade.server.ArticleService;
import com.liang.bbs.article.facade.server.CommentService;
import com.liang.bbs.article.facade.server.LabelService;
import com.liang.bbs.article.persistence.entity.ArticlePo;
import com.liang.bbs.article.persistence.entity.ArticlePoExample;
import com.liang.bbs.article.persistence.mapper.ArticlePoExMapper;
import com.liang.bbs.article.persistence.mapper.ArticlePoMapper;
import com.liang.bbs.article.service.config.ArticleReadingProperties;
import com.liang.bbs.article.service.mapstruct.ArticleMS;
import com.liang.bbs.article.service.reading.ArticleCountPublicSnapshot;
import com.liang.bbs.article.service.reading.ArticleCountRedisCache;
import com.liang.bbs.article.service.reading.ArticleHtmlRedisCache;
import com.liang.bbs.article.service.reading.ArticleStaticHtmlPublisher;
import com.liang.bbs.article.service.search.ArticleSearchPageResult;
import com.liang.bbs.article.service.search.ArticleSearchService;
import com.liang.bbs.article.service.search.ArticleSearchSyncService;
import com.liang.bbs.common.enums.ArticleStateEnum;
import com.liang.bbs.user.facade.dto.FollowDTO;
import com.liang.bbs.user.facade.dto.LikeDTO;
import com.liang.bbs.user.facade.dto.LikeSearchDTO;
import com.liang.bbs.user.facade.dto.UserLevelDTO;
import com.liang.bbs.user.facade.server.FollowService;
import com.liang.bbs.user.facade.server.LikeService;
import com.liang.bbs.user.facade.server.UserLevelService;
import com.liang.manage.auth.facade.dto.user.UserDTO;
import com.liang.manage.auth.facade.server.FileService;
import com.liang.manage.auth.facade.server.UserService;
import com.liang.manage.auth.facade.server.VisitService;
import com.liang.nansheng.common.auth.UserSsoDTO;
import com.liang.nansheng.common.enums.ImageTypeEnum;
import com.liang.nansheng.common.enums.ResponseCode;
import com.liang.nansheng.common.utils.CommonUtils;
import com.liang.nansheng.common.web.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.dubbo.config.annotation.Reference;
import org.apache.dubbo.config.annotation.Service;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @date 2022/4/6 14:36
 */
@Slf4j
@Component
@Service
public class ArticleServiceImpl implements ArticleService {
    @Autowired
    private ArticlePoMapper articlePoMapper;

    @Autowired
    private ArticlePoExMapper articlePoExMapper;

    @Autowired
    private ArticleLabelService articleLabelService;

    @Autowired
    private LabelService labelService;

    @Autowired
    private CommentService commentService;

    @DubboReference
    private UserService userService;

    @DubboReference
    private LikeService likeService;

    @DubboReference
    private VisitService visitService;

    @DubboReference
    private FollowService followService;

    @DubboReference
    private UserLevelService userLevelService;

    @DubboReference
    private FileService fileService;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private ArticleReadingProperties articleReadingProperties;

    @Autowired
    private ArticleHtmlRedisCache articleHtmlRedisCache;

    @Autowired
    private ArticleStaticHtmlPublisher articleStaticHtmlPublisher;

    @Autowired
    private ArticleCountRedisCache articleCountRedisCache;

    @Autowired
    private ArticleSearchService articleSearchService;

    @Autowired
    private ArticleSearchSyncService articleSearchSyncService;

    private static final Integer contentMax = 200;

    /**
     * 获取所有审核通过的文章
     *
     * @param startTime
     * @param endTime
     * @return
     */
    @Override
    public List<ArticleDTO> getPassAll(LocalDateTime startTime, LocalDateTime endTime) {
        ArticlePoExample example = new ArticlePoExample();
        example.createCriteria().andIsDeletedEqualTo(false)
                .andCreateTimeBetween(startTime, endTime)
                .andStateEqualTo(ArticleStateEnum.enable.getCode());
        return ArticleMS.INSTANCE.toDTO(articlePoMapper.selectByExample(example));
    }

    /**
     * 获取文章
     *
     * @param articleSearchDTO
     * @param currentUser
     * @param articleStateEnum
     * @return
     */
    @Override
    public PageInfo<ArticleDTO> getList(ArticleSearchDTO articleSearchDTO, UserSsoDTO currentUser, ArticleStateEnum articleStateEnum) {
        if (articleSearchService.supports(articleSearchDTO)) {
            try {
                return searchByElasticsearch(articleSearchDTO, currentUser, articleStateEnum);
            } catch (Exception e) {
                log.warn("Elasticsearch search failed, fallback to mysql like query, title={}", articleSearchDTO.getTitle(), e);
            }
        }

        List<Integer> articleIds = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(articleSearchDTO.getLabelIds())) {
            // 根据标签id集合获取文章标签信息
            List<ArticleLabelDTO> articleLabelDTOS = articleLabelService.getByLabelIds(articleSearchDTO.getLabelIds());
            if (CollectionUtils.isNotEmpty(articleLabelDTOS)) {
                articleIds = articleLabelDTOS.stream().map(ArticleLabelDTO::getArticleId).collect(Collectors.toList());
            } else {
                // 该标签下没有文章
                return new PageInfo<>(new ArrayList<>());
            }
        }

        ArticlePoExample example = new ArticlePoExample();
        ArticlePoExample.Criteria criteria = example.createCriteria()
                .andIsDeletedEqualTo(false);
        if (articleStateEnum != null) {
            criteria.andStateEqualTo(articleStateEnum.getCode());
        }
        if (articleSearchDTO.getId() != null) {
            criteria.andIdEqualTo(articleSearchDTO.getId());
        }
        if (StringUtils.isNotBlank(articleSearchDTO.getTitle())) {
            criteria.andTitleLike("%" + articleSearchDTO.getTitle() + "%");
        }
        if (CollectionUtils.isNotEmpty(articleIds)) {
            criteria.andIdIn(articleIds);
        }
        if (articleSearchDTO.getCreateUser() != null) {
            criteria.andCreateUserEqualTo(articleSearchDTO.getCreateUser());
        }
        applyCreateTimeFilter(criteria, articleSearchDTO.getTimeRange());
        example.setOrderByClause("top desc, create_time desc, `id` desc");

        PageHelper.startPage(articleSearchDTO.getCurrentPage(), articleSearchDTO.getPageSize());
        List<ArticlePo> articlePos = articlePoMapper.selectByExample(example);
        PageInfo<ArticleDTO> pageInfo = ArticleMS.INSTANCE.toPage(new PageInfo<>(articlePos));
        if (CollectionUtils.isEmpty(pageInfo.getList())) {
            return pageInfo;
        }

        // 构建文章信息
        buildArticleInfo(pageInfo.getList(), currentUser);

        return pageInfo;
    }

    @Override
    public PageInfo<ArticleDTO> searchArticles(ArticleSearchDTO articleSearchDTO, UserSsoDTO currentUser, ArticleStateEnum articleStateEnum) {
        return getList(articleSearchDTO, currentUser, articleStateEnum);
    }

    private PageInfo<ArticleDTO> searchByElasticsearch(ArticleSearchDTO articleSearchDTO, UserSsoDTO currentUser, ArticleStateEnum articleStateEnum) {
        ArticleSearchPageResult searchPageResult = articleSearchService.search(articleSearchDTO, articleStateEnum);
        PageInfo<ArticleDTO> pageInfo = new PageInfo<>();
        pageInfo.setPageNum(searchPageResult.getCurrentPage());
        pageInfo.setPageSize(searchPageResult.getPageSize());
        pageInfo.setTotal(searchPageResult.getTotal());
        pageInfo.setPages(calculatePages(searchPageResult.getTotal(), searchPageResult.getPageSize()));

        if (CollectionUtils.isEmpty(searchPageResult.getArticleIds())) {
            pageInfo.setList(new ArrayList<>());
            pageInfo.setSize(0);
            return pageInfo;
        }

        List<ArticleDTO> articleDTOS = getBaseByIds(searchPageResult.getArticleIds(), articleStateEnum);
        if (CollectionUtils.isEmpty(articleDTOS)) {
            pageInfo.setList(new ArrayList<>());
            pageInfo.setSize(0);
            return pageInfo;
        }

        attachSearchHighlights(articleDTOS, searchPageResult);
        buildArticleInfo(articleDTOS, currentUser);
        pageInfo.setList(articleDTOS);
        pageInfo.setSize(articleDTOS.size());
        return pageInfo;
    }

    private int calculatePages(long total, int pageSize) {
        if (pageSize <= 0) {
            return 0;
        }
        return (int) ((total + pageSize - 1) / pageSize);
    }

    private void applyCreateTimeFilter(ArticlePoExample.Criteria criteria, String timeRange) {
        if (StringUtils.isBlank(timeRange)) {
            return;
        }
        LocalDateTime now = LocalDateTime.now();
        switch (timeRange) {
            case "day":
                criteria.andCreateTimeGreaterThanOrEqualTo(now.minusDays(1));
                break;
            case "week":
                criteria.andCreateTimeGreaterThanOrEqualTo(now.minusWeeks(1));
                break;
            case "month":
                criteria.andCreateTimeGreaterThanOrEqualTo(now.minusMonths(1));
                break;
            case "year":
                criteria.andCreateTimeGreaterThanOrEqualTo(now.minusYears(1));
                break;
            case "older":
                criteria.andCreateTimeLessThan(now.minusYears(1));
                break;
            default:
                break;
        }
    }

    @Override
    public Long getUserArticleCount(Long createUser, ArticleStateEnum articleStateEnum) {
        ArticlePoExample example = new ArticlePoExample();
        ArticlePoExample.Criteria criteria = example.createCriteria()
                .andIsDeletedEqualTo(false)
                .andCreateUserEqualTo(createUser);
        if (articleStateEnum != null) {
            criteria.andStateEqualTo(articleStateEnum.getCode());
        }

        return articlePoMapper.countByExample(example);
    }

    /**
     * 获取待审核的文章
     *
     * @param articleSearchDTO
     * @param currentUser
     * @return
     */
    @Override
    public PageInfo<ArticleDTO> getPendingReviewArticles(ArticleSearchDTO articleSearchDTO, UserSsoDTO currentUser) {
        return this.getList(articleSearchDTO, currentUser, ArticleStateEnum.pendingReview);
    }

    /**
     * 获取禁用的文章
     *
     * @param articleSearchDTO
     * @param currentUser
     * @return
     */
    @Override
    public PageInfo<ArticleDTO> getDisabledArticles(ArticleSearchDTO articleSearchDTO, UserSsoDTO currentUser) {
        return this.getList(articleSearchDTO, currentUser, ArticleStateEnum.disabled);
    }

    /**
     * 修改文章审批状态
     *
     * @param articleDTO
     * @param currentUser
     * @return
     */
    @Override
    public Boolean updateState(ArticleDTO articleDTO, UserSsoDTO currentUser) {
        ArticlePo articlePo = new ArticlePo();
        articlePo.setId(articleDTO.getId());
        articlePo.setUpdateTime(LocalDateTime.now());
        if (ArticleStateEnum.pendingReview.getCode().equals(articleDTO.getState())) {
            articlePo.setState(ArticleStateEnum.pendingReview.getCode());
        }
        if (ArticleStateEnum.disabled.getCode().equals(articleDTO.getState())) {
            articlePo.setState(ArticleStateEnum.disabled.getCode());
        }
        if (ArticleStateEnum.enable.getCode().equals(articleDTO.getState())) {
            articlePo.setState(ArticleStateEnum.enable.getCode());
        }

        if (articlePoMapper.updateByPrimaryKeySelective(articlePo) <= 0) {
            throw BusinessException.build(ResponseCode.OPERATE_FAIL, "修改文章审批状态失败");
        }

        syncStaticHtmlAndRedisCache(articleDTO.getId());
        articleSearchSyncService.syncArticle(articleDTO.getId());
        return true;
    }

    /**
     * 获取点赞过的文章
     *
     * @param likeSearchDTO
     * @param currentUser
     * @return
     */
    @Override
    public PageInfo<ArticleDTO> getLikesArticle(LikeSearchDTO likeSearchDTO, UserSsoDTO currentUser) {
        PageInfo<ArticleDTO> pageInfo = new PageInfo<>();
        PageInfo<LikeDTO> likeDTOPageInfo = likeService.getArticleByUserId(likeSearchDTO);
        BeanUtils.copyProperties(likeDTOPageInfo, pageInfo);
        if (CollectionUtils.isNotEmpty(likeDTOPageInfo.getList())) {
            List<Integer> articleIds = likeDTOPageInfo.getList().stream().distinct().map(LikeDTO::getArticleId).collect(Collectors.toList());
            List<ArticleDTO> articleDTOS = getBaseByIds(articleIds, ArticleStateEnum.enable);

            // 构建文章信息
            buildArticleInfo(articleDTOS, currentUser);
            pageInfo.setList(articleDTOS);
        }

        return pageInfo;
    }

    /**
     * 通过文章id集合获取文章信息
     *
     * @param ids
     * @param isPv        是否增加文章浏览数量
     * @param currentUser
     * @return
     */
    @Override
    public List<ArticleDTO> getByIds(List<Integer> ids, Boolean isPv, UserSsoDTO currentUser) {
        List<ArticleDTO> articleDTOS = getBaseByIds(ids, null);
        if (CollectionUtils.isEmpty(articleDTOS)) {
            return articleDTOS;
        }

        // 构建文章信息
        buildArticleInfo(articleDTOS, currentUser);

        Set<Integer> idsNeedMongo = articleDTOS.stream().map(ArticleDTO::getId).collect(Collectors.toCollection(HashSet::new));

        // 已发布文章：非作者读详情时可命中 Redis 中的正文 HTML，减轻数据库压力
        if (articleReadingProperties.getRedis().isHtmlCacheEnabled()) {
            Iterator<Integer> needIt = idsNeedMongo.iterator();
            while (needIt.hasNext()) {
                Integer aid = needIt.next();
                ArticleDTO dto = articleDTOS.stream().filter(a -> aid.equals(a.getId())).findFirst().orElse(null);
                if (dto == null) {
                    continue;
                }
                if (!ArticleStateEnum.enable.getCode().equals(dto.getState())) {
                    continue;
                }
                if (isArticleOwner(currentUser, dto)) {
                    continue;
                }
                String cachedHtml = articleHtmlRedisCache.get(aid);
                if (StringUtils.isNotBlank(cachedHtml)) {
                    dto.setHtml(cachedHtml);
                    needIt.remove();
                }
            }
        }

        if (!idsNeedMongo.isEmpty()) {
            List<ArticleMarkdownInfo> articleMarkdownInfos = getMarkdownByArticleIds(new ArrayList<>(idsNeedMongo));
            Map<Integer, List<ArticleMarkdownInfo>> articleId2List = articleMarkdownInfos.stream().collect(Collectors.groupingBy(ArticleMarkdownInfo::getArticleId));
            articleDTOS.forEach(articleDTO -> {
                if (!articleId2List.containsKey(articleDTO.getId())) {
                    return;
                }
                ArticleMarkdownInfo articleMarkdownInfo = articleId2List.get(articleDTO.getId()).get(0);
                articleDTO.setMarkdown(articleMarkdownInfo.getArticleMarkdown());
                articleDTO.setHtml(articleMarkdownInfo.getArticleHtml());
                if (articleReadingProperties.getRedis().isHtmlCacheEnabled()
                        && ArticleStateEnum.enable.getCode().equals(articleDTO.getState())
                        && StringUtils.isNotBlank(articleMarkdownInfo.getArticleHtml())
                        && !isArticleOwner(currentUser, articleDTO)) {
                    articleHtmlRedisCache.put(articleDTO.getId(), articleMarkdownInfo.getArticleHtml());
                }
            });
        }

        if (articleReadingProperties.getStaticHtml().isEnabled()) {
            articleDTOS.forEach(dto -> {
                if (ArticleStateEnum.enable.getCode().equals(dto.getState())) {
                    dto.setStaticHtmlUrl(articleStaticHtmlPublisher.buildPublicUrl(dto.getId()));
                }
            });
        }

        // 增加文章浏览数量（数据库原子自增，避免缓存命中时使用陈旧 pv）
        if (isPv != null && isPv) {
            this.updatePv(articleDTOS.get(0));
        }

        return articleDTOS;
    }

    /**
     * 通过文章id集合获取文章信息(最基础的信息)
     *
     * @param ids
     * @return
     */
    @Override
    public List<ArticleDTO> getBaseByIds(List<Integer> ids, ArticleStateEnum articleStateEnum) {
        ArticlePoExample example = new ArticlePoExample();
        ArticlePoExample.Criteria criteria = example.createCriteria();
        criteria.andIdIn(ids);
        if (articleStateEnum != null) {
            criteria.andStateEqualTo(articleStateEnum.getCode());
        }
        // 按in的参数顺序排序
        String stringIds = ids.stream().map(String::valueOf).collect(Collectors.joining(","));
        example.setOrderByClause("field(id, " + stringIds + ")");

        return ArticleMS.INSTANCE.toDTO(articlePoMapper.selectByExample(example));
    }

    /**
     * 撰写文章（无配图）
     *
     * @param articleDTO
     * @param currentUser
     * @return
     */
    @Override
    public Boolean create(ArticleDTO articleDTO, List<Integer> labelIds, UserSsoDTO currentUser) {
        if (StringUtils.isBlank(articleDTO.getTitle()) || StringUtils.isBlank(articleDTO.getHtml())) {
            throw BusinessException.build(ResponseCode.NOT_EXISTS, "参数不合规");
        }
        articleDTO.setIsDeleted(false);
        String content = CommonUtils.html2Text(articleDTO.getHtml());
        articleDTO.setContent(content.length() < contentMax ? content : content.substring(0, contentMax));
        articleDTO.setCreateUser(currentUser.getUserId());
        articleDTO.setUpdateUser(currentUser.getUserId());
        LocalDateTime now = LocalDateTime.now();
        articleDTO.setCreateTime(now);
        articleDTO.setUpdateTime(now);
        // 通过审核的文章才会启用（即：默认待审核）
        articleDTO.setState(ArticleStateEnum.pendingReview.getCode());
        ArticlePo articlePo = ArticleMS.INSTANCE.toPo(articleDTO);
        if (articlePoMapper.insertSelective(articlePo) <= 0) {
            throw BusinessException.build(ResponseCode.OPERATE_FAIL, "撰写文章失败");
        }
        // 新增文件标签关系信息
        articleLabelService.create(labelIds, articlePo.getId(), currentUser);

        // 插入文章内容（mongo）
        insertArticleContent(articlePo.getId(), articleDTO.getMarkdown(), articleDTO.getHtml(), currentUser.getUserId(), now);

        articleSearchSyncService.syncArticle(articlePo.getId());
        return true;
    }

    /**
     * 更新文章（无配图）
     *
     * @param articleDTO
     * @param currentUser
     * @return
     */
    @Override
    public Boolean update(ArticleDTO articleDTO, List<Integer> labelIds, UserSsoDTO currentUser) {
        if (StringUtils.isBlank(articleDTO.getTitle()) || StringUtils.isBlank(articleDTO.getHtml())) {
            throw BusinessException.build(ResponseCode.NOT_EXISTS, "参数不合规");
        }
        ArticlePo oldArticlePo = articlePoMapper.selectByPrimaryKey(articleDTO.getId());
        // 只能更新自己的文章
        if (!currentUser.getUserId().equals(oldArticlePo.getCreateUser())) {
            throw BusinessException.build(ResponseCode.OPERATE_FAIL, "无法更新，只能更新自己撰写的文章！");
        }

        String content = CommonUtils.html2Text(articleDTO.getHtml());
        articleDTO.setContent(content.length() < contentMax ? content : content.substring(0, contentMax));
        LocalDateTime now = LocalDateTime.now();
        articleDTO.setUpdateTime(now);
        articleDTO.setUpdateUser(currentUser.getUserId());
        articleDTO.setState(ArticleStateEnum.pendingReview.getCode());
        ArticlePo articlePo = ArticleMS.INSTANCE.toPo(articleDTO);
        if (articlePoMapper.updateByPrimaryKeySelective(articlePo) <= 0) {
            throw BusinessException.build(ResponseCode.OPERATE_FAIL, "更新文章失败");
        }
        // 更新文件标签关系信息
        articleLabelService.update(labelIds, articlePo.getId(), currentUser);

        // 更新文章内容（mongo）
        updateArticleContent(articlePo.getId(), articleDTO.getMarkdown(), articleDTO.getHtml(), currentUser.getUserId(), now);

        syncStaticHtmlAndRedisCache(articlePo.getId());
        articleSearchSyncService.syncArticle(articlePo.getId());
        return true;
    }

    /**
     * 撰写文章
     *
     * @param bytes
     * @param sourceFileName
     * @param articleDTO
     * @param currentUser
     * @return
     */
    @Override
    public Boolean create(byte[] bytes, String sourceFileName, ArticleDTO articleDTO, List<Integer> labelIds, UserSsoDTO currentUser) {
        try {
            // 文件上传（按比例压缩）
            String picture = fileService.fileScaleUpload(bytes, sourceFileName, ImageTypeEnum.articleTitleMap.name());
            articleDTO.setTitleMap(picture);
            create(articleDTO, labelIds, currentUser);
        } catch (Exception e) {
            log.error("撰写文章异常！", e);
            throw BusinessException.build(ResponseCode.OPERATE_FAIL, "撰写文章异常!");
        }

        return null;
    }

    /**
     * 更新文章
     *
     * @param bytes
     * @param sourceFileName
     * @param articleDTO
     * @param currentUser
     * @return
     */
    @Override
    public Boolean update(byte[] bytes, String sourceFileName, ArticleDTO articleDTO, List<Integer> labelIds, UserSsoDTO currentUser) {
        try {
            // 文件上传（按比例压缩）
            String picture = fileService.fileScaleUpload(bytes, sourceFileName, ImageTypeEnum.articleTitleMap.name());
            articleDTO.setTitleMap(picture);
            update(articleDTO, labelIds, currentUser);
        } catch (Exception e) {
            log.error("撰写文章异常！", e);
            throw BusinessException.build(ResponseCode.OPERATE_FAIL, "更新文章异常!");
        }

        return null;
    }

    /**
     * 上传图片（一张）- mavonEditor
     *
     * @param bytes
     * @param sourceFileName
     * @return
     */
    @Override
    public String uploadPicture(byte[] bytes, String sourceFileName) {
        // 文件上传（按比例压缩）
        return fileService.fileScaleUpload(bytes, sourceFileName, ImageTypeEnum.articlePicture.name());
    }

    /**
     * 获取文章评论访问总数
     *
     * @return
     */
    @Override
    public TotalDTO getArticleCommentVisitTotal() {
        TotalDTO totalDTO = new TotalDTO();
        totalDTO.setArticleCount(getTotal());
        totalDTO.setCommentCount(commentService.getTotal());
        totalDTO.setVisitCount(visitService.getTotal());
        return totalDTO;
    }

    /**
     * 获取文章数量
     *
     * @return
     */
    @Override
    public Long getTotal() {
        ArticlePoExample example = new ArticlePoExample();
        example.createCriteria().andIsDeletedEqualTo(false)
                .andStateEqualTo(ArticleStateEnum.enable.getCode());
        return articlePoMapper.countByExample(example);
    }

    /**
     * 获取用户阅读数量
     *
     * @param userIds
     * @return
     */
    @Override
    public List<ArticleReadDTO> getUserReadCount(List<Long> userIds) {
        return articlePoExMapper.selectUserReadCount(userIds);
    }

    /**
     * 获取文章一些统计数据
     *
     * @param id
     * @param currentUser
     * @return
     */
    @Override
    public ArticleCountDTO getCountById(Integer id, UserSsoDTO currentUser) {
        ArticlePo articlePo = articlePoMapper.selectByPrimaryKey(id);
        ArticleCountDTO articleCountDTO = new ArticleCountDTO();

        ArticleCountPublicSnapshot cached = articleCountRedisCache.get(id);
        if (cached != null) {
            articleCountDTO.setLikeCount(cached.getLikeCount());
            articleCountDTO.setCommentCount(cached.getCommentCount());
            articleCountDTO.setLevel(cached.getLevel());
        } else {
            articleCountDTO.setLikeCount(likeService.getLikeCountArticle(Collections.singletonList(id)));
            if (currentUser != null) {
                articleCountDTO.setIsLike(likeService.isLike(id, currentUser.getUserId()));
                FollowDTO followDTO = followService.getByFromToUser(currentUser.getUserId(), articlePo.getCreateUser(), false);
                if (followDTO != null) {
                    articleCountDTO.setIsFollow(true);
                }
            }
            articleCountDTO.setCommentCount(commentService.getCommentCountByArticle(id));
            List<UserLevelDTO> userLevelDTOS = userLevelService.getByUserId(articlePo.getCreateUser());
            if (CollectionUtils.isNotEmpty(userLevelDTOS)) {
                articleCountDTO.setLevel(userLevelDTOS.get(0).getLevel());
            }
            articleCountRedisCache.put(id, new ArticleCountPublicSnapshot(
                    articleCountDTO.getLikeCount(),
                    articleCountDTO.getCommentCount(),
                    articleCountDTO.getLevel()));
        }

        if (currentUser != null) {
            if (cached != null) {
                articleCountDTO.setIsLike(likeService.isLike(id, currentUser.getUserId()));
                FollowDTO followDTO = followService.getByFromToUser(currentUser.getUserId(), articlePo.getCreateUser(), false);
                if (followDTO != null) {
                    articleCountDTO.setIsFollow(true);
                }
            }
        }

        return articleCountDTO;
    }

    /**
     * 增加文章浏览数量
     *
     * @param articleDTO
     * @return
     */
    @Override
    public Boolean updatePv(ArticleDTO articleDTO) {
        int rows = articlePoExMapper.incrementPv(articleDTO.getId());
        if (rows <= 0) {
            throw BusinessException.build(ResponseCode.OPERATE_FAIL, "增加文章浏览量失败");
        }
        ArticlePo fresh = articlePoMapper.selectByPrimaryKey(articleDTO.getId());
        if (fresh != null) {
            articleDTO.setPv(fresh.getPv());
        }
        return true;
    }

    /**
     * 通过用户获取文章信息
     *
     * @param userId
     * @return
     */
    @Override
    public List<ArticleDTO> getByUserId(Long userId) {
        ArticlePoExample example = new ArticlePoExample();
        example.createCriteria().andIsDeletedEqualTo(false)
                .andStateEqualTo(ArticleStateEnum.enable.getCode())
                .andCreateUserEqualTo(userId);

        return ArticleMS.INSTANCE.toDTO(articlePoMapper.selectByExample(example));
    }

    @Override
    public Boolean articleTop(Integer id, Boolean top, UserSsoDTO currentUser) {
        ArticlePo articlePo = articlePoMapper.selectByPrimaryKey(id);
        articlePo.setUpdateTime(LocalDateTime.now());
        articlePo.setUpdateUser(currentUser.getUserId());
        // 置顶
        if (top) {
            int maxTop = Objects.isNull(this.getMaxTop()) ? 0 : this.getMaxTop();
            articlePo.setTop(maxTop + 1);
            if (articlePoMapper.updateByPrimaryKey(articlePo) <= 0) {
                throw BusinessException.build(ResponseCode.OPERATE_FAIL, "文章置顶失败");
            }
        } else {
            // 取消置顶
            articlePo.setTop(null);
            if (articlePoMapper.updateByPrimaryKey(articlePo) <= 0) {
                throw BusinessException.build(ResponseCode.OPERATE_FAIL, "文章取消置顶失败");
            }
        }

        return true;
    }

    @Override
    public Integer getMaxTop() {
        ArticlePoExample example = new ArticlePoExample();
        example.setOrderByClause("top desc limit 1");
        List<ArticlePo> articlePos = articlePoMapper.selectByExample(example);
        if (CollectionUtils.isNotEmpty(articlePos)) {
            return articlePos.get(0).getTop();
        }

        return null;
    }

    @Override
    public Boolean delete(Integer id, UserSsoDTO currentUser) {
        ArticlePo articlePo = articlePoMapper.selectByPrimaryKey(id);
        // 只能删除自己的文章
        if (!currentUser.getUserId().equals(articlePo.getCreateUser())) {
            throw BusinessException.build(ResponseCode.OPERATE_FAIL, "无法删除，只能删除自己撰写的文章！");
        }
        articlePo.setIsDeleted(true);
        articlePo.setUpdateTime(LocalDateTime.now());
        articlePo.setUpdateUser(currentUser.getUserId());
        if (articlePoMapper.updateByPrimaryKeySelective(articlePo) <= 0) {
            throw BusinessException.build(ResponseCode.OPERATE_FAIL, "文章删除失败");
        }

        articleHtmlRedisCache.evict(id);
        articleCountRedisCache.evict(id);
        articleStaticHtmlPublisher.unpublish(id);
        articleSearchSyncService.deleteArticle(id);
        return true;
    }

    @Override
    public ArticleCheckCountDTO getArticleCheckCount(String title) {
        ArticleCheckCountDTO articleCheckCountDTO = new ArticleCheckCountDTO();
        List<Map<String, Object>> list = articlePoExMapper.selectArticleCheckCount(title);
        if (CollectionUtils.isNotEmpty(list)) {
            list.forEach(stringLongMap -> {
                Integer state = Integer.valueOf(stringLongMap.get("state").toString());
                Long num = Long.valueOf(stringLongMap.get("num").toString());
                if (ArticleStateEnum.enable.getCode().equals(state)) {
                    articleCheckCountDTO.setEnableCount(num);
                }
                if (ArticleStateEnum.disabled.getCode().equals(state)) {
                    articleCheckCountDTO.setDisabledCount(num);
                }
                if (ArticleStateEnum.pendingReview.getCode().equals(state)) {
                    articleCheckCountDTO.setPendingReviewCount(num);
                }
            });
        }

        return articleCheckCountDTO;
    }

    @Override
    public ArticleSearchRebuildDTO rebuildSearchIndex() {
        long start = System.currentTimeMillis();
        int indexedCount = articleSearchService.rebuild(getSearchableArticles());
        ArticleSearchRebuildDTO result = new ArticleSearchRebuildDTO();
        result.setIndexedCount(indexedCount);
        result.setCostMillis(System.currentTimeMillis() - start);
        return result;
    }

    @Override
    public ArticleSearchHealthDTO getSearchHealth() {
        return articleSearchService.health();
    }

    /**
     * 获取文章id到标签信息的映射
     *
     * @param articleIds
     * @return
     */
    private Map<Integer, List<LabelDTO>> getArticleIdToLabels(List<Integer> articleIds) {
        // 文章id到标签信息的映射（key:文章id, value:标签信息）
        Map<Integer, List<LabelDTO>> articleToLabelMap = new HashMap<>();

        List<ArticleLabelDTO> articleLabelDTOS = articleLabelService.getByArticleIds(articleIds);
        if (CollectionUtils.isNotEmpty(articleLabelDTOS)) {
            // 获取所有的标签id集合
            List<Integer> labelIds = articleLabelDTOS.stream().distinct().map(ArticleLabelDTO::getLabelId).collect(Collectors.toList());
            Map<Integer, List<ArticleLabelDTO>> articleIdKeyMap = articleLabelDTOS.stream().collect(Collectors.groupingBy(ArticleLabelDTO::getArticleId));
            List<LabelDTO> labelDTOS = labelService.getByIds(labelIds);
            if (CollectionUtils.isNotEmpty(labelDTOS)) {
                Map<Integer, List<LabelDTO>> labelMap = labelDTOS.stream().collect(Collectors.groupingBy(LabelDTO::getId));
                articleIdKeyMap.forEach((k, v) -> {
                    List<LabelDTO> result = new ArrayList<>();
                    v.forEach(articleLabelDTO -> {
                        List<LabelDTO> labelDTOList = labelMap.get(articleLabelDTO.getLabelId());
                        result.addAll(CollectionUtils.isNotEmpty(labelDTOList) ? labelDTOList : new ArrayList<>());
                    });
                    articleToLabelMap.put(k, result);
                });
            }
        }

        return articleToLabelMap;
    }

    /**
     * 构建文章信息
     *
     * @param articleDTOS
     * @param currentUser
     */
    private void buildArticleInfo(List<ArticleDTO> articleDTOS, UserSsoDTO currentUser) {
        // 获取文章id到标签信息的映射
        List<Integer> articleIds = articleDTOS.stream().map(ArticleDTO::getId).collect(Collectors.toList());
        Map<Integer, List<LabelDTO>> articleToLabelMap = getArticleIdToLabels(articleIds);

        // 通过用户id集合获取用户信息
        List<Long> userIds = articleDTOS.stream().map(ArticleDTO::getCreateUser).collect(Collectors.toList());
        // 用户基础信息
        Map<Long, List<UserDTO>> idUsers = userService.getByIds(userIds).stream().collect(Collectors.groupingBy(UserDTO::getId));
        // 用户等级信息
        Map<Long, List<UserLevelDTO>> userIdToUserLevel = userLevelService.getByUserIds(userIds).stream().collect(Collectors.groupingBy(UserLevelDTO::getUserId));
        // 通过id获取name
        articleDTOS.forEach(articleDTO -> {
            if (idUsers.containsKey(articleDTO.getCreateUser())) {
                articleDTO.setCreateUserName(idUsers.get(articleDTO.getCreateUser()).get(0).getName());
                articleDTO.setPicture(idUsers.get(articleDTO.getCreateUser()).get(0).getPicture());
            }
            if (userIdToUserLevel.containsKey(articleDTO.getCreateUser())) {
                articleDTO.setLevel(userIdToUserLevel.get(articleDTO.getCreateUser()).get(0).getLevel());
            }
            // 数据库里面去拿
            if (!articleToLabelMap.isEmpty()) {
                articleDTO.setLabelDTOS(articleToLabelMap.get(articleDTO.getId()));
            }
            articleDTO.setArticleCountDTO(this.getCountById(articleDTO.getId(), currentUser));
        });
    }

    private boolean isArticleOwner(UserSsoDTO currentUser, ArticleDTO dto) {
        return currentUser != null && dto.getCreateUser() != null
                && currentUser.getUserId().equals(dto.getCreateUser());
    }

    /**
     * 正文或审核状态变更后：刷新 Redis 正文缓存与静态 HTML 文件。
     */
    private void syncStaticHtmlAndRedisCache(Integer articleId) {
        if (articleId == null) {
            return;
        }
        articleHtmlRedisCache.evict(articleId);
        articleCountRedisCache.evict(articleId);
        ArticlePo po = articlePoMapper.selectByPrimaryKey(articleId);
        if (po == null || Boolean.TRUE.equals(po.getIsDeleted())) {
            articleStaticHtmlPublisher.unpublish(articleId);
            return;
        }
        if (ArticleStateEnum.enable.getCode().equals(po.getState())) {
            articleStaticHtmlPublisher.publish(articleId);
        } else {
            articleStaticHtmlPublisher.unpublish(articleId);
        }
    }

    /**
     * 插入文章内容（mongo）
     *
     * @param articleId
     * @param markdown
     * @param html
     * @param userId
     * @param now
     */
    private void insertArticleContent(Integer articleId, String markdown, String html, Long userId, LocalDateTime now) {
        ArticleMarkdownInfo articleMarkdownInfo = new ArticleMarkdownInfo();
        articleMarkdownInfo.setArticleId(articleId);
        articleMarkdownInfo.setArticleMarkdown(markdown);
        articleMarkdownInfo.setArticleHtml(html);
        articleMarkdownInfo.setUserId(userId);
        articleMarkdownInfo.setTime(now);
        mongoTemplate.insert(Collections.singletonList(articleMarkdownInfo), ArticleMarkdownInfo.class);
    }

    /**
     * 更新文章内容（mongo）
     *
     * @param articleId
     * @param markdown
     * @param html
     * @param userId
     * @param now
     */
    private void updateArticleContent(Integer articleId, String markdown, String html, Long userId, LocalDateTime now) {
        Query query = new Query(Criteria.where("articleId").is(articleId));
        Update update = new Update();
        update.set("articleMarkdown", markdown);
        update.set("articleHtml", html);
        update.set("userId", userId);
        update.set("time", now);
        mongoTemplate.updateMulti(query, update, ArticleMarkdownInfo.class);
    }

    /**
     * 通过文章id集合获取文章内容
     *
     * @param articleIds
     * @return
     */
    private List<ArticleMarkdownInfo> getMarkdownByArticleIds(List<Integer> articleIds) {
        Query query = new Query(Criteria.where("articleId").in(articleIds));
        return mongoTemplate.find(query, ArticleMarkdownInfo.class);
    }

    private void syncArticleSearchIndex(Integer articleId) {
        if (articleId == null || !articleSearchService.isEnabled()) {
            return;
        }

        ArticlePo articlePo = articlePoMapper.selectByPrimaryKey(articleId);
        if (!isSearchable(articlePo)) {
            articleSearchService.delete(articleId);
            return;
        }

        ArticleDTO articleDTO = ArticleMS.INSTANCE.toDTO(articlePo);
        attachSearchContent(articleDTO);
        articleSearchService.save(articleDTO);
    }

    private boolean isSearchable(ArticlePo articlePo) {
        return articlePo != null
                && !Boolean.TRUE.equals(articlePo.getIsDeleted())
                && ArticleStateEnum.enable.getCode().equals(articlePo.getState());
    }

    private List<ArticleDTO> getSearchableArticles() {
        ArticlePoExample example = new ArticlePoExample();
        example.createCriteria()
                .andIsDeletedEqualTo(false)
                .andStateEqualTo(ArticleStateEnum.enable.getCode());
        example.setOrderByClause("id asc");
        List<ArticleDTO> articleDTOS = ArticleMS.INSTANCE.toDTO(articlePoMapper.selectByExample(example));
        attachSearchContent(articleDTOS);
        attachSearchMetadata(articleDTOS);
        return articleDTOS;
    }

    private void attachSearchMetadata(List<ArticleDTO> articleDTOS) {
        if (CollectionUtils.isEmpty(articleDTOS)) {
            return;
        }

        List<Integer> articleIds = articleDTOS.stream()
                .map(ArticleDTO::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        List<Long> userIds = articleDTOS.stream()
                .map(ArticleDTO::getCreateUser)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        Map<Integer, List<LabelDTO>> articleToLabelMap = getArticleIdToLabels(articleIds);
        Map<Long, List<UserDTO>> idUsers = userIds.isEmpty()
                ? Collections.emptyMap()
                : userService.getByIds(userIds).stream().collect(Collectors.groupingBy(UserDTO::getId));

        articleDTOS.forEach(articleDTO -> {
            if (articleDTO == null) {
                return;
            }
            articleDTO.setLabelDTOS(articleToLabelMap.getOrDefault(articleDTO.getId(), Collections.emptyList()));
            List<UserDTO> userDTOS = idUsers.get(articleDTO.getCreateUser());
            if (CollectionUtils.isNotEmpty(userDTOS)) {
                articleDTO.setCreateUserName(userDTOS.get(0).getName());
            }
        });
    }

    private void attachSearchContent(ArticleDTO articleDTO) {
        if (articleDTO == null || articleDTO.getId() == null) {
            return;
        }

        List<ArticleMarkdownInfo> articleMarkdownInfos = getMarkdownByArticleIds(Collections.singletonList(articleDTO.getId()));
        if (CollectionUtils.isEmpty(articleMarkdownInfos)) {
            return;
        }
        articleDTO.setContent(extractSearchContent(articleMarkdownInfos.get(0), articleDTO.getContent()));
    }

    private void attachSearchContent(List<ArticleDTO> articleDTOS) {
        if (CollectionUtils.isEmpty(articleDTOS)) {
            return;
        }

        List<Integer> articleIds = articleDTOS.stream()
                .map(ArticleDTO::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(articleIds)) {
            return;
        }

        Map<Integer, List<ArticleMarkdownInfo>> contentMap = getMarkdownByArticleIds(articleIds).stream()
                .collect(Collectors.groupingBy(ArticleMarkdownInfo::getArticleId));
        articleDTOS.forEach(articleDTO -> {
            List<ArticleMarkdownInfo> articleMarkdownInfos = contentMap.get(articleDTO.getId());
            if (CollectionUtils.isNotEmpty(articleMarkdownInfos)) {
                articleDTO.setContent(extractSearchContent(articleMarkdownInfos.get(0), articleDTO.getContent()));
            }
        });
    }

    private String extractSearchContent(ArticleMarkdownInfo articleMarkdownInfo, String fallbackContent) {
        if (articleMarkdownInfo == null) {
            return fallbackContent;
        }
        if (StringUtils.isNotBlank(articleMarkdownInfo.getArticleHtml())) {
            String content = CommonUtils.html2Text(articleMarkdownInfo.getArticleHtml());
            if (StringUtils.isNotBlank(content)) {
                return content;
            }
        }
        if (StringUtils.isNotBlank(articleMarkdownInfo.getArticleMarkdown())) {
            return articleMarkdownInfo.getArticleMarkdown();
        }
        return fallbackContent;
    }

    private void attachSearchHighlights(List<ArticleDTO> articleDTOS, ArticleSearchPageResult searchPageResult) {
        if (CollectionUtils.isEmpty(articleDTOS) || searchPageResult == null) {
            return;
        }

        Map<Integer, String> highlightTitleMap = searchPageResult.getHighlightTitleMap();
        Map<Integer, String> highlightContentMap = searchPageResult.getHighlightContentMap();
        articleDTOS.forEach(articleDTO -> {
            if (articleDTO == null || articleDTO.getId() == null) {
                return;
            }
            articleDTO.setHighlightTitle(highlightTitleMap.get(articleDTO.getId()));
            articleDTO.setHighlightContent(highlightContentMap.get(articleDTO.getId()));
        });
    }
}
