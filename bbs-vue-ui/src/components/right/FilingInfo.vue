<template>
  <div class="div-content">
    <a-row>
      <a-tooltip placement="bottom">
        <template slot="title">
          {{ $t("common.getTheWord") }}
        </template>
        <a-col class="user_agreement" :span="8" @click="routerBook">{{ $t("common.userGuidance") }}</a-col>
      </a-tooltip>
      <a-col class="links" :span="8">
        <a href="https://juejin.cn/" target="_blank">{{ $t("common.links") }}</a>
      </a-col>
      <a-col class="about_us" :span="8" @click="routerAbout">{{ $t("common.about") }}</a-col>
    </a-row>
    <a-row>
      <a href="/" target="_blank">社区论坛</a>
    </a-row>
  </div>
</template>

<script>
export default {
  name: "FilingInfo",

  methods: {
    // 路由到Book说明页面
    routerBook() {
      window.open('/book', '_blank');
    },

    // 点击跳转到 关于我们 页面
    routerAbout() {
      window.open('/about', '_blank');
    }
  }
}
</script>

<style scoped>
.div-content, a {
  font-size: 12px;
  line-height: 2;
  color: #909090;
  cursor: pointer;
}

.user_agreement:hover, .user_guidance:hover, .links:hover, .about_us:hover, a:hover {
  color: #13c2c2;
}

</style>
