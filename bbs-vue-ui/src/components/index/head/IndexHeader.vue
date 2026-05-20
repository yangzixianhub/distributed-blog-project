<template>
  <a-layout-header>
    <div class="main-header">
      <div class="header-right-logo">
        <div v-if="!$store.state.collapsed" class="header-item-logo" @click="refresh">
          <span class="site-mark">DS</span>
          <span class="site-title">分布式博客</span>
        </div>

        <IndexMenu v-if="!$store.state.collapsed"/>
        <IndexSider v-else/>
      </div>

      <div class="header-right-content">
        <div class="header-search">
          <div class="search-bar">
            <a-select v-model="timeRangeTemp" class="search-time-filter" @change="onSearchFilterChange">
              <a-select-option v-for="option in timeRangeOptions" :key="option.value" :value="option.value">
                {{ option.label }}
              </a-select-option>
            </a-select>
            <a-input-search
                v-model="searchContentTemp"
                :placeholder="$t('common.searchPlaceholder')"
                class="search-input"
                @search="onSearch"
            />
          </div>
        </div>

        <div v-if="$store.state.isLogin && !$store.state.collapsed" class="header-item header-cta" @click="routerWrite">
          <div class="options">
            <span>{{ $t("common.writeArticle") }}</span>
          </div>
        </div>

        <div v-if="!$store.state.isLogin && !$store.state.collapsed" class="header-item" @click="routerManage">
          <div class="options">
            <span>{{ $t("common.management") }}</span>
          </div>
        </div>

        <div class="header-item">
          <a-dropdown overlayClassName="header-theme-color-config" :placement="'bottomRight'" :trigger="['click']">
            <div slot="overlay" class="ant-dropdown-menu">
              <p>{{ $t("common.themeColor") }}</p>
              <div class="color-options">
                <div
                    v-for="color of colorOptions"
                    :key="color"
                    :style="'background: ' + color"
                    @click="changeColor(color)"
                >
                  <a-icon v-if="themeColor === color" style="color: white" type="check"/>
                </div>
              </div>
            </div>
            <div class="options theme-color">
              <i class="iconfont icon-theme" :style="{ color: themeColor }"></i>
            </div>
          </a-dropdown>
        </div>

        <div v-if="$store.state.isLogin" class="header-item badge-container">
          <a-dropdown
              v-model="visible"
              class="dropdown"
              overlayClassName="header-message-box"
              :placement="'bottomRight'"
              :trigger="['click']"
          >
            <div slot="overlay" class="ant-dropdown-menu">
              <MessageBox :visible.sync="visible"/>
            </div>
            <div class="options">
              <a-badge class="badge" :count="$store.state.isLogin ? messageNumbers : 0" :overflow-count="99">
                <i class="iconfont icon-bell"></i>
              </a-badge>
            </div>
          </a-dropdown>
        </div>

        <div v-if="$store.state.isLogin" class="header-item avatar-container">
          <a-dropdown :placement="'bottomRight'" :trigger="['click']">
            <a-menu slot="overlay" @click="handleClick">
              <a-menu-item key="writeArticle">
                <i class="iconfont icon-writeArticle"></i>{{ " " + $t("common.writeArticle") }}
              </a-menu-item>
              <a-menu-item key="PROFILE">
                <i class="iconfont icon-user-picture"></i>{{ " " + $t("common.profile") }}
              </a-menu-item>
              <a-divider style="margin: 3px 0"/>
              <a-menu-item key="setUp">
                <i class="iconfont icon-setUp"></i>{{ " " + $t("common.setUp") }}
              </a-menu-item>
              <a-menu-item key="about">
                <i class="iconfont icon-about"></i>{{ " " + $t("common.about") }}
              </a-menu-item>
              <a-divider style="margin: 3px 0"/>
              <a-menu-item key="management">
                <i class="iconfont icon-setUp"></i>{{ " " + $t("common.management") }}
              </a-menu-item>
              <a-divider style="margin: 3px 0"/>
              <a-menu-item key="LOG_OUT">
                <i class="iconfont icon-quit"></i>{{ " " + $t("common.logOut") }}
              </a-menu-item>
            </a-menu>
            <div class="options">
              <a-avatar v-if="$store.state.picture" class="avatar" :src="$store.state.picture"/>
              <img v-else src="@/assets/img/default_avatar.png" class="default-avatar" width="32"/>
            </div>
          </a-dropdown>
        </div>

        <div
            v-if="!$store.state.collapsed"
            class="header-item languages"
            @click="changeLanguage"
        >
          <a-icon type="global"/>
          <span>{{ languageTitle }}</span>
        </div>

        <div v-if="!$store.state.isLogin" class="header-item-login">
          <div class="options" @click="showLoginModal">
            <a-button class="login-button">
              {{ $t("common.login") }}
            </a-button>
          </div>
          <Login/>
          <Register/>
          <MobileResetPassword/>
          <EmailResetPassword/>
        </div>
      </div>
    </div>
  </a-layout-header>
