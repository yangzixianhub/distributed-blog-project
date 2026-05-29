<template>
  <div id="label-content">
    <div class="label-search">
      <div class="search-box">
        <a-input-search
            v-model="searchContentTemp"
            :placeholder="$t('common.searchLabel')"
            class="search-input"
            @search="onLabelSearch"
        />
      </div>

      <a-popover v-model="labelAddVisible" :title="$t('common.labelAdd')" trigger="click" placement="bottomRight">
        <div slot="content" style="width: 500px;">
          <LabelCreate
              @hideLabelVisibleFn="hideLabelVisibleFn"
              @refresh="refresh"/>
        </div>
      </a-popover>

      <a-button
          v-if="$store.state.isManage"
          class="add-item"
          type="primary"
          @click="labelAddCheck"
      >
        {{ $t('common.add') }}
      </a-button>
    </div>

    <a-empty v-if="data.length === 0" class="empty-state"/>

    <div v-else class="tag-grid">
      <article class="info-box" v-for="item of data" :key="item.id">
        <a-popover v-model="labelEditVisible[item.id]" :title="$t('common.labelEdit')" trigger="click" placement="bottom">
          <div slot="content" style="width: 500px;">
            <LabelCreate
                @hideLabelVisibleFn="hideLabelVisibleFn"
                :labelLogoInit="item.logo"
                :labelId="item.id"
                :labelName="item.labelName"
                @refresh="refresh"/>
          </div>
        </a-popover>

        <div class="card-top">
          <a-avatar class="avatar" :size="74" :src="item.logo" @click="routerLabelToArticle(item.id)"/>

          <a-dropdown v-if="$store.state.isManage" :trigger="['click']">
            <a-menu slot="overlay">
              <a-menu-item key="labelEdit" @click="labelUpdateCheck(item.id)">
                {{ ' ' + $t('common.edit') }}
              </a-menu-item>
              <a-menu-item key="labelDel" @click="labelDelete(item.id)">
                <span style="color: red">{{ ' ' + $t('common.delete') }}</span>
              </a-menu-item>
            </a-menu>
            <button class="card-more" type="button">
              <a-icon type="ellipsis"/>
            </button>
          </a-dropdown>
        </div>

        <div class="title" @click="routerLabelToArticle(item.id)">{{ item.labelName }}</div>
        <div class="meta-article">{{ item.articleUseCount + ' ' + $t('common.article') }}</div>
        <div class="meta-line"></div>
        <div class="meta-note">点进这个标签，看看都整理了哪些内容。</div>
      </article>
    </div>
  </div>
</template>

<script>
import LabelCreate from "@/components/label/LabelCreate";
import labelService from "@/service/labelService";

export default {
  name: "LabelContent",

  components: {LabelCreate},

  props: {
    data: {type: Array, default: []},
    searchContent: {type: String, default: ""},
  },

  data() {
    return {
      searchContentTemp: this.searchContent,
      labelAddVisible: false,
      labelEditVisible: {},
    };
  },

  methods: {
    onLabelSearch(value) {
      this.$emit("refresh", value);
    },

    labelAddCheck() {
      if (this.isLoginFn()) {
        this.labelAddVisible = true;
      }
    },

    labelUpdateCheck(labelId) {
      if (this.isLoginFn()) {
        this.labelEditVisible[labelId] = true;
      }
    },

    hideLabelVisibleFn(labelId) {
      this.labelAddVisible = false;
      this.$set(this.labelEditVisible, labelId, false);
    },

    labelDelete(labelId) {
      if (this.isLoginFn()) {
        this.$confirm({
          centered: true,
          title: this.$t("common.deleteLabelTitle"),
          content: this.$t("common.deletePrompt"),
          onOk: () => {
            labelService.labelDelete(labelId)
                .then(() => {
                  this.refresh();
                })
                .catch(err => {
                  this.$message.error(err.desc);
                });
          },
        });
      }
    },

    refresh() {
      this.$emit("refresh");
    },

    isLoginFn() {
      if (this.$store.state.isLogin) {
        return true;
      }
      this.$store.state.loginVisible = true;
    },

    routerLabelToArticle(labelId) {
      let routeData = this.$router.resolve("/label/" + labelId);
      window.open(routeData.href, "_blank");
    },

    updateLabelEditVisible() {
      const labelEditVisibleNew = {};
      this.data.forEach(value => {
        labelEditVisibleNew[value.id] = false;
      });
      this.labelEditVisible = labelEditVisibleNew;
    },
  },

  mounted() {
    this.updateLabelEditVisible();
  },

  watch: {
    data: {
      handler() {
        this.updateLabelEditVisible();
      }
    },
    searchContent: {
      handler(newVal) {
        this.searchContentTemp = newVal;
      }
    }
  }
};
</script>

