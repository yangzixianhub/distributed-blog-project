<template>
  <div id="authors-list" v-if="data.length !== 0">
    <header class="user-block-header">
      <span class="block-kicker">PEOPLE</span>
      <span class="block-title">最近常出现的人</span>
    </header>
    <a-list item-layout="horizontal" :data-source="data" :split="false">
      <a-list-item slot="renderItem" slot-scope="item, index" @click="routerUserCenter(item.id)">
        <a-list-item-meta :description="item.intro">
          <span class="username" slot="title">
            <span style="padding-right: 2px;">{{ item.name }}</span>
            <img :src="require('@/assets/img/level/' + item.level + '.svg')" alt="" @click.stop="routerBook"/>
          </span>
          <a-avatar slot="avatar"
                    :src="item.picture ? item.picture : require('@/assets/img/default-avatar.png')"/>
        </a-list-item-meta>
      </a-list-item>
    </a-list>
    <div class="full-list" @click="recommended">
      <a>
        <span>{{ $t("common.fullList") }}</span>
        <a-icon type="right"/>
      </a>
    </div>
  </div>
</template>

<script>
import userService from "@/service/userService";

export default {
  data() {
    return {
      data: [],
      params: {currentPage: 1, pageSize: 3},
      finish: false
    };
  },
  methods: {
    getHotAuthorsList(params) {
      userService.getHotAuthorsList(params)
          .then(res => {
            this.data = res.data.list;
            this.finish = true;
          })
          .catch(err => {
            this.finish = true;
            this.$message.error(err.desc);
          });
    },
    recommended() {
      let routeData = this.$router.resolve("/recommended");
      window.open(routeData.href, "_blank");
    },
    routerUserCenter(userId) {
      let routeData = this.$router.resolve("/user/" + userId);
      window.open(routeData.href, "_blank");
    },
    routerBook() {
      let routeData = this.$router.resolve("/book");
      window.open(routeData.href, "_blank");
    }
  },
  mounted() {
    this.getHotAuthorsList(this.params);
  }
};
</script>

<style scoped>
#authors-list {
  padding: 18px 0 12px;
}

#authors-list .user-block-header {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: 0 18px 14px;
}

#authors-list .block-kicker {
  color: #9b6d42;
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.1em;
}

#authors-list .block-title {
  color: #3d4735;
  font-size: 18px;
  font-weight: 800;
}

#authors-list .username {
  display: flex;
  align-items: baseline;
}

#authors-list .ant-list-item-meta {
  display: flex;
  align-items: center;
  padding: 0 18px;
}

#authors-list .ant-list-item-meta-avatar .ant-avatar {
  box-shadow: 0 10px 22px rgba(109, 92, 65, 0.12);
}

#authors-list .ant-list-item-meta-title > a, .ant-list-item-meta-description {
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 1;
}

#authors-list .ant-list-item-meta-description {
  font-weight: 400;
  font-size: 12px;
  color: #7d7669;
  line-height: 1.8;
}

#authors-list .ant-list-item, .full-list {
  cursor: pointer;
}

#authors-list .ant-list-item:hover {
  background: rgba(242, 176, 108, 0.08);
}

#authors-list .full-list {
  margin: 10px 18px 0;
  padding: 12px 0;
  text-align: center;
  border-top: 1px solid rgba(201, 186, 164, 0.12);
}

#authors-list .full-list:hover {
  color: #9b6d42;
}
</style>
