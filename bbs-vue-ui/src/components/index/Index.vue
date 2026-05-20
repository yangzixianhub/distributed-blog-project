<template>
  <a-layout class="home-layout">
    <a-layout id="components-layout-basic">
      <IndexHeader class="header"
                   @refresh="refresh"
                   :searchContent="searchContent"
                   :timeRange="timeRange"/>
      <a-layout-content>
        <main class="content">
          <section v-if="!isSearchMode()" class="hero-panel glass-card">
            <div class="hero-copy">
              <span class="hero-kicker">{{ heroKicker }}</span>
              <h1 class="hero-title">{{ heroTitle }}</h1>
              <p class="hero-subtitle">{{ heroSubtitle }}</p>
            </div>
            <div v-if="!$store.state.collapsed" class="hero-art">
              <div class="hero-art-core">
                <div class="hero-orbit hero-orbit-one"></div>
                <div class="hero-orbit hero-orbit-two"></div>
                <div class="hero-orbit hero-orbit-three"></div>
                <div class="hero-node hero-node-one"></div>
                <div class="hero-node hero-node-two"></div>
                <div class="hero-node hero-node-three"></div>
                <div class="hero-node hero-node-four"></div>
                <div class="hero-center-card">
                  <span class="hero-center-kicker">DS BLOG</span>
                  <strong>Distributed</strong>
                  <span>Blog System</span>
                </div>
              </div>
              <div class="hero-art-panel hero-art-panel-top">
                <span>Cluster</span>
                <strong>Spring Boot</strong>
              </div>
              <div class="hero-art-panel hero-art-panel-bottom">
                <span>Search</span>
                <strong>Elastic + Redis</strong>
              </div>
            </div>
            <div class="hero-stats">
              <div class="hero-stat">
                <span class="hero-stat-value">{{ listData.length }}</span>
                <span class="hero-stat-label">{{ statPrimaryLabel }}</span>
              </div>
              <div class="hero-stat">
                <span class="hero-stat-value">{{ isSearchMode() ? timeRangeLabel : "24/7" }}</span>
                <span class="hero-stat-label">{{ statSecondaryLabel }}</span>
              </div>
            </div>
          </section>

          <section class="main-grid">
            <a-col :span="$store.state.collapsed ? 24 : 18" class="main-column">
              <CustomEmpty v-if="spinning"/>
              <div v-else>
                <div v-if="!isSearchMode() && !$store.state.collapsed && $store.state.isCarousel" class="hero-carousel-wrap">
                  <SlideShow class="hero-carousel"/>
                </div>
                <div class="article-check-left-buttons"
                     :style="$store.state.collapsedMax ? '' : 'right:70px'"
                     v-if="$store.state.isManage">
                  <ArticleCheck
                      ref="child"
                      @initArticles="initArticles"/>
                </div>
                <section class="feed-shell section-card">
                  <div class="feed-heading">
                    <div>
                      <span class="feed-kicker">{{ feedKicker }}</span>
                      <h2 class="feed-title">{{ feedTitle }}</h2>
                    </div>
                    <span class="feed-tip">{{ feedTip }}</span>
                  </div>
                  <FrontPageArticle v-if="$store.state.isManage && !spinning"
                                    :finish="finish"
                                    :hasNext="hasNext"
                                    :data="listData"
                                    :isAdminAudit="true"
                                    @updateData="updateData"
                                    @articleTopCallBack="articleTopCallBack"
                                    @refresh="refresh"/>
                  <FrontPageArticle v-if="!$store.state.isManage && !spinning"
                                    :finish="finish"
                                    :hasNext="hasNext"
                                    :data="listData"
                                    @refresh="refresh"/>
                </section>
              </div>
            </a-col>

            <a-col v-if="!$store.state.collapsed" :span="6" class="sidebar-column">
              <ProjectIntro class="sidebar-card section-card"/>
              <AuthorsList class="sidebar-card section-card"/>
              <LatestComment class="sidebar-card section-card"/>
              <FriendDonate class="sidebar-card section-card"/>
              <FilingInfo class="sidebar-footer"/>
            </a-col>
          </section>
        </main>
      </a-layout-content>
      <FooterButtons v-if="!$store.state.collapsed"/>
    </a-layout>
  </a-layout>