</template>

<script>
import {mapMutations, mapState} from "vuex";
import loginService from "@/service/loginService";
import Login from "@/components/login/Login";
import MessageBox from "@/components/index/messages/MessageBox";
import Register from "@/components/login/Register";
import MobileResetPassword from "@/components/login/MobileResetPassword";
import EmailResetPassword from "@/components/login/EmailResetPassword";
import IndexMenu from "@/components/index/head/IndexMenu";
import IndexSider from "@/components/index/head/IndexSider";

export default {
  components: {MessageBox, Login, Register, MobileResetPassword, EmailResetPassword, IndexMenu, IndexSider},
  props: {
    searchContent: {type: String, default: ""},
    timeRange: {type: String, default: ""}
  },
  data() {
    return {
      visible: false,
      params: {currentPage: 1, pageSize: 10},
      searchContentTemp: this.searchContent,
      timeRangeTemp: this.timeRange || ""
    };
  },
  computed: {
    ...mapState(["userInfo", "locale", "themeColor", "colorOptions", "systemNotifyCount", "taskNotifyCount"]),
    messageNumbers() {
      return Number(this.systemNotifyCount) + Number(this.taskNotifyCount);
    },
    languageTitle() {
      if (this.$store.state.locale === "en_US") {
        return "中文";
      }
      return "EN";
    },
    timeRangeOptions() {
      if (this.$store.state.locale === "en_US") {
        return [
          {value: "", label: "Any time"},
          {value: "day", label: "Past day"},
          {value: "week", label: "Past week"},
          {value: "month", label: "Past month"},
          {value: "year", label: "Past year"},
          {value: "older", label: "Older than 1 year"}
        ];
      }
      return [
        {value: "", label: "全部时间"},
        {value: "day", label: "最近一天"},
        {value: "week", label: "最近一周"},
        {value: "month", label: "最近一月"},
        {value: "year", label: "最近一年"},
        {value: "older", label: "一年以前"}
      ];
    }
  },
  methods: {
    ...mapMutations(["changeColor"]),
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
    handleClick({key}) {
      if (key === "writeArticle") this.routerWrite();
      if (key === "PROFILE") this.routerUserCenter(this.$store.state.userId);
      if (key === "setUp") this.routerSetUp();
      if (key === "about") this.routerAbout();
      if (key === "management") this.routerManage();
      if (key === "LOG_OUT") this.logout();
    },
    showLoginModal() {
      this.$store.state.loginVisible = true;
    },
    logout() {
      loginService.logout()
          .then(() => {
            this.$router.go(0);
          })
          .catch(err => {
            this.$message.error(err.desc);
          });
    },
    onSearch(value) {
      this.pushSearchRoute(value, this.timeRangeTemp);
    },
    onSearchFilterChange(value) {
      this.timeRangeTemp = value;
      this.pushSearchRoute(this.searchContentTemp, value);
    },
    pushSearchRoute(searchValue, timeRangeValue) {
      const query = {};
      const normalizedKeyword = (searchValue || "").trim();
      if (normalizedKeyword) query.query = normalizedKeyword;
      if (timeRangeValue) query.timeRange = timeRangeValue;
      this.$router.push({path: "/search", query});
    },
    routerUserCenter(userId) {
      let routeData = this.$router.resolve("/user/" + userId);
      window.open(routeData.href, "_self");
    },
    routerWrite() {
      this.$router.push("/write");
    },
    routerSetUp() {
      this.$router.push("/settings/profile");
    },
    routerAbout() {
      window.open("/about", "_blank");
    },
    routerManage() {
      window.open(this.$store.state.manageDomain, "_blank");
    }
  },
  watch: {
    searchContent: {
      handler(newVal) {
        this.searchContentTemp = newVal;
      }
    },
    timeRange: {
      handler(newVal) {
        this.timeRangeTemp = newVal || "";
      }
    }
  }
};
</script>

<style lang="less">
.ant-layout-header {
  padding: 10px 16px;
  position: relative;
  z-index: 2100;
}

