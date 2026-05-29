<template>
  <div class="div-content">
    <a-row>
      <a-tooltip placement="bottom">
        <template slot="title">
          {{ $t("common.getTheWord") }}
        </template>
        <a-col class="user_agreement" :span="12" @click="routerBook">{{ $t("common.userGuidance") }}</a-col>
      </a-tooltip>
      <a-col class="about_us" :span="12" @click="routerAbout">{{ $t("common.about") }}</a-col>
    </a-row>
  </div>
</template>

<script>
export default {
  name: "FilingInfo",
  methods: {
    routerBook() {
      window.open("/book", "_blank");
    },
    routerAbout() {
      window.open("/about", "_blank");
    }
  }
};
</script>

<style scoped>
.div-content,
a {
  font-size: 12px;
  line-height: 2;
  color: #7d8ca6;
  cursor: pointer;
}

.div-content {
  padding: 8px 6px;
}

.user_agreement:hover,
.user_guidance:hover,
.about_us:hover,
a:hover {
  color: #1869ff;
}
</style>