</template>

<script>
import IndexHeader from "@/components/index/head/IndexHeader";
import SlideShow from "@/components/concern/SlideShow";
import FrontPageArticle from "@/components/article/FrontPageArticle";
import ProjectIntro from "@/components/right/ProjectIntro";
import articleService from "@/service/articleService";
import AuthorsList from "@/components/right/AuthorsList";
import FilingInfo from "@/components/right/FilingInfo";
import FooterButtons from "@/components/utils/FooterButtons";
import CustomEmpty from "@/components/utils/CustomEmpty";
import LatestComment from "@/components/right/LatestComment";
import FriendDonate from "@/components/right/FriendDonate";
import ArticleCheck from "@/components/article/ArticleCheck";

export default {
  components: {
    IndexHeader,
    FooterButtons,
    SlideShow,
    FrontPageArticle,
    ProjectIntro,
    AuthorsList,
    FilingInfo,
    CustomEmpty,
    LatestComment,
    FriendDonate,
    ArticleCheck
  },
  data() {
    return {
      spinning: true,
      listData: [],
      hasNext: true,
      finish: false,
      params: {currentPage: 1, pageSize: 12},
      searchContent: "",
      timeRange: ""
    };
  },
  computed: {
    heroKicker() {
      return this.isSearchMode() ? "SEARCH MODE" : "Distributed Software";
    },
    heroTitle() {
      if (this.isSearchMode()) {
        return this.searchContent ? `围绕 “${this.searchContent}” 的内容结果` : "按时间筛选的精选内容";
      }
      return "社区首页";
    },
    heroSubtitle() {
      if (this.isSearchMode()) {
        return "搜索结果会优先展示更相关、更活跃的讨论内容，方便快速定位文章与评论。";
      }
      return "文章、作者和评论流组织版面，阅读起来更轻盈。";
    },
    statPrimaryLabel() {
      return this.isSearchMode() ? "当前结果数" : "当前已加载文章";
    },
    statSecondaryLabel() {
      return this.isSearchMode() ? "筛选范围" : "社区在线状态";
    },
    timeRangeLabel() {
      const map = {
        day: "1D",
        week: "7D",
        month: "30D",
        year: "1Y",
        older: "1Y+"
      };
      return map[this.timeRange] || "ALL";
    },
    feedKicker() {
      return this.isSearchMode() ? "精准检索" : "内容广场";
    },
    feedTitle() {
      return this.isSearchMode() ? "搜索结果" : "最新发布";
    },
    feedTip() {
      if (this.$store.state.isManage) {
        return "支持管理审核与置顶操作";
      }
      return this.isSearchMode() ? "按相关性与时间综合排序" : "发现最新文章与热门讨论";
    }
  },
  methods: {
    loadMore() {
      this.params.currentPage++;
      if (this.isSearchMode()) {
        this.getSearchArticleList(this.params, true);
        return;
      }
      if (this.$store.state.articleCheck === "enable") {
        this.getArticleList(this.params, true);
      }
      if (this.$store.state.articleCheck === "pendingReview") {
        this.getPendingReviewArticles(this.params, true);
      }
      if (this.$store.state.articleCheck === "disabled") {
        this.getDisabledArticles(this.params, true);
      }
    },
    initArticles() {
      this.$nextTick(() => {
        let dom = document.querySelector("#app");
        if (dom !== null) {
          dom.scrollTop = 0;
        }
      });

      this.hasNext = true;
      if (this.isSearchMode()) {
        this.getSearchArticleList(this.params);
        return;
      }
      if (this.$store.state.articleCheck === "enable") {
        this.getArticleList(this.params);
      }
      if (this.$store.state.articleCheck === "pendingReview") {
        this.getPendingReviewArticles(this.params);
      }
      if (this.$store.state.articleCheck === "disabled") {
        this.getDisabledArticles(this.params);
      }
    },
    getArticleList(params, isLoadMore) {
      if (!isLoadMore) {
        this.params.currentPage = 1;
      }
      this.finish = false;
      articleService.getArticleList(params)
          .then(res => {
            if (isLoadMore) {
              this.listData = this.listData.concat(res.data.list);
              this.hasNext = res.data.list.length !== 0;
            } else {
              this.listData = res.data.list;
            }
            this.spinning = false;
            this.finish = true;
          })
          .catch(err => {
            this.finish = true;
            this.$message.error(err.desc);
          });
    },
    getSearchArticleList(params, isLoadMore) {
      if (!isLoadMore) {
        this.params.currentPage = 1;
      }
      this.finish = false;
      articleService.searchArticles(params)
          .then(res => {
            if (isLoadMore) {
              this.listData = this.listData.concat(res.data.list);
              this.hasNext = res.data.list.length !== 0;
            } else {
              this.listData = res.data.list;
            }
            this.spinning = false;
            this.finish = true;
          })
          .catch(err => {
            this.finish = true;
            this.$message.error(err.desc);
          });
    },
    getPendingReviewArticles(params, isLoadMore) {
      if (!isLoadMore) {
        this.params.currentPage = 1;
      }
      this.finish = false;
      articleService.getPendingReviewArticles(params)
          .then(res => {
            if (isLoadMore) {
              this.listData = this.listData.concat(res.data.list);
              this.hasNext = res.data.list.length !== 0;
            } else {
              this.listData = res.data.list;
            }
            this.spinning = false;
            this.finish = true;
          })
          .catch(err => {
            this.finish = true;
            this.$message.error(err.desc);
          });
    },
    getDisabledArticles(params, isLoadMore) {
      if (!isLoadMore) {
        this.params.currentPage = 1;
      }
      this.finish = false;
      articleService.getDisabledArticles(params)
          .then(res => {
            if (isLoadMore) {
              this.listData = this.listData.concat(res.data.list);
              this.hasNext = res.data.list.length !== 0;
            } else {
              this.listData = res.data.list;
            }
            this.spinning = false;
            this.finish = true;
          })
          .catch(err => {
            this.finish = true;
            this.$message.error(err.desc);
          });
    },
    refresh() {
      this.params = {currentPage: 1, pageSize: 10};
      if (this.isSearchMode()) {
        this.params.title = this.searchContent;
        this.params.timeRange = this.timeRange;
        this.getSearchArticleList(this.params);
        return;
      }
      this.getArticleList(this.params);
    },
    isSearchMode() {
      return !!((this.$route.query.query && this.$route.query.query.trim()) || this.$route.query.timeRange);
    },
    updateData(tempData) {
      this.listData = tempData;
      if (this.$store.state.isManage) {
        this.$refs.child.getArticleCheckCount();
      }
    },
    articleTopCallBack() {
      this.$nextTick(() => {
        let dom = document.querySelector("#app");
        if (dom !== null) {
          dom.scrollTop = 0;
        }
      });

      if (this.$store.state.articleCheck === "enable") {
        this.getArticleList(this.params);
      }
      if (this.$store.state.articleCheck === "pendingReview") {
        this.getPendingReviewArticles(this.params);
      }
      if (this.$store.state.articleCheck === "disabled") {
        this.getDisabledArticles(this.params);
      }
    }
  },
  mounted() {
    this.$nextTick(() => {
      let dom = document.querySelector("#app");
      if (dom !== null) {
        dom.scrollTop = 0;
      }
    });

    let query = this.$route.query.query;
    let timeRange = this.$route.query.timeRange || "";
    this.searchContent = query;
    this.timeRange = timeRange;
    this.params.title = query;
    this.params.timeRange = timeRange;
    if (this.isSearchMode()) {
      this.getSearchArticleList(this.params);
    } else {
      this.getArticleList(this.params);
    }
    this.$utils.scroll.call(this, document.querySelector("#app"));
  },
  watch: {
    $route() {
      let query = this.$route.query.query;
      let timeRange = this.$route.query.timeRange || "";
      this.searchContent = query;
      this.timeRange = timeRange;
      this.params.title = query;
      this.params.timeRange = timeRange;
      if (this.isSearchMode()) {
        this.getSearchArticleList(this.params);
        return;
      }
      if (this.$store.state.isManage) {
        if (this.$store.state.articleCheck === "enable") {
          this.getArticleList(this.params);
        }
        if (this.$store.state.articleCheck === "pendingReview") {
          this.getPendingReviewArticles(this.params);
        }
        if (this.$store.state.articleCheck === "disabled") {
          this.getDisabledArticles(this.params);
        }
        this.$refs.child.getArticleCheckCount();
      } else {
        this.getArticleList(this.params);
      }
    }
  }
};
</script>

