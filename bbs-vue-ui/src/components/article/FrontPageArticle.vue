<template>
  <div id="main-article-content">
    <a-list item-layout="vertical" size="large" :data-source="tempData">
      <a-list-item slot="renderItem" key="item.title" slot-scope="item, index" class="feed-item" style="cursor: pointer;"
                   @click="routerArticleDetail(item.id)">
        <template v-for="{ type, text } in actions" slot="actions">
          <span class="collectLikeComment" :key="type">
            <span v-if="type==='eye'">
              <a-icon :type="type" style="margin-right: 6px;"/>
              <span v-text="item.pv"></span>
            </span>
            <span v-if="type==='like-o'" @click.stop="pageViewsLikeComment(type, item.id, index)">
              <span v-if="item.articleCountDTO.isLike" :style="{color: $store.state.themeColor}">
                <a-icon :type="type" style="margin-right: 6px"/>
                <span v-text="item.articleCountDTO.likeCount"></span>
              </span>
              <span v-else>
                <a-icon :type="type" style="margin-right: 8px"/>
                <span v-text="item.articleCountDTO.likeCount"></span>
              </span>
            </span>
            <span v-if="type==='message'" @click.stop="routerArticleDetailToComment(item.id)">
              <a-icon :type="type" style="margin-right: 6px;"/>
              <span v-text="item.articleCountDTO.commentCount"></span>
            </span>
            <span
                v-if="(($store.state.isManage && isAdminAudit) || $store.state.userId === item.createUser) && type==='ellipsis'"
                @click.stop>
              <a-dropdown :placement="'bottomCenter'" :trigger="['click']">
                <a-menu slot="overlay">
                  <a-menu-item key="articleNotTop" v-if="$store.state.isManage && isAdminAudit && !item.top"
                               @click="articleTop(item.id)">
                    <span style="color: #1869ff">{{ ' ' + $t("common.isTop") }}</span>
                  </a-menu-item>
                  <a-menu-item key="articleTop" v-if="$store.state.isManage && isAdminAudit && item.top"
                               @click="articleNotTop(item.id)">
                    <span style="color: #eb2f96">{{ ' ' + $t("common.isNotTop") }}</span>
                  </a-menu-item>
                  <a-menu-item key="articleEdit" v-if="$store.state.userId === item.createUser"
                               @click="routerArticleEdit(item.id)">
                    <span style="color: #722ed1">{{ ' ' + $t("common.edit") }}</span>
                  </a-menu-item>
                  <a-menu-item key="articleDel" v-if="$store.state.userId === item.createUser"
                               @click="articleDelete(item.id, index)">
                    <span style="color: red">{{ ' ' + $t("common.delete") }}</span>
                  </a-menu-item>
                </a-menu>
                <div class="options">
                  <a-icon :type="type"/>
                </div>
              </a-dropdown>
            </span>
          </span>
        </template>

        <div slot="extra" class="label-titleMap">
          <div slot="title">
            <a v-for="(label, index) in item.labelDTOS" :key="item.labelName" style="float: right"
               @click.stop="routerLabelToArticle(label.id)">
              <span class="label-name">{{ label.labelName }}</span>
              <a-divider v-if="index !== 0" type="vertical"/>
            </a>
          </div>
          <div>
            <img style="padding-top: 10px" :width="$store.state.collapsed ? 80 : 150" alt="logo" v-if="item.titleMap"
                 :src="item.titleMap"/>
          </div>
        </div>

        <a-list-item-meta>
          <a-avatar slot="avatar" :src="item.picture ? item.picture : require('@/assets/img/default_avatar.png')"
                    @click.stop="routerUserCenter(item.createUser)"/>
          <a slot="title" class="username" @click.stop="routerUserCenter(item.createUser)">
            <div class="left">
              <span slot="title" style="padding-right: 2px;">{{ item.createUserName }}</span>
              <img :src="require('@/assets/img/level/' + item.level + '.svg')" alt="" @click.stop="routerBook"/>
              <small style="color: #8d9ab0; padding-left: 10px" v-text="$utils.showtime(item.createTime)"></small>
            </div>
            <a-tooltip placement="left">
              <template slot="title">
                {{ $t("common.top") }}
              </template>
              <a-icon type="fire" :style="{color: $store.state.themeColor}" v-if="item.top"/>
            </a-tooltip>
          </a>
          <template slot="description">
            <span class="article-list-title" v-html="renderTitle(item)"></span>
          </template>
        </a-list-item-meta>
        <div class="article-content" v-html="renderContent(item)"></div>
      </a-list-item>
    </a-list>
    <div style="text-align: center; padding-bottom: 20px;" v-if="!hasNext && finish">
      <a-divider/>
      {{ $t("common.noAgain") }}
    </div>
  </div>
