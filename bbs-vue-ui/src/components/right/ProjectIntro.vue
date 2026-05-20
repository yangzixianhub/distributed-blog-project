<template>
  <div class="project-intro-card">
    <div class="intro-hero">
      <span class="intro-badge">COMMUNITY</span>
      <h3 class="intro-title">{{ $t('common.projectIntro') }}</h3>
      <p class="intro-desc">内容、评论、访问和轮播控制统一汇总，让首页信息更集中也更有秩序。</p>
    </div>

    <div class="stats-grid">
      <div class="stat-cell">
        <span class="stat-label">{{ $t("common.article") }}</span>
        <span class="stat-value">{{ data.articleCount || 0 }}</span>
      </div>
      <div class="stat-cell">
        <span class="stat-label">{{ $t("common.comment") }}</span>
        <span class="stat-value">{{ data.commentCount || 0 }}</span>
      </div>
      <div class="stat-cell">
        <span class="stat-label">{{ $t("common.visit") }}</span>
        <span class="stat-value">{{ data.visitCount || 0 }}</span>
      </div>
      <div class="stat-cell stat-switch">
        <span class="stat-label">{{ $t("common.carousel") }}</span>
        <a-switch default-checked @change="carouselSwitch" v-if="$store.state.isCarousel"/>
        <a-switch @change="carouselSwitch" v-else/>
      </div>
    </div>
  </div>
</template>

<script>
import articleService from "@/service/articleService";

export default {
  data() {
    return {
      data: {}
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
    carouselSwitch(checked) {
      this.$store.state.isCarousel = checked ? 1 : 0;
      window.localStorage.isCarousel = checked ? 1 : 0;
    }
  },
  mounted() {
    this.getArticleCommentVisitTotal();
  }
};
</script>

<style scoped>
.project-intro-card {
  padding: 22px;
}

.intro-hero {
  position: relative;
  overflow: hidden;
  padding: 18px 18px 16px;
  border-radius: 20px;
  background: linear-gradient(135deg, rgba(19, 194, 194, 0.12), rgba(24, 105, 255, 0.1));
}

.intro-badge {
  display: inline-flex;
  padding: 5px 10px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.6);
  color: #1869ff;
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.12em;
}

.intro-title {
  margin: 14px 0 8px;
  font-size: 20px;
  color: #172033;
}

.intro-desc {
  margin: 0;
  color: #6d7e99;
  line-height: 1.8;
  font-size: 13px;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
  margin-top: 16px;
}

.stat-cell {
  padding: 16px;
  border-radius: 18px;
  background: rgba(246, 249, 253, 0.95);
  border: 1px solid rgba(117, 136, 167, 0.12);
}

.stat-label {
  display: block;
  color: #7d8ca6;
  font-size: 12px;
}

.stat-value {
  display: block;
  margin-top: 10px;
  color: #172033;
  font-size: 22px;
  font-weight: 800;
}

.stat-switch {
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}
</style>