<style>
#components-layout-basic .header {
  position: fixed;
  z-index: 999;
  width: 100%;
  background: rgba(255, 255, 255, 0.72);
  border-bottom: 1px solid rgba(117, 136, 167, 0.14);
  box-shadow: 0 10px 40px rgba(18, 33, 62, 0.06);
  backdrop-filter: blur(18px);
}

#components-layout-basic .content {
  margin-top: 88px;
  width: 100%;
  max-width: 1220px;
  padding: 0 16px 32px;
}

#components-layout-basic .ant-layout-header,
#components-layout-basic .ant-layout-content {
  display: flex;
  align-items: center;
  justify-content: center;
}

#components-layout-basic .ant-layout-header {
  background: transparent;
  height: auto;
  line-height: 2.3;
}

.home-layout {
  background: transparent;
}

.hero-panel {
  position: relative;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 18px;
  margin-bottom: 24px;
  min-height: 220px;
  padding: 28px 34px;
  border-radius: 32px;
  background:
      linear-gradient(135deg, rgba(255, 255, 255, 0.96) 0%, rgba(247, 250, 255, 0.96) 52%, rgba(235, 243, 255, 0.98) 100%);
  box-shadow: 0 24px 64px rgba(20, 45, 86, 0.1);
}

.hero-panel::before {
  content: "";
  position: absolute;
  inset: auto -40px -80px auto;
  width: 240px;
  height: 240px;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(24, 105, 255, 0.24), transparent 68%);
}