</template>

<script>
import userService from "@/service/userService";
import articleService from "@/service/articleService";

export default {
  props: {
    data: {type: Array, default: []},
    pageSize: {type: Number, default: 0},
    current: {type: Number, default: 1},
    finish: {type: Boolean, default: false},
    hasNext: {type: Boolean, default: false},
    isUserCenter: {type: Boolean, default: false},
    userId: {type: Number, default: 0},
    isAdminAudit: {type: Boolean, default: false}
  },
  data() {
    return {
      tempData: this.data,
      actions: [
        {type: "eye", text: "156"},
        {type: "like-o", text: "156"},
        {type: "message", text: "2"},
        {type: "ellipsis", text: "12"}
      ]
    };
  },
  methods: {
    pageViewsLikeComment(type, articleId, index) {
      if (type === "like-o") {
        userService.updateLikeState({articleId: articleId})
            .then(() => {
              let isLike = this.tempData[index].articleCountDTO.isLike;
              if (isLike) {
                this.tempData[index].articleCountDTO.likeCount--;
              } else {
                this.tempData[index].articleCountDTO.likeCount++;
              }
              this.tempData[index].articleCountDTO.isLike = !isLike;
            })
            .catch(err => {
              this.$message.error(err.desc);
            });
      }
    },
    articleTop(articleId) {
      this.$confirm({
        centered: true,
        title: this.$t("common.confirmTop"),
        onOk: () => {
          articleService.articleTop({id: articleId, top: true})
              .then(() => {
                this.$emit("articleTopCallBack");
                this.$message.success(this.$t("common.topSuccessed"));
              })
              .catch(err => {
                this.$message.error(err.desc);
              });
        }
      });
    },
    articleNotTop(articleId) {
      this.$confirm({
        centered: true,
        title: this.$t("common.confirmNotTop"),
        onOk: () => {
          articleService.articleTop({id: articleId, top: false})
              .then(() => {
                this.$emit("articleTopCallBack");
                this.$message.success(this.$t("common.notTopSuccessed"));
              })
              .catch(err => {
                this.$message.error(err.desc);
              });
        }
      });
    },
    articleDelete(articleId, index) {
      this.$confirm({
        centered: true,
        title: this.$t("common.deleteArticleTitle"),
        onOk: () => {
          articleService.articleDelete(articleId)
              .then(() => {
                this.tempData = this.tempData.filter(article => article.id !== articleId);
              })
              .catch(err => {
                this.$message.error(err.desc);
              });
        }
      });
    },
    routerArticleDetail(articleId) {
      let routeData = this.$router.resolve("/detail/" + articleId);
      window.open(routeData.href, "_blank");
    },
    routerArticleDetailToComment(articleId) {
      let routeData = this.$router.resolve("/detail/" + articleId + "#article-comment-all");
      window.open(routeData.href, "_blank");
    },
    routerUserCenter(userId) {
      let routeData = this.$router.resolve("/user/" + userId);
      window.open(routeData.href, "_blank");
    },
    routerLabelToArticle(labelId) {
      let routeData = this.$router.resolve("/label/" + labelId);
      window.open(routeData.href, "_blank");
    },
    routerBook() {
      let routeData = this.$router.resolve("/book");
      window.open(routeData.href, "_blank");
    },
    routerArticleEdit(articleId) {
      this.$router.push("/edit/" + articleId);
    },
    renderTitle(item) {
      return this.renderHighlightHtml(item.highlightTitle, item.title);
    },
    renderContent(item) {
      return this.renderHighlightHtml(item.highlightContent, item.content);
    },
    renderHighlightHtml(highlightValue, fallbackValue) {
      if (highlightValue) {
        return highlightValue;
      }
      return this.highlightKeyword(this.escapeHtml(fallbackValue || ""));
    },
    highlightKeyword(text) {
      const keyword = (this.$route.query.query || "").trim();
      if (!keyword) {
        return text;
      }
      const pattern = new RegExp(this.escapeRegExp(keyword), "ig");
      return text.replace(pattern, matched => `<em class="search-highlight">${matched}</em>`);
    },
    escapeHtml(text) {
      return text
          .replace(/&/g, "&amp;")
          .replace(/</g, "&lt;")
          .replace(/>/g, "&gt;")
          .replace(/\"/g, "&quot;")
          .replace(/'/g, "&#39;");
    },
    escapeRegExp(text) {
      return text.replace(/[.*+?^${}()|[\]\\]/g, "\\$&");
    }
  },
  watch: {
    data: {
      handler(newVal) {
        this.tempData = newVal;
      }
    }
  }
};
</script>

<style lang="less">
#main-article-content {
  padding: 0 16px 10px;
}

#main-article-content em.ant-list-item-action-split {
  display: none;
}

