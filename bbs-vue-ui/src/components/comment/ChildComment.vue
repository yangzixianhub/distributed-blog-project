<template>
  <div id="child-comment" :class="data.depth > 0 ? 'reply-comment' : 'primary-comment'">
    <a-comment :id="'reply-' + data.id">
      <a class="username" slot="author" @click="routerUserCenter(data.commentUser)">
        {{ data.commentUserName }}
        <img :src="require('@/assets/img/level/' + data.level + '.svg')" alt="" @click.stop="routerBook"/>
        <small class="time" slot="title" style="color: #b5b9b9" v-text="$utils.showtime(data.createTime)"></small>
      </a>
      <a-avatar slot="avatar" :src="data.picture ? data.picture : require('@/assets/img/default_avatar.png')"
                @click="routerUserCenter(data.commentUser)"/>
      <p class="comment-content" slot="content">
        <span style="width: 100%">
          <span v-if="data.replyToName" class="reply-prefix">回复给{{ data.replyToName }}：</span>
          <span v-html="data.content">{{ data.content }}</span>
        </span>
<!--        <span class="del" v-if="data.commentUser === $store.state.userId"-->
<!--              @click="deleteComment(data.id)">{{ $t("common.delete") }}</span>-->
      </p>
      <span slot="content">
        <a class="operate comment-like">
          <i class="iconfont icon-like" @click="likeCommentAction(data.id)"
             :style="data.isLike ? 'color:' + $store.state.themeColor : 'color: #8a919f;'">
            <small> {{ data.likeCount === 0 ? '' : data.likeCount }}</small>
          </i>
        </a>
        <a class="operate comment-comment" v-if="data.depth < 2" @click="isShowFn(data.id)">
          <i class="iconfont icon-comment" style="color: #8a919f;">
            <small v-if="isShow"> {{ $t("common.cancelReply") }}</small>
            <small v-else> {{ $t("common.reply") }}</small>
            <small> {{ data.repliesCount }}</small>
          </i>
        </a>
        <!-- 自己的评论 or 自己的文章  都可以删除对应评论信息 -->
        <b v-if="data.commentUser === $store.state.userId || articleUserId === $store.state.userId">
          <a-dropdown :placement="'bottomCenter'" :trigger="['click']">
            <a-menu slot="overlay">
              <a-menu-item key="delete" @click="deleteComment(data.id)">
                <span style="color: red">{{ ' ' + $t("common.delete") }}</span>
              </a-menu-item>
            </a-menu>
            <span class="options comment-operate">
              <a-icon type="ellipsis" style="cursor: pointer"/>
            </span>
          </a-dropdown>
        </b>
      </span>
      <CreateComment v-show="isShow"
                     :preId="preId"
                     @refresh="getCommentByArticleId"/>
      <ChildComment v-if="data.depth < 2"
                    v-for="(item, index) of data.child"
                    :data="item"
                    :key="index"
                    :articleUserId="articleUserId"
                    @getCommentByArticleId="getCommentByArticleId"/>
    </a-comment>
  </div>
</template>

<script>
import userService from "@/service/userService";
import CreateComment from "@/components/comment/CreateComment";
import store from "@/store";
import commentService from "@/service/commentService";