.hero-panel::after {
  content: "";
  position: absolute;
  inset: -90px auto auto -50px;
  width: 200px;
  height: 200px;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(19, 194, 194, 0.16), transparent 70%);
}

.hero-copy,
.hero-art,
.hero-stats {
  position: relative;
  z-index: 1;
}

.hero-copy {
  max-width: 760px;
}

.hero-kicker,
.feed-kicker {
  display: inline-flex;
  align-items: center;
  padding: 7px 14px;
  border-radius: 999px;
  background: linear-gradient(135deg, rgba(24, 105, 255, 0.12), rgba(19, 194, 194, 0.08));
  color: #245edb;
  font-size: 13px;
  font-weight: 700;
  letter-spacing: 0.1em;
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.6);
}

.hero-title {
  margin: 14px 0 10px;
  font-family: "HamburgSerial-Xbold", "PingFang SC", "Segoe UI", sans-serif;
  font-size: 54px;
  font-weight: 700;
  letter-spacing: 0.01em;
  line-height: 1.02;
  color: #16233d;
  text-shadow: 0 1px 0 rgba(255, 255, 255, 0.55);
}

.hero-subtitle {
  margin: 0;
  max-width: 700px;
  color: #5f708f;
  line-height: 1.7;
  font-size: 17px;
  font-weight: 500;
}

.hero-stats {
  align-self: stretch;
  display: flex;
  gap: 14px;
  min-width: 320px;
}

.hero-art {
  position: relative;
  width: 300px;
  min-width: 300px;
  height: 180px;
  margin-left: auto;
}

.hero-art-core {
  position: absolute;
  top: 14px;
  left: 52px;
  width: 176px;
  height: 176px;
  border-radius: 50%;
  background: radial-gradient(circle at 50% 50%, rgba(255, 255, 255, 0.96), rgba(233, 241, 255, 0.86) 58%, rgba(210, 225, 249, 0.18) 100%);
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.84), 0 18px 48px rgba(24, 62, 122, 0.16);
}

.hero-orbit {
  position: absolute;
  border: 1px solid rgba(67, 121, 210, 0.18);
  border-radius: 50%;
}

.hero-orbit-one {
  inset: 12px;
}

.hero-orbit-two {
  inset: 28px;
}

.hero-orbit-three {
  inset: 46px;
}