<style lang="less" scoped>
#label-content {
  .label-search {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 16px;
    padding: 12px 8px 26px;
  }

  .search-box {
    flex: 1;
    max-width: 340px;
  }

  .search-input /deep/ .ant-input {
    height: 48px;
    border-radius: 18px;
    border: 1px solid rgba(183, 166, 142, 0.16);
    background: rgba(255, 252, 246, 0.86);
    box-shadow: 0 10px 24px rgba(143, 120, 88, 0.06);
  }

  .search-input /deep/ .ant-input-search-icon {
    color: #9d927f;
  }

  .add-item {
    height: 46px;
    padding: 0 22px;
    border: none;
    border-radius: 16px;
    background: linear-gradient(135deg, #d7b06f 0%, #8fb791 100%);
    box-shadow: 0 14px 30px rgba(149, 126, 89, 0.16);
  }

  .empty-state {
    padding: 34px 0 16px;
  }

  .tag-grid {
    display: grid;
    grid-template-columns: repeat(5, minmax(0, 1fr));
    gap: 18px;
  }

  .info-box {
    min-height: 270px;
    padding: 22px 20px 20px;
    display: flex;
    flex-direction: column;
    border-radius: 26px;
    background: rgba(255, 252, 247, 0.9);
    border: 1px solid rgba(186, 169, 143, 0.14);
    box-shadow: 0 16px 36px rgba(126, 104, 72, 0.08);
    transition: transform 0.2s ease, box-shadow 0.2s ease;
  }

  .info-box:hover {
    transform: translateY(-4px);
    box-shadow: 0 22px 42px rgba(126, 104, 72, 0.12);
  }

  .card-top {
    display: flex;
    align-items: flex-start;
    justify-content: space-between;
    gap: 12px;
  }

  .avatar {
    flex-shrink: 0;
    cursor: pointer;
    border-radius: 24px;
    box-shadow: 0 12px 26px rgba(120, 102, 82, 0.12);
  }

  .card-more {
    width: 34px;
    height: 34px;
    border: none;
    border-radius: 12px;
    background: rgba(245, 240, 231, 0.92);
    color: #8f8578;
    cursor: pointer;
    box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.58);
  }

  .title {
    margin-top: 18px;
    cursor: pointer;
    font-size: 18px;
    line-height: 1.4;
    color: #334232;
    font-weight: 600;
  }

  .meta-article {
    margin-top: 10px;
    font-size: 15px;
    color: #8d8478;
  }

  .meta-line {
    width: 100%;
    height: 1px;
    margin: 16px 0 14px;
    background: linear-gradient(90deg, rgba(213, 193, 164, 0.34), rgba(213, 193, 164, 0));
  }

  .meta-note {
    color: #9b9387;
    font-size: 13px;
    line-height: 1.75;
  }

  @media (max-width: 1100px) {
    .tag-grid {
      grid-template-columns: repeat(4, minmax(0, 1fr));
    }
  }

  @media (max-width: 900px) {
    .tag-grid {
      grid-template-columns: repeat(3, minmax(0, 1fr));
    }
  }

  @media (max-width: 768px) {
    .label-search {
      flex-direction: column;
      align-items: stretch;
      padding: 8px 0 18px;
    }

    .search-box {
      max-width: none;
    }

    .tag-grid {
      grid-template-columns: repeat(2, minmax(0, 1fr));
      gap: 12px;
    }

    .info-box {
      min-height: 230px;
      padding: 18px 16px;
      border-radius: 20px;
    }
  }

  @media (max-width: 520px) {
    .tag-grid {
      grid-template-columns: 1fr;
    }
  }
}
</style>