export default {
  name: 'ChildComment',

  components: {CreateComment},

  props: {
    data: {type: Object, default: () => ({})},
    // 当前文章的作者
    articleUserId: {type: Number, default: 0},
  },

  data() {
    return {
      isShow: false,
      preId: 0,
    }
  },

  methods: {
    // 点赞/取消点赞
    likeCommentAction(commentId) {
      userService.updateLikeCommentState({commentId: commentId})
          .then(() => {
            this.$emit("getCommentByArticleId");
          })
          .catch(err => {
            this.$message.error(err.desc);
          });
    },

    // 刷新评论数据
    getCommentByArticleId() {
      this.$emit("getCommentByArticleId");
    },

    // 评论回复 的显示与否
    isShowFn(id) {
      if (this.$store.state.isLogin) {
        this.isShow = !this.isShow;
        this.preId = id;
      } else {
        store.state.loginVisible = true;
      }
    },

    //  删除评论
    deleteComment(commentId) {
      this.$confirm({
        centered: true,
        title: this.$t("common.deleteCommentTitle"),
        content: this.$t("common.deletePrompt"),
        onOk: () => {
          commentService.deleteComment(commentId)
              .then(() => {
                this.$emit("getCommentByArticleId");
              })
              .catch((err) => {
                this.$message.error(err.desc);
              });
        },
      });
    },

    // 路由到用户中心页面
    routerUserCenter(userId) {
      let routeData = this.$router.resolve("/user/" + userId);
      window.open(routeData.href, '_blank');
    },

    // 路由到Book说明页面
    routerBook() {
      let routeData = this.$router.resolve("/book");
      window.open(routeData.href, '_blank');
    },

  },
}
</script>

<style lang="less">
#child-comment {
  /* 背景色逐渐消失-start */
  @keyframes myAnimation {
    from {
      background-color: rgba(19,194,194,0.15);
    }
    to {
      background-color: transparent;
    }
  }
  .selectedComment {
    animation: myAnimation 8s running;
    transition: background-color 8s;
  }
  /* 背景色逐渐消失-end */
}

#child-comment .ant-comment-content-author-name {
  width: 100%;

  .username {
    font-size: 14px;
  }

  .time {
    float: right;
    cursor: auto;
  }

}

#child-comment .operate .iconfont:hover {
  color: #8a919fb3 !important;
}

#child-comment .comment-content {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 8px;
  padding: 16px 18px;
  border-radius: 22px;
  line-height: 1.9;

  span {
    overflow: hidden;
    text-overflow: ellipsis;
    display: -webkit-box;
    -webkit-box-orient: vertical;
    //-webkit-line-clamp: 3;
    /* 实现长英文字母自动换行*/
    word-break: break-all;
  }

  .del {
    cursor: pointer;
    color: red;
    padding-right: 8px;
  }
}

#child-comment .reply-prefix {
  color: #9b7854;
  font-weight: 600;
}

#child-comment.primary-comment .comment-content {
  background: linear-gradient(135deg, rgba(247, 237, 214, 0.92), rgba(252, 245, 231, 0.96));
  border: 1px solid rgba(218, 195, 151, 0.36);
  box-shadow: 0 12px 28px rgba(186, 159, 117, 0.12);
}

#child-comment.reply-comment .comment-content {
  background: linear-gradient(135deg, rgba(236, 221, 202, 0.9), rgba(244, 232, 217, 0.94));
  border: 1px solid rgba(194, 165, 132, 0.34);
  box-shadow: 0 10px 24px rgba(163, 132, 98, 0.1);
}

#child-comment.reply-comment .ant-comment-inner {
  padding: 14px 16px;
  border-radius: 24px;
  background: linear-gradient(135deg, rgba(236, 221, 202, 0.9), rgba(244, 232, 217, 0.94));
  border: 1px solid rgba(194, 165, 132, 0.34);
  box-shadow: 0 12px 28px rgba(163, 132, 98, 0.1);
}

#child-comment.reply-comment .comment-content {
  padding: 10px 0 0;
  border: none;
  border-radius: 0;
  background: transparent;
  box-shadow: none;
}

#child-comment.reply-comment .ant-comment-content-author-name {
  .time {
    color: #a18a72 !important;
  }
}

#child-comment.reply-comment .operate .iconfont,
#child-comment.reply-comment .comment-operate {
  color: #8f7a63 !important;
}

#child-comment .comment-comment, .comment-operate {
  margin-left: 16px;
}

#child-comment .ant-comment-nested {
  margin-left: 44px;
}

#child-comment.reply-comment .ant-comment-nested {
  margin-left: 0;
}

#child-comment .ant-comment-inner {
  padding: 5px 0;
}


</style>
