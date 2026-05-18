<template>
  <a-layout-header>
    <div class="main-header">
      <div class="header-right-logo">
        <div v-if="!$store.state.collapsed" class="header-item-logo" @click="refresh">
          <span class="site-title">社区论坛</span>
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

        <div v-if="$store.state.isLogin && !$store.state.collapsed" class="header-item" @click="routerWrite">
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
              <a-divider style="margin: 3px 0 3px 0"/>
              <a-menu-item key="setUp">
                <i class="iconfont icon-setUp"></i>{{ " " + $t("common.setUp") }}
              </a-menu-item>
              <a-menu-item key="about">
                <i class="iconfont icon-about"></i>{{ " " + $t("common.about") }}
              </a-menu-item>
              <a-divider style="margin: 3px 0 3px 0"/>
              <a-menu-item key="management">
                <i class="iconfont icon-setUp"></i>{{ " " + $t("common.management") }}
              </a-menu-item>
              <a-divider style="margin: 3px 0 3px 0"/>
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
            style="display: flex; align-items: center"
            @click="changeLanguage"
        >
          <a-icon type="global"/>
          <span style="padding-left: 3px">{{ languageTitle }}</span>
        </div>

        <div v-if="!$store.state.isLogin" class="header-item-login">
          <div class="options" @click="showLoginModal">
            <a-button style="border: 1px solid rgba(30,128,255,.3); background: rgba(30,128,255,.05); color: #007fff;">
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
    timeRange: {type: String, default: ""},
  },

  data() {
    return {
      visible: false,
      params: {currentPage: 1, pageSize: 10},
      searchContentTemp: this.searchContent,
      timeRangeTemp: this.timeRange || "",
    }
  },

  computed: {
    ...mapState(["userInfo", "locale", "themeColor", "colorOptions", "systemNotifyCount", "taskNotifyCount"]),
    messageNumbers() {
      return Number(this.systemNotifyCount) + Number(this.taskNotifyCount);
    },

    languageTitle() {
      if (this.$store.state.locale === "en_US") {
        return "中";
      }
      return "En";
    },

    timeRangeOptions() {
      if (this.$store.state.locale === "en_US") {
        return [
          {value: "", label: "Any time"},
          {value: "day", label: "Past day"},
          {value: "week", label: "Past week"},
          {value: "month", label: "Past month"},
          {value: "year", label: "Past year"},
          {value: "older", label: "Older than 1 year"},
        ];
      }
      return [
        {value: "", label: "全部时间"},
        {value: "day", label: "一天内"},
        {value: "week", label: "一周内"},
        {value: "month", label: "一月内"},
        {value: "year", label: "一年内"},
        {value: "older", label: "超过一年前"},
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
      if (key === "writeArticle") {
        this.routerWrite();
      }
      if (key === "PROFILE") {
        this.routerUserCenter(this.$store.state.userId);
      }
      if (key === "setUp") {
        this.routerSetUp();
      }
      if (key === "about") {
        this.routerAbout();
      }
      if (key === "management") {
        this.routerManage();
      }
      if (key === "LOG_OUT") {
        this.logout();
      }
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
      if (normalizedKeyword) {
        query.query = normalizedKeyword;
      }
      if (timeRangeValue) {
        query.timeRange = timeRangeValue;
      }
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
    },
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
  padding: 0 10px;
}

.main-header {
  width: 100%;
  max-width: 1100px;
  background: #fff;
  padding: 0;
  display: flex;
  justify-content: space-between;
  align-items: center;

  .header-right-logo {
    display: flex;
    justify-content: space-between;
    align-items: center;

    .header-item-logo {
      cursor: pointer;
      height: 40px;

      .site-title {
        display: inline-flex;
        align-items: center;
        height: 100%;
        color: #007fff;
        font-size: 20px;
        font-weight: 700;
      }
    }
  }

  .header-right-content {
    display: flex;
    align-items: center;
    justify-content: flex-end;

    .ant-input {
      border: 1px solid #ffffff !important;
    }

    .header-search, .header-item, .header-item-login {
      padding: 0 12px;
    }

    .header-search {
      min-width: 320px;
    }

    .search-bar {
      display: flex;
      align-items: center;
      width: 100%;
      gap: 8px;
    }

    .search-time-filter {
      width: 116px;
      flex-shrink: 0;
    }

    .search-input {
      min-width: 100px;
      width: 100%;
    }

    .badge-container {
      .badge {
        position: relative;
        top: 3px;
      }

      .ant-avatar {
        background-color: transparent;
        overflow: visible;
      }

      .iconfont {
        color: #807a7a;
        cursor: pointer;
      }
    }

    .avatar-container {
      .default-avatar,
      .avatar {
        cursor: pointer;
        user-select: none;
      }

      .default-avatar {
        border-radius: 50%;
      }
    }

    .languages {
      padding-left: 6px;
      box-sizing: content-box;

      .options {
        div {
          height: 40px;
        }
      }
    }

    .header-item {
      cursor: pointer;
      color: #71777c;

      .options:hover {
        opacity: 0.7;
      }
    }

    .header-item-login {
      cursor: auto;
      color: #71777c;

      .options:hover {
        opacity: 0.7;
      }
    }
  }

  .header-message-box {
    .ant-tabs-nav-container {
      padding: 0 16px;
    }
  }

  .avatar-menu-icon {
    font-size: 16px !important;
  }

  .ant-menu-horizontal {
    border-bottom: 0;
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

  .ant-menu-item, .ant-menu-submenu-title {
    padding: 0 16px;
  }

  .ant-tabs-nav .ant-tabs-tab {
    margin: 0;
  }
}

.header-theme-color-config {
  .ant-dropdown-menu {
    margin-top: -2px;
    padding: 12px;

    .color-options {
      display: flex;
      justify-content: space-around;

      div {
        width: 20px;
        height: 20px;
        margin: 0 4px;
        cursor: pointer;
        display: flex;
        align-items: center;
        justify-content: center;
      }
    }
  }
}

@media screen and (max-width: 900px) {
  .header-message-box {
    left: 0 !important;
    position: relative;
    display: flex;
    justify-content: center;
  }

  .header-right-content {
    .header-search, .header-item, .header-item-login {
      padding: 0 8px;
    }

    .search-bar {
      gap: 6px;
    }

    .search-time-filter {
      width: 100px;
    }
  }
}
</style>