.hero-node {
  position: absolute;
  width: 12px;
  height: 12px;
  border-radius: 50%;
  background: linear-gradient(135deg, #13c2c2, #1869ff);
  box-shadow: 0 8px 18px rgba(24, 105, 255, 0.26);
}

.hero-node-one {
  top: 20px;
  right: 24px;
}

.hero-node-two {
  bottom: 24px;
  right: 18px;
}

.hero-node-three {
  bottom: 30px;
  left: 22px;
}

.hero-node-four {
  top: 34px;
  left: 18px;
}

.hero-center-card {
  position: absolute;
  inset: 50% auto auto 50%;
  transform: translate(-50%, -50%);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  width: 96px;
  height: 96px;
  border-radius: 28px;
  background: linear-gradient(145deg, #17325f, #245edb);
  color: #fff;
  box-shadow: 0 18px 34px rgba(27, 68, 139, 0.24);
}

.hero-center-kicker {
  font-size: 9px;
  letter-spacing: 0.16em;
  opacity: 0.72;
}

.hero-center-card strong {
  margin-top: 5px;
  font-size: 16px;
  line-height: 1.1;
}

.hero-center-card span:last-child {
  margin-top: 2px;
  font-size: 11px;
  opacity: 0.84;
}

.hero-art-panel {
  position: absolute;
  display: flex;
  flex-direction: column;
  gap: 4px;
  padding: 12px 14px;
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.84);
  border: 1px solid rgba(117, 136, 167, 0.14);
  box-shadow: 0 16px 36px rgba(20, 45, 86, 0.08);
  backdrop-filter: blur(10px);
}

.hero-art-panel span {
  color: #7d8da8;
  font-size: 11px;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.hero-art-panel strong {
  color: #16233d;
  font-size: 15px;
  line-height: 1.2;
}

.hero-art-panel-top {
  top: 0;
  right: 0;
}

.hero-art-panel-bottom {
  right: 10px;
  bottom: -6px;
}

.hero-stat {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
  min-width: 136px;
  padding: 20px 20px 18px;
  border-radius: 24px;
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.9), rgba(245, 249, 255, 0.95));
  border: 1px solid rgba(117, 136, 167, 0.15);
  box-shadow: 0 14px 34px rgba(20, 45, 86, 0.07), inset 0 1px 0 rgba(255, 255, 255, 0.78);
}

.hero-stat-value {
  display: block;
  color: #15213a;
  font-size: 38px;
  font-weight: 800;
  line-height: 1.2;
}

.hero-stat-label {
  display: block;
  margin-top: 10px;
  color: #7d8da8;
  font-size: 14px;
  font-weight: 500;
}

.main-grid {
  display: flex;
  width: 100%;
}

.main-column {
  padding-right: 20px;
}

.sidebar-column {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.hero-carousel-wrap {
  margin-bottom: 18px;
}

.hero-carousel {
  overflow: hidden;
  border-radius: var(--radius-xl);
  box-shadow: var(--shadow-float);
}

.feed-shell {
  padding: 12px 0 8px;
}

.feed-heading {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 16px;
  padding: 12px 28px 8px;
}

.feed-title {
  margin: 10px 0 0;
  color: #172033;
  font-size: 24px;
  font-weight: 800;
}

.feed-tip {
  color: #8a99b4;
  font-size: 13px;
}

#components-layout-basic .article-check-left-buttons {
  position: relative;
  top: 0;
  margin-bottom: 14px;
  display: none;
}

.sidebar-card {
  overflow: hidden;
}

.sidebar-footer {
  padding: 8px 8px 16px;
}

.index-drawer-wrap .ant-drawer-content-wrapper {
  width: 250px !important;
}

@media screen and (max-width: 1100px) {
  .hero-panel {
    flex-direction: column;
    align-items: flex-start;
    min-height: 0;
  }

  .hero-art {
    width: 100%;
    min-width: 0;
    margin: 8px 0 4px;
  }

  .hero-stats {
    width: 100%;
  }
}

@media screen and (max-width: 900px) {
  #components-layout-basic .content {
    margin-top: 78px;
    padding: 0 12px 28px;
  }

  .hero-panel {
    padding: 24px 20px;
    margin-bottom: 18px;
  }

  .hero-title {
    font-size: 38px;
  }

  .hero-subtitle {
    font-size: 15px;
  }

  .hero-art {
    display: none;
  }

  .main-column {
    padding-right: 0;
  }

  .feed-heading {
    padding: 10px 18px 6px;
  }

  .feed-title {
    font-size: 21px;
  }
}
</style>
