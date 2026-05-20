<template>
  <a-config-provider :locale="lang[$store.state.locale]">
    <div v-if="$store.state.width" id="app">
      <div class="ambient-bg ambient-bg-one"></div>
      <div class="ambient-bg ambient-bg-two"></div>
      <div class="ambient-grain"></div>
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
  --page-bg: #f7eedf;
  --page-bg-accent: linear-gradient(135deg, #fff4e8 0%, #f8ecdf 38%, #eef4e8 100%);
  --surface-strong: rgba(255, 251, 245, 0.84);
  --surface-soft: rgba(255, 250, 244, 0.7);
  --surface-border: rgba(175, 153, 118, 0.14);
  --text-primary: #2f3b31;
  --text-secondary: #7a7a70;
  --shadow-soft: 0 20px 60px rgba(123, 100, 70, 0.09);
  --shadow-float: 0 28px 76px rgba(123, 100, 70, 0.15);
  --radius-xl: 28px;
  --radius-lg: 22px;
  --radius-md: 16px;
}

html,
body {
  min-height: 100%;
  background: #f5ebdc;
}

body::before {
  content: "";
  position: fixed;
  inset: 0;
  background:
    radial-gradient(circle at 14% 18%, rgba(247, 189, 112, 0.42), transparent 24%),
    radial-gradient(circle at 82% 16%, rgba(240, 214, 153, 0.34), transparent 22%),
    radial-gradient(circle at 20% 82%, rgba(193, 219, 183, 0.3), transparent 26%),
    radial-gradient(circle at 86% 76%, rgba(248, 196, 154, 0.3), transparent 22%),
    var(--page-bg-accent);
  background-size: 135% 135%, 145% 145%, 140% 140%, 135% 135%, 100% 100%;
  animation: warmDrift 24s ease-in-out infinite alternate;
  pointer-events: none;
  z-index: 0;
}

body::after {
  content: "";
  position: fixed;
  inset: -12%;
  background:
    radial-gradient(circle at 28% 30%, rgba(255, 249, 239, 0.88), transparent 26%),
    radial-gradient(circle at 72% 62%, rgba(250, 229, 191, 0.66), transparent 28%),
    radial-gradient(circle at 52% 52%, rgba(220, 232, 214, 0.34), transparent 36%);
  filter: blur(42px);
  opacity: 0.82;
  transform-origin: center;
  animation: warmPulse 16s ease-in-out infinite;
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

#app .ambient-bg,
#app .ambient-grain {
  position: fixed;
  inset: 0;
  pointer-events: none;
}

#app .ambient-bg {
  z-index: 0;
}

#app .ambient-bg-one {
  inset: -10%;
  background:
    radial-gradient(circle at 18% 22%, rgba(245, 177, 90, 0.44), transparent 22%),
    radial-gradient(circle at 78% 20%, rgba(247, 215, 143, 0.36), transparent 20%),
    radial-gradient(circle at 22% 78%, rgba(196, 223, 183, 0.34), transparent 24%),
    radial-gradient(circle at 82% 76%, rgba(247, 183, 143, 0.32), transparent 20%);
  filter: blur(28px);
  opacity: 1;
  animation: ambientFloatOne 22s ease-in-out infinite alternate;
}

#app .ambient-bg-two {
  inset: -14%;
  background:
    radial-gradient(circle at 48% 18%, rgba(255, 243, 218, 0.78), transparent 26%),
    radial-gradient(circle at 62% 58%, rgba(241, 219, 174, 0.52), transparent 30%),
    radial-gradient(circle at 34% 52%, rgba(224, 237, 215, 0.42), transparent 32%);
  filter: blur(56px);
  opacity: 0.96;
  animation: ambientFloatTwo 18s ease-in-out infinite alternate;
}

#app .ambient-grain {
  z-index: 0;
  opacity: 0.1;
  background-image:
    linear-gradient(rgba(255, 255, 255, 0.18) 1px, transparent 1px),
    linear-gradient(90deg, rgba(255, 255, 255, 0.12) 1px, transparent 1px);
  background-size: 28px 28px, 28px 28px;
  mask-image: radial-gradient(circle at center, black 55%, transparent 100%);
}

#app > *:not(.ambient-bg):not(.ambient-grain) {
  position: relative;
  z-index: 1;
}

#app .ant-layout,
#app .ant-layout-content,
#app .ant-layout-sider,
#app main,
#app .content,
#app .about_content {
  background: transparent !important;
}

#app .header,
#app .ant-layout-header,
#app .main-header {
  background: rgba(255, 249, 242, 0.78) !important;
  backdrop-filter: blur(18px);
}

#app .content > .ant-row > .ant-col[style*="background: #fff"],
#app .content > .ant-row > .ant-col[style*="background:#fff"],
#app .content > .ant-col[style*="background: #fff"],
#app .content > .ant-col[style*="background:#fff"],
#app .content [style*="background: #fff"],
#app .content [style*="background:#fff"],
#app .about_content > div,
#app .about_content [style*="background: #fff"],
#app .about_content [style*="background:#fff"] {
  background: rgba(255, 250, 243, 0.82) !important;
  border: 1px solid rgba(188, 159, 120, 0.12);
  box-shadow: 0 18px 48px rgba(120, 95, 63, 0.08);
  backdrop-filter: blur(18px);
}

#app .content > .ant-row > .ant-col[style*="#f0f2f5"],
#app .content > .ant-col[style*="#f0f2f5"],
#app .content [style*="#f0f2f5"],
#app .about_content [style*="#f0f2f5"] {
  background: rgba(255, 248, 239, 0.54) !important;
  border-color: rgba(188, 159, 120, 0.08);
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

@keyframes warmDrift {
  0% {
    background-position: 0% 0%, 100% 0%, 0% 100%, 100% 100%, 50% 50%;
  }
  50% {
    background-position: 10% 6%, 92% 8%, 8% 92%, 94% 96%, 50% 50%;
  }
  100% {
    background-position: 16% 12%, 86% 14%, 14% 86%, 88% 90%, 50% 50%;
  }
}

@keyframes warmPulse {
  0% {
    transform: scale(1) translate3d(0, 0, 0);
    opacity: 0.76;
  }
  50% {
    transform: scale(1.04) translate3d(0, -1.2%, 0);
    opacity: 0.96;
  }
  100% {
    transform: scale(1.02) translate3d(0.8%, 0.6%, 0);
    opacity: 0.82;
  }
}

@keyframes ambientFloatOne {
  0% {
    transform: translate3d(-3%, -2%, 0) scale(1);
  }
  50% {
    transform: translate3d(3%, 2.5%, 0) scale(1.08);
  }
  100% {
    transform: translate3d(5%, -3%, 0) scale(1.12);
  }
}

@keyframes ambientFloatTwo {
  0% {
    transform: translate3d(0, 0, 0) scale(1);
  }
  50% {
    transform: translate3d(-3.5%, 2.5%, 0) scale(1.07);
  }
  100% {
    transform: translate3d(3.5%, -2.5%, 0) scale(1.12);
  }
}
</style>
