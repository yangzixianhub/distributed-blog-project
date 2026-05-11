<template>
  <div>
    <a-row>
      <a-col :span="24" style="text-align: center">
        <div class="project-intro">
          {{ $t('common.projectIntro') }}
        </div>
      </a-col>
      <a-divider style="font-size: 12px;">{{ $t("common.commonCount") }}</a-divider>
      <a-col :span="24" style="text-align: center">
        <a-col :span="6">
          <p>{{ $t("common.article") }}</p>
          <a-badge :overflow-count="9999999" :count="data.articleCount"
                   :number-style="{ backgroundColor: $store.state.themeColor }"/>
        </a-col>
        <a-col :span="6">
          <p>{{ $t("common.comment") }}</p>
          <a-badge :overflow-count="9999999" :count="data.commentCount"
                   :number-style="{ backgroundColor: $store.state.themeColor }"/>
        </a-col>
        <a-col :span="6">
          <p>{{ $t("common.visit") }}</p>
          <a-badge :overflow-count="9999999" :count="data.visitCount"
                   :number-style="{ backgroundColor: $store.state.themeColor }"/>
        </a-col>
        <a-col :span="6">
          <p>{{ $t("common.carousel") }}</p>
            <a-switch default-checked @change="carouselSwitch" v-if="$store.state.isCarousel"/>
            <a-switch @change="carouselSwitch" v-else/>
        </a-col>
      </a-col>
    </a-row>
    <br>
  </div>
</template>

<script>
import articleService from "@/service/articleService";

export default {
  props: {},
  data() {
    return {
      data: {},
    };
  },

  methods: {
    getArticleCommentVisitTotal() {
      articleService.getArticleCommentVisitTotal()
          .then(res => {
            this.data = res.data;
          })
          .catch(err => {
            this.$message.error(err.desc);
          });
    },

    // 轮播图开关
    carouselSwitch(checked) {
      this.$store.state.isCarousel = checked ? 1 : 0;
      window.localStorage.isCarousel = checked ? 1 : 0;
    },
  },

  mounted() {
    this.getArticleCommentVisitTotal();
  },

}
</script>

<style scoped>
.project-intro {
  line-height: 28px;
  padding: 18px 12px 10px 12px;
}

/* 走马灯 */
.ant-switch {
  margin-top: 3px;
}
</style>
