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
    FriendDonate
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
      return this.isSearchMode() ? "搜索结果" : "记录生活与创作";
    },
    heroTitle() {
      if (this.isSearchMode()) {
        return this.searchContent ? `与“${this.searchContent}”相关的内容` : "按时间筛选的搜索结果";
      }
      return "把技术、生活和灵感都写进日常";
    },
    heroSubtitle() {
      if (this.isSearchMode()) {
        return "把更相关、更有价值的文章与讨论整理出来，方便你快速找到真正想看的内容。";
      }
      return "这里可以有技术、随笔、读书、项目与心情。首页不必太冷，也可以像一本被认真整理过的生活手账。";
    },
    statPrimaryLabel() {
      return this.isSearchMode() ? "当前结果数" : "已加载文章";
    },
    statSecondaryLabel() {
      return this.isSearchMode() ? "筛选范围" : "陪伴状态";
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
        return "支持管理置顶与内容整理";
      }
      return this.isSearchMode() ? "按相关性与时间综合排序" : "看看最近有哪些值得读的内容";
    }
  },
  methods: {
    loadMore() {
      this.params.currentPage++;
      if (this.isSearchMode()) {
        this.getSearchArticleList(this.params, true);
        return;
      }
      this.getArticleList(this.params, true);
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
      this.getArticleList(this.params);
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
    },
    articleTopCallBack() {
      this.$nextTick(() => {
        let dom = document.querySelector("#app");
        if (dom !== null) {
          dom.scrollTop = 0;
        }
      });

      this.getArticleList(this.params);
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
      this.getArticleList(this.params);
    }
  }
};
</script>

<style>
#components-layout-basic .header {
  position: fixed;
  z-index: 999;
  width: 100%;
  background: rgba(255, 252, 247, 0.78);
  border-bottom: 1px solid rgba(190, 176, 156, 0.16);
  box-shadow: 0 10px 34px rgba(91, 74, 49, 0.06);
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
  gap: 22px;
  margin-bottom: 24px;
  min-height: 188px;
  padding: 24px 30px;
  border-radius: 32px;
  background: linear-gradient(135deg, rgba(255, 251, 244, 0.98) 0%, rgba(249, 245, 238, 0.96) 48%, rgba(242, 247, 238, 0.98) 100%);
  box-shadow: 0 24px 60px rgba(94, 78, 58, 0.1);
}

.hero-panel::before {
  content: "";
  position: absolute;
  inset: auto -24px -92px auto;
  width: 250px;
  height: 250px;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(242, 176, 108, 0.22), transparent 68%);
}

.hero-panel::after {
  content: "";
  position: absolute;
  inset: -68px auto auto -36px;
  width: 180px;
  height: 180px;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(160, 201, 169, 0.22), transparent 72%);
}

.hero-copy,
.hero-stats {
  position: relative;
  z-index: 1;
}

.hero-copy {
  max-width: 840px;
}

.hero-kicker,
.feed-kicker {
  display: inline-flex;
  align-items: center;
  padding: 7px 14px;
  border-radius: 999px;
  background: linear-gradient(135deg, rgba(245, 180, 111, 0.18), rgba(160, 201, 169, 0.14));
  color: #8c5e35;
  font-size: 13px;
  font-weight: 700;
  letter-spacing: 0.06em;
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.65);
}

.hero-title {
  margin: 14px 0 10px;
  font-family: "HamburgSerial-Xbold", "PingFang SC", "Segoe UI", sans-serif;
  font-size: 48px;
  font-weight: 700;
  letter-spacing: 0.01em;
  line-height: 1.06;
  color: #334232;
}

.hero-subtitle {
  margin: 0;
  max-width: 740px;
  color: #6d7365;
  line-height: 1.75;
  font-size: 16px;
  font-weight: 500;
}

.hero-stats {
  align-self: stretch;
  display: flex;
  gap: 14px;
  min-width: 300px;
}

.hero-stat {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
  min-width: 136px;
  padding: 18px 20px 16px;
  border-radius: 24px;
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.92), rgba(249, 245, 238, 0.96));
  border: 1px solid rgba(198, 181, 154, 0.18);
  box-shadow: 0 14px 34px rgba(112, 95, 71, 0.08), inset 0 1px 0 rgba(255, 255, 255, 0.78);
}

.hero-stat-value {
  display: block;
  color: #364432;
  font-size: 34px;
  font-weight: 800;
  line-height: 1.2;
}

.hero-stat-label {
  display: block;
  margin-top: 10px;
  color: #8a8274;
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
  color: #334232;
  font-size: 24px;
  font-weight: 800;
}

.feed-tip {
  color: #9a8e7f;
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
    padding: 22px 18px;
    margin-bottom: 18px;
  }

  .hero-title {
    font-size: 36px;
  }

  .hero-subtitle {
    font-size: 15px;
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