#main-article-content .ant-list-vertical .ant-list-item-action > li:first-child {
  padding-left: 0;
}

#main-article-content .ant-list-vertical .ant-list-item-action > li {
  padding: 0 12px;
}

#main-article-content .label-titleMap {
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  align-items: flex-end;
}

#main-article-content .label-name {
  color: #8f7a5e;
  font-weight: 600;
  padding: 5px 10px;
  border-radius: 999px;
  background: rgba(245, 180, 111, 0.1);
}

#main-article-content .label-name:hover {
  color: #7b9a74;
  background: rgba(160, 201, 169, 0.16);
}

#main-article-content .username {
  display: flex;
  justify-content: space-between;
  gap: 14px;

  .left {
    display: flex;
    align-items: baseline;
    gap: 8px;
  }
}

#main-article-content .ant-list-item-meta-description {
  font-weight: 800;
  font-size: 24px;
  color: #394534;
  line-height: 1.45;
}

#main-article-content .article-list-title .search-highlight,
#main-article-content .article-content .search-highlight {
  color: #8c5b31;
  background: linear-gradient(180deg, rgba(255, 246, 224, 0) 10%, #f7e7c4 10%);
  font-style: normal;
  padding: 0 3px;
  border-radius: 6px;
}

#main-article-content .ant-list-item-meta-description,
#main-article-content .article-content {
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-box-orient: vertical;
}

#main-article-content .ant-list-item-meta-description {
  -webkit-line-clamp: 1;
}

#main-article-content .article-content {
  margin-top: 10px;
  color: #6f7167;
  line-height: 1.9;
  font-size: 15px;
  -webkit-line-clamp: 2;
}

#main-article-content .collectLikeComment:hover {
  color: #8c5b31;
}

#main-article-content li.ant-list-item {
  margin-bottom: 16px;
  padding: 26px;
  border: 1px solid rgba(200, 186, 165, 0.16);
  border-radius: 26px;
  background: linear-gradient(180deg, rgba(255, 253, 249, 0.98), rgba(250, 246, 240, 0.96));
  box-shadow: 0 16px 38px rgba(109, 92, 65, 0.08);
  transition: transform 0.28s ease, box-shadow 0.28s ease, border-color 0.28s ease;

  .ant-list-item-main {
    width: 50%;
  }
}

#main-article-content li.ant-list-item:hover {
  transform: translateY(-3px);
  border-color: rgba(160, 201, 169, 0.28);
  box-shadow: 0 24px 56px rgba(109, 92, 65, 0.12);
}

#main-article-content .ant-list-item-meta-avatar {
  margin-right: 16px;
}

#main-article-content .ant-list-item-meta-avatar .ant-avatar {
  width: 48px;
  height: 48px;
  box-shadow: 0 10px 22px rgba(109, 92, 65, 0.12);
}

#main-article-content .ant-list-item-extra img {
  max-height: 120px;
  max-width: 168px;
  width: auto;
  border-radius: 20px;
  box-shadow: 0 16px 28px rgba(109, 92, 65, 0.14);
}

#main-article-content .username {
  color: #5f5a50;
}

#main-article-content .username small {
  color: #9a8f80 !important;
}

#main-article-content .ant-list-item-action {
  color: #8e8578;
}

@media screen and (max-width: 576px) {
  #main-article-content {
    padding: 0 10px 8px;
  }

  #main-article-content li.ant-list-item {
    padding: 18px;
    border-radius: 18px;
  }

  #main-article-content .ant-list-item-meta-description {
    font-size: 18px;
  }

  .ant-list-vertical .ant-list-item-extra {
    margin: 0 auto 16px;
  }
}
</style>
