<template>
  <div class="header-left-content">
    <a-menu v-model="current" :mode="$store.state.collapsed ? 'inline' : 'horizontal'">
      <a-menu-item key="frontPage" @click="refresh">{{ $t("common.home") }}</a-menu-item>
      <a-menu-item key="boilingPoint" @click="routerLabel">{{ $t("common.label") }}</a-menu-item>
      <a-menu-item key="liveStreaming" @click="routerResource">{{ $t("common.resource") }}</a-menu-item>
      <a-menu-item key="authorList" @click="routerAuthorList" v-if="$store.state.collapsed">{{ $t("common.authorList") }}</a-menu-item>
      <a-menu-item key="commentDonate" @click="routerCommentDonate" v-if="$store.state.collapsed">{{ $t("common.commentDonate") }}</a-menu-item>
      <a-menu-item key="writeArticle" @click="routerWriteArticle" v-if="$store.state.collapsed">{{ $t("common.writeArticle") }}</a-menu-item>
      <a-divider style="margin: 3px 0" v-if="$store.state.collapsed"/>
      <a-menu-item key="management" @click="routerManagement" v-if="$store.state.collapsed">{{ $t("common.management") }}</a-menu-item>
      <a-menu-item key="about" @click="routerAbout">{{ $t("common.about") }}</a-menu-item>
      <a-menu-item key="globalization" @click="changeLanguage" v-if="$store.state.collapsed">
        <a-icon type="global"/>
        <span>{{ languageTitle }}</span>
      </a-menu-item>
    </a-menu>
  </div>
</template>

<script>
export default {
  data() {
    return {
      current: ["frontPage"]
    };
  },
  computed: {
    languageTitle() {
      if (this.$store.state.locale === "en_US") {
        return "中文";
      }
      return "English";
    }
  },
  methods: {
    changeLanguage() {
      if (this.$store.state.locale === "zh_CN") {
        this.$store.state.locale = "en_US";
        localStorage.language = "en_US";
      } else {
        this.$store.state.locale = "zh_CN";
        localStorage.language = "zh_CN";
      }
    },
    refresh() {
      this.$router.push("/");
    },
    routerLabel() {
      this.$router.push("/label");
    },
    routerResource() {
      this.$router.push("/resource");
    },
    routerAuthorList() {
      this.$router.push("/recommended");
    },
    routerWriteArticle() {
      this.$router.push("/write");
    },
    routerManagement() {
      window.open(this.$store.state.manageDomain, "_blank");
    },
    routerCommentDonate() {
      this.$router.push("/commentDonate");
    },
    routerAbout() {
      this.$router.push("/about");
    }
  },
  mounted() {
    let name = this.$route.name;
    this.current.pop();
    if (name === "home") this.current.push("frontPage");
    if (name === "label") this.current.push("boilingPoint");
    if (name === "resource") this.current.push("liveStreaming");
    if (name === "recommended") this.current.push("authorList");
    if (name === "commentDonate") this.current.push("commentDonate");
    if (name === "about") this.current.push("about");
  }
};
</script>

<style lang="less" scoped>
.header-left-content {
  display: flex;
  justify-content: left;
  align-items: center;
}

.ant-menu-horizontal {
  border-bottom: 0;
  background: transparent;
}

.ant-menu-horizontal > .ant-menu-item,
.ant-menu-horizontal > .ant-menu-submenu {
  margin-top: 0;
  padding: 0 14px;
}

.ant-menu-horizontal > .ant-menu-item::after,
.ant-menu-horizontal > .ant-menu-submenu::after {
  border-bottom: 0 !important;
}

.ant-menu-horizontal > .ant-menu-item-active,
.ant-menu-horizontal > .ant-menu-item-open,
.ant-menu-horizontal > .ant-menu-item-selected,
.ant-menu-horizontal:not(.ant-menu-dark) > .ant-menu-item:hover,
.ant-menu-horizontal > .ant-menu-submenu-active,
.ant-menu-horizontal > .ant-menu-submenu-open,
.ant-menu-horizontal:not(.ant-menu-dark) > .ant-menu-submenu-selected,
.ant-menu-horizontal:not(.ant-menu-dark) > .ant-menu-submenu {
  border-bottom: 2px solid transparent;
}

.ant-menu-horizontal > .ant-menu-item,
.ant-menu-horizontal > .ant-menu-submenu-title,
.ant-menu-horizontal > .ant-menu-submenu {
  color: #746b60;
  font-size: 15px;
  border-radius: 14px;
  transition: background 0.24s ease, color 0.24s ease;
}

.ant-menu-horizontal > .ant-menu-item-selected,
.ant-menu-horizontal > .ant-menu-item:hover {
  color: #3b4735 !important;
  background: rgba(160, 201, 169, 0.12);
}

.ant-menu-inline {
  border-right: 0;
}

.ant-menu-inline .ant-menu-item::after,
.ant-menu-vertical .ant-menu-item::after,
.ant-menu-vertical-left .ant-menu-item::after,
.ant-menu-vertical-right .ant-menu-item::after {
  border-right: 3px solid transparent;
}

.ant-menu:not(.ant-menu-horizontal) .ant-menu-item-selected {
  background-color: transparent;
}
</style>
