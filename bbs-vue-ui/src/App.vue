<template>
  <a-config-provider :locale="lang[$store.state.locale]">
    <div v-if="$store.state.width" id="app">
      <router-view/>
    </div>
  </a-config-provider>
</template>

<script>
import {mapMutations} from "vuex";
import userService from "./service/userService";
import zh_CN from "ant-design-vue/lib/locale-provider/zh_CN";
import en_US from "ant-design-vue/lib/locale-provider/en_US";

export default {
  data() {
    return {
      lang: {
        zh_CN: zh_CN,
        en_US: en_US
      }
    };
  },
  methods: {
    ...mapMutations(["changeColor"]),
    getAccess() {
      userService.getCurrentUserAccess()
          .then(res => {
            this.checkErrorPage(res);
            if (res.code === 0) {
              this.$store.state.userId = res.data.userId;
              this.$store.state.isLogin = true;
              res.data.roles.forEach(data => {
                if (data.grade === "NS_SUPER_ADMIN_ROLE") {
                  this.$store.state.isManage = true;
                }
              });
            }
          })
          .catch(() => {
          });
    },
    initDom() {
      const that = this;
      window.onload = setWidth;
      window.onresize = setWidth;

      function setWidth() {
        that.$store.state.width = window.innerWidth;
        that.$store.state.height = window.innerHeight;
        that.$store.state.collapsed = window.innerWidth < 1000;
        that.$store.state.collapsedMax = window.innerWidth < 1300;
      }
    },
    checkErrorPage() {
      if (this.$route.path === "/500") {
        this.$router.push({path: "/"});
      }
    },
    setLanguageAndTheme() {
      let navLanguage;
      if (navigator.language === "zh-CN") {
        navLanguage = "zh_CN";
      } else {
        navLanguage = "en_US";
      }
      this.$store.state.locale = localStorage.language ? localStorage.language : navLanguage;
      this.changeColor(localStorage.themeColor || "#13c2c2");
    },
    setIsCarousel() {
      if (Number(window.localStorage.isCarousel) === 0) {
        this.$store.state.isCarousel = 0;
      } else {
        this.$store.state.isCarousel = 1;
      }
    }
  },
  created() {
    this.setLanguageAndTheme();
    this.setIsCarousel();
  },
  mounted() {
    this.initDom();
    this.getAccess();
  }
};
</script>

<style>
:root {
  --page-bg: #f4f7fb;
  --page-bg-accent: radial-gradient(circle at top left, rgba(19, 194, 194, 0.2), transparent 28%),
  radial-gradient(circle at top right, rgba(24, 105, 255, 0.12), transparent 24%),
  linear-gradient(180deg, #fbfdff 0%, #f4f7fb 45%, #eef3f8 100%);
  --surface-strong: rgba(255, 255, 255, 0.9);
  --surface-soft: rgba(255, 255, 255, 0.72);
  --surface-border: rgba(114, 136, 170, 0.16);
  --text-primary: #172033;
  --text-secondary: #61718d;
  --shadow-soft: 0 20px 60px rgba(28, 52, 84, 0.08);
  --shadow-float: 0 28px 76px rgba(20, 58, 115, 0.14);
  --radius-xl: 28px;
  --radius-lg: 22px;
  --radius-md: 16px;
}

html,
body {
  min-height: 100%;
  background: #eef3f8;
}

body::before {
  content: "";
  position: fixed;
  inset: 0;
  background: var(--page-bg-accent);
  pointer-events: none;
  z-index: 0;
}

#app {
  position: relative;
  z-index: 1;
  font-family: "Segoe UI", "PingFang SC", "Microsoft YaHei", sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  color: var(--text-primary);
  height: 100%;
  margin: 0;
  padding: 0;
  overflow: auto;
  background: transparent;
}

a {
  transition: color 0.24s ease, opacity 0.24s ease;
}

.glass-card {
  background: var(--surface-strong);
  border: 1px solid var(--surface-border);
  box-shadow: var(--shadow-soft);
  border-radius: var(--radius-lg);
  backdrop-filter: blur(16px);
}

.section-card {
  background: var(--surface-strong);
  border: 1px solid var(--surface-border);
  box-shadow: var(--shadow-soft);
  border-radius: var(--radius-lg);
  overflow: hidden;
}
</style>