.main-header {
  position: relative;
  z-index: 2101;
  width: 100%;
  max-width: 1220px;
  padding: 0;
  display: flex;
  justify-content: space-between;
  align-items: center;

  .header-right-logo {
    display: flex;
    align-items: center;
    gap: 16px;

    .header-item-logo {
      display: inline-flex;
      align-items: center;
      gap: 12px;
      cursor: pointer;
      height: 48px;
      padding: 0 10px 0 0;

      .site-mark {
        display: inline-flex;
        align-items: center;
        justify-content: center;
        width: 38px;
        height: 38px;
        border-radius: 14px;
        background: linear-gradient(135deg, #f1b06f 0%, #8fb791 100%);
        color: #fffdf7;
        font-family: "HamburgSerial-Xbold", "Segoe UI", sans-serif;
        font-size: 15px;
        letter-spacing: 0.08em;
        box-shadow: 0 12px 24px rgba(175, 138, 92, 0.22);
      }

      .site-title {
        display: inline-flex;
        align-items: center;
        height: 100%;
        color: #384337;
        font-family: "HamburgSerial-Xbold", "Segoe UI", sans-serif;
        font-size: 24px;
        letter-spacing: 0.03em;
      }
    }
  }

  .header-right-content {
    display: flex;
    align-items: center;
    justify-content: flex-end;
    gap: 8px;

    .header-search,
    .header-item,
    .header-item-login {
      padding: 0;
    }

    .header-search {
      min-width: 430px;
    }

    .search-bar {
      display: flex;
      align-items: center;
      width: 100%;
      gap: 10px;
      padding: 7px;
      border-radius: 22px;
      background: rgba(255, 251, 245, 0.92);
      border: 1px solid rgba(199, 182, 159, 0.18);
      box-shadow: 0 8px 22px rgba(121, 100, 73, 0.06), inset 0 1px 0 rgba(255, 255, 255, 0.9);
    }

    .search-time-filter {
      width: 122px;
      flex-shrink: 0;
    }

    .search-input {
      min-width: 120px;
      width: 100%;
    }

    .search-time-filter .ant-select-selection {
      border: 0 !important;
      background: rgba(247, 240, 229, 0.88);
      border-radius: 16px;
      box-shadow: none !important;
    }

    .search-input .ant-input {
      height: 38px;
      border: 0 !important;
      background: transparent;
      box-shadow: none !important;
      color: #4f5549;
    }

    .search-input .ant-input-search-button {
      border: 0 !important;
      background: linear-gradient(135deg, rgba(241, 176, 111, 0.12), rgba(160, 201, 169, 0.1));
      border-radius: 16px;
      color: #8b6b48;
      box-shadow: none !important;
    }

    .header-item,
    .header-item-login {
      display: flex;
      align-items: center;
      height: 46px;
      color: #756e63;
    }

    .header-item .options,
    .header-item-login .options {
      display: inline-flex;
      align-items: center;
      justify-content: center;
      gap: 6px;
      min-height: 40px;
      padding: 0 14px;
      border-radius: 16px;
      transition: transform 0.24s ease, background 0.24s ease, color 0.24s ease, box-shadow 0.24s ease;
    }

    .header-item {
      cursor: pointer;

      .options:hover {
        color: #384337;
        background: rgba(160, 201, 169, 0.12);
        box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.5);
        transform: translateY(-1px);
      }
    }

    .header-cta .options {
      background: linear-gradient(135deg, rgba(241, 176, 111, 0.18), rgba(160, 201, 169, 0.13));
      color: #7f5d38;
      font-weight: 700;
    }

    .badge-container {
      .badge {
        position: relative;
        top: 1px;
      }

      .iconfont {
        font-size: 18px;
        color: #857a6c;
      }
    }

    .avatar-container {
      .default-avatar,
      .avatar {
        cursor: pointer;
        user-select: none;
        box-shadow: 0 10px 24px rgba(97, 80, 57, 0.12);
      }

      .default-avatar {
        border-radius: 50%;
      }
    }

    .languages span {
      padding-left: 6px;
      font-weight: 600;
    }

    .login-button {
      height: 40px;
      padding: 0 18px;
      border: 1px solid rgba(182, 160, 129, 0.2);
      border-radius: 16px;
      background: linear-gradient(135deg, rgba(241, 176, 111, 0.12), rgba(160, 201, 169, 0.1));
      color: #7f5d38;
      box-shadow: none;
    }
  }

  .header-message-box {
    .ant-tabs-nav-container {
      padding: 0 16px;
    }
  }
}

.header-theme-color-config {
  .ant-dropdown-menu {
    margin-top: 8px;
    padding: 14px;
    border-radius: 16px;
    border: 1px solid rgba(191, 177, 157, 0.16);
    box-shadow: 0 24px 60px rgba(97, 80, 57, 0.14);

    p {
      margin-bottom: 10px;
      color: #384337;
      font-weight: 700;
    }

    .color-options {
      display: flex;
      justify-content: space-around;

      div {
        width: 22px;
        height: 22px;
        margin: 0 4px;
        border-radius: 50%;
        cursor: pointer;
        display: flex;
        align-items: center;
        justify-content: center;
      }
    }
  }
}

@media screen and (max-width: 900px) {
  .ant-layout-header {
    padding: 10px 12px;
  }

  .main-header {
    gap: 10px;
  }

  .header-message-box {
    left: 0 !important;
    position: relative;
    display: flex;
    justify-content: center;
  }

  .header-right-content {
    .header-search {
      min-width: 0;
    }

    .search-bar {
      gap: 6px;
      padding: 4px;
      border-radius: 14px;
    }

    .search-time-filter {
      width: 98px;
    }
  }
}
</style>
