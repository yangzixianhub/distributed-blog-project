<template>
  <div id="latest-comment" v-if="data.length !== 0">
    <header class="user-block-header">
      <span class="block-kicker">FRESH TALK</span>
      <span class="block-title">{{ $t("common.latestComment") }}</span>
    </header>
    <a-list item-layout="horizontal" :data-source="data" :split="false">
      <a-list-item slot="renderItem" slot-scope="item, index" @click="routerArticleDetail(item.articleId, item.id)">
        <a-list-item-meta>
          <span slot="description" v-html="item.content">{{ item.content }}</span>
          <a-avatar slot="avatar"
                    :src="item.picture ? item.picture : require('@/assets/img/default-avatar.png')"/>
        </a-list-item-meta>
      </a-list-item>
    </a-list>
  </div>
</template>

<script>
import commentService from "@/service/commentService";

export default {
  data() {
    return {
      data: [],
      params: {currentPage: 1, pageSize: 6}
    };
  },
  methods: {
    getLatestComment(params) {
      commentService.getLatestComment(params)
          .then(res => {
            this.data = res.data.list;
          })
          .catch(err => {
            this.$message.error(err.desc);
          });
    },
    routerArticleDetail(articleId, commentId) {
      let routeData = this.$router.resolve("/detail/" + articleId + "#reply-" + commentId);
      window.open(routeData.href, "_blank");
    }
  },
  mounted() {
    this.getLatestComment(this.params);
  }
};
</script>

<style scoped>
#latest-comment {
  padding: 18px 0 12px;
}

#latest-comment .user-block-header {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: 0 18px 14px;
}

#latest-comment .block-kicker {
  color: #13c2c2;
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.12em;
}

#latest-comment .block-title {
  color: #172033;
  font-size: 18px;
  font-weight: 800;
}

#latest-comment .ant-list-item-meta {
  display: flex;
  align-items: flex-start;
  padding: 0 18px;
}

#latest-comment .ant-list-item-meta-avatar .ant-avatar {
  box-shadow: 0 10px 22px rgba(24, 48, 87, 0.1);
}

#latest-comment .ant-list-item-meta-title > a, .ant-list-item-meta-description {
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 3;
  word-break: break-all;
}

#latest-comment .ant-list-item-meta-description {
  font-weight: 500;
  font-size: 12px;
  color: #72829d;
  line-height: 1.9;
}

#latest-comment .ant-list-item, .full-list {
  cursor: pointer;
}

#latest-comment .ant-list-item:hover {
  background: rgba(19, 194, 194, 0.05);
}
</style>
