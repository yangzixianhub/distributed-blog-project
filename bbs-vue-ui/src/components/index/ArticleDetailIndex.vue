<template>
  <a-layout>
    <a-layout id="article-detail-index">
      <IndexHeader class="header" />
      <a-layout-content>
        <main class="content" :class="{ collapsed: $store.state.collapsed }">
          <div class="article-left-buttons">
            <LeftButtons
              ref="child"
              @articleCommentCountFn="articleCommentCountFn"
            />
          </div>

          <section class="detail-shell" :class="{ collapsed: $store.state.collapsed }">
            <a-col
              :span="$store.state.collapsed ? 24 : 18"
              class="main-column"
            >
              <div class="reading-card article-card">
                <ArticleDetail @initLabelIds="initLabelIds" />
              </div>

              <div class="reading-card comment-card">
                <ArticleComment
                  :articleUserId="userId"
                  :articleCommentCount="articleCommentCount"
                  @refresh="refresh"
                />
              </div>
            </a-col>

            <a-col v-if="!$store.state.collapsed" :span="6" class="side-column">
              <div class="side-stack">
                <div class="side-card" v-if="finishArticleDetail">
                  <AuthorBlock :userId="userId" />
                </div>

                <div class="side-card" v-if="finishArticleDetail">
                  <RelatArticle :labelIds="labelIds" />
                </div>

                <div class="side-card">
                  <AuthorsList />
                </div>

                <div class="side-card filing-card">
                  <FilingInfo />
                </div>
              </div>
            </a-col>
          </section>
        </main>
      </a-layout-content>
      <FooterButtons v-if="!$store.state.collapsed" />
    </a-layout>
  </a-layout>
</template>

<script>
import IndexHeader from "@/components/index/head/IndexHeader";
import AuthorsList from "@/components/right/AuthorsList";
import FilingInfo from "@/components/right/FilingInfo";
import ArticleDetail from "@/components/article/ArticleDetail";
import FooterButtons from "@/components/utils/FooterButtons";
import LeftButtons from "@/components/article/LeftButtons";
import ArticleComment from "@/components/comment/ArticleComment";
import AuthorBlock from "@/components/right/AuthorBlock";
import RelatArticle from "@/components/right/RelatArticle";

export default {
  components: {
    IndexHeader,
    ArticleDetail,
    AuthorBlock,
    AuthorsList,
    FilingInfo,
    FooterButtons,
    LeftButtons,
    ArticleComment,
    RelatArticle,
  },

  data() {
    return {
      articleHtml: "",
      finishArticleDetail: false,
      labelIds: [],
      userId: 0,
      articleCommentCount: 0,
    };
  },

  methods: {
    initLabelIds(labelIds, finishArticleDetail, userId, articleHtml) {
      this.labelIds = labelIds;
      this.finishArticleDetail = finishArticleDetail;
      this.userId = userId;
      this.articleHtml = articleHtml;
    },

    refresh() {
      this.$refs.child.getArticleCountById();
    },

    articleCommentCountFn(commentCount) {
      this.articleCommentCount = commentCount;
    },
  },
};
</script>

<style lang="less">
#article-detail-index {
  min-height: 100vh;
  background:
    radial-gradient(circle at top left, rgba(213, 238, 225, 0.75), transparent 22%),
    radial-gradient(circle at top right, rgba(247, 230, 208, 0.72), transparent 28%),
    linear-gradient(180deg, #f7f3ea 0%, #f5f1e8 45%, #f3efe6 100%);

  .header {
    position: fixed;
    width: 100%;
    z-index: 2100;
    background: rgba(255, 251, 245, 0.94);
    backdrop-filter: blur(18px);
    border-bottom: 1px solid rgba(137, 120, 93, 0.12);
    box-shadow: 0 14px 40px rgba(146, 128, 103, 0.08);
  }

  .ant-layout-header {
    display: flex;
    align-items: center;
    justify-content: center;
    background: transparent;
    height: auto;
    line-height: 2.3;
  }

  .ant-layout-content {
    display: flex;
    justify-content: center;
  }

  .content {
    width: 100%;
    max-width: 1220px;
    margin-top: 84px;
    padding: 0 20px 48px;
  }

  .content.collapsed {
    max-width: 100%;
    padding: 0 12px 36px;
  }

  .detail-shell {
    display: grid;
    grid-template-columns: minmax(0, 3fr) minmax(260px, 1fr);
    gap: 22px;
    align-items: start;
  }

  .detail-shell.collapsed {
    grid-template-columns: 1fr;
  }

  .main-column,
  .side-column {
    width: 100%;
  }

  .reading-card,
  .side-card {
    background: rgba(255, 252, 247, 0.96);
    border: 1px solid rgba(164, 145, 118, 0.14);
    border-radius: 28px;
    box-shadow: 0 18px 45px rgba(154, 136, 110, 0.08);
    overflow: hidden;
  }

  .article-card,
  .comment-card {
    margin-bottom: 20px;
  }

  .side-stack {
    display: flex;
    flex-direction: column;
    gap: 18px;
    position: sticky;
    top: 92px;
  }

  .filing-card {
    background: rgba(255, 250, 243, 0.88);
  }

  .article-left-buttons {
    position: fixed;
    left: max(16px, calc((100vw - 1220px) / 2 - 72px));
    top: 150px;
    z-index: 180;
  }

  .index-drawer-wrap .ant-drawer-content-wrapper {
    width: 250px !important;
  }

  @media (max-width: 1280px) {
    .article-left-buttons {
      left: 10px;
    }
  }

  @media (max-width: 1100px) {
    .detail-shell {
      grid-template-columns: 1fr;
    }

    .side-stack {
      position: static;
    }

    .article-left-buttons {
      display: none;
    }
  }

  @media (max-width: 768px) {
    .content {
      margin-top: 74px;
      padding: 0 10px 28px;
    }

    .reading-card,
    .side-card {
      border-radius: 22px;
    }
  }
}
</style>
