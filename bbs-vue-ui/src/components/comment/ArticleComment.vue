<template>
  <div id="article-comment" :class="{ collapsed: $store.state.collapsed }">
    <div class="comment-header">
      <div>
        <p class="title">{{ $t('common.comment') }}</p>
        <p class="subtitle">留下你的想法，也看看别人读到这里时的心情。</p>
      </div>
    </div>

    <div class="comment-editor">
      <CreateComment @refresh="refresh" />
    </div>

    <div class="comment-toolbar">
      <div class="toolbar-left">
        <p class="text"><span id="article-comment-all"></span>{{ $t('common.hotComment') }}</p>
        <span class="count">{{ articleCommentCount }}</span>
      </div>
      <a-radio-group :default-value="sortRule" size="small" @change="onChange" class="sort-group">
        <a-radio-button value="hottest">
          {{ $t('common.hottest') }}
        </a-radio-button>
        <a-radio-button value="newest">
          {{ $t('common.newest') }}
        </a-radio-button>
      </a-radio-group>
    </div>

    <a-empty :description="false" v-if="comments.length === 0" class="empty-box" />
    <ChildComment
      v-else
      v-for="(item, index) of comments"
      :key="index"
      :data="item"
      :articleUserId="articleUserId"
      @getCommentByArticleId="getCommentByArticleId"
    />
  </div>
</template>

<script>
import ChildComment from "@/components/comment/ChildComment";
import commentService from "@/service/commentService";
import CreateComment from "@/components/comment/CreateComment";

export default {
  components: { ChildComment, CreateComment },

  props: {
    articleUserId: { type: Number, default: 0 },
    articleCommentCount: { type: Number, default: 0 },
  },

  data() {
    return {
      comments: [],
      sortRule: "hottest",
    };
  },

  methods: {
    onChange(e) {
      this.sortRule = e.target.value;
      this.refresh();
    },

    getCommentByArticleId() {
      commentService
        .getCommentByArticleId({ articleId: this.$route.params.id, sortRule: this.sortRule })
        .then((res) => {
          this.comments = res.data;
        })
        .catch((err) => {
          this.$message.error(err.desc);
        });
    },

    refresh() {
      this.getCommentByArticleId();
      this.$emit("refresh");
    },
  },

  mounted() {
    this.getCommentByArticleId();
  },
};
</script>

<style lang="less" scoped>
#article-comment {
  padding: 28px 30px 34px;

  &.collapsed {
    padding: 18px 14px 24px;
  }

  #article-comment-all {
    margin-top: -70px;
    padding-top: 70px;
  }

  .comment-header {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    gap: 12px;
    margin-bottom: 22px;
  }

  .title {
    margin: 0;
    font-size: 26px;
    font-weight: 700;
    color: #28332b;
    line-height: 1.3;
  }

  .subtitle {
    margin: 8px 0 0;
    font-size: 14px;
    line-height: 1.8;
    color: #8d8677;
  }

  .comment-editor {
    padding: 18px 20px;
    margin-bottom: 22px;
    background: linear-gradient(180deg, rgba(252, 248, 240, 0.96), rgba(248, 242, 231, 0.92));
    border: 1px solid rgba(168, 148, 119, 0.14);
    border-radius: 22px;
  }

  .comment-toolbar {
    display: flex;
    align-items: center;
    justify-content: space-between;
    flex-wrap: wrap;
    gap: 14px;
    margin-bottom: 18px;
  }

  .toolbar-left {
    display: flex;
    align-items: center;
    gap: 10px;
  }

  .text {
    margin: 0;
    font-size: 18px;
    font-weight: 600;
    color: #334133;
    line-height: 1.5;
  }

  .count {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    min-width: 34px;
    height: 34px;
    padding: 0 12px;
    border-radius: 999px;
    background: rgba(232, 239, 223, 0.9);
    color: #6f845f;
    font-weight: 600;
  }

  .sort-group /deep/ .ant-radio-button-wrapper {
    height: 34px;
    line-height: 32px;
    padding: 0 16px;
    border-radius: 999px;
    border: 1px solid rgba(138, 158, 113, 0.24);
    background: rgba(252, 249, 243, 0.95);
    color: #7c776a;
    box-shadow: none;
  }

  .sort-group /deep/ .ant-radio-button-wrapper:not(:first-child)::before {
    display: none;
  }

  .sort-group /deep/ .ant-radio-button-wrapper + .ant-radio-button-wrapper {
    margin-left: 10px;
  }

  .sort-group /deep/ .ant-radio-button-wrapper-checked {
    background: linear-gradient(135deg, #e8efdf 0%, #d9e7cc 100%);
    border-color: rgba(117, 141, 92, 0.28);
    color: #576e47;
  }

  .empty-box {
    padding: 18px 0 4px;
  }

  @media (max-width: 768px) {
    padding: 22px 18px 26px;

    .title {
      font-size: 22px;
    }

    .comment-editor {
      padding: 16px 14px;
      border-radius: 18px;
    }

    .comment-toolbar {
      align-items: flex-start;
    }
  }
}
</style>
