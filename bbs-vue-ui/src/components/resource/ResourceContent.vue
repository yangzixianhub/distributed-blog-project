<template>
  <div id="resource-content" v-if="categoryList.length !== 0 && data.length !== 0">
    <div class="tabs">
      <div class="tab-group">
        <button
            v-for="item of categoryList"
            :key="item"
            type="button"
            class="tab-chip"
            :class="{ active: size === item }"
            @click="handleCategoryClick(item)"
        >
          {{ item === 'all' ? '全部资源' : item }}
        </button>
      </div>

      <a-popover v-model="resourceAddVisible" :title="$t('common.resourceAdd')" trigger="click" placement="bottomRight">
        <div slot="content" style="width: 500px;">
          <ResourceCreate
              @hideResourceVisibleFn="hideResourceVisibleFn"
              @refresh="refresh"/>
        </div>
      </a-popover>

      <a-button
          v-if="$store.state.isManage"
          class="add-item"
          type="primary"
          @click="resourceAddCheck"
      >
        {{ $t('common.add') }}
      </a-button>
    </div>

    <div class="resource-grid">
      <article class="info-box" v-for="item of data" :key="item.id" @click="routerLink(item.link)">
        <a-popover v-model="resourceEditVisible[item.id]" :title="$t('common.resourceEdit')" trigger="click" placement="bottom">
          <div slot="content" style="width: 500px;">
            <ResourceCreate
                @hideResourceVisibleFn="hideResourceVisibleFn"
                :resourceLogoInit="item.logo"
                :resourceId="item.id"
                :resourceName="item.resourceName"
                :category="item.category"
                :desc="item.desc"
                :link="item.link"
                @refresh="refresh"/>
          </div>
        </a-popover>

        <div class="card-top">
          <div class="head-name">
            <a-avatar class="avatar" :size="54" :src="item.logo"/>
            <div class="title-group">
              <div class="title">{{ item.resourceName }}</div>
              <span class="category-pill">{{ item.category }}</span>
            </div>
          </div>

          <a-dropdown :trigger="['click']" v-if="$store.state.isManage">
            <a-menu slot="overlay">
              <a-menu-item key="resourceEdit" @click="resourceUpdateCheck(item.id)">
                {{ ' ' + $t('common.edit') }}
              </a-menu-item>
              <a-menu-item key="resourceDel" @click="resourceDelete(item.id)">
                <span style="color: red">{{ ' ' + $t('common.delete') }}</span>
              </a-menu-item>
            </a-menu>
            <button class="card-more" type="button" @click.stop>
              <a-icon type="ellipsis"/>
            </button>
          </a-dropdown>
        </div>

        <div class="meta-article">{{ item.desc }}</div>
        <div class="meta-line"></div>
        <div class="meta-link">{{ item.link }}</div>
      </article>
    </div>
  </div>
</template>

<script>
import resourceService from "@/service/resourceService";
import ResourceCreate from "@/components/resource/ResourceCreate";

export default {
  name: "ResourceContent",

  components: {ResourceCreate},

  props: {
    data: {type: Array, default: []},
  },

  data() {
    return {
      size: "all",
      categoryList: [],
      resourceAddVisible: false,
      resourceEditVisible: {},
    };
  },

  methods: {
    getCategorys() {
      resourceService.getCategorys()
          .then(res => {
            this.categoryList = ["all", ...res.data];
          })
          .catch(err => {
            this.$message.error(err.desc);
          });
    },

    resourceAddCheck() {
      if (this.isLoginFn()) {
        this.resourceAddVisible = true;
      }
    },

    resourceUpdateCheck(resourceId) {
      if (this.isLoginFn()) {
        this.resourceEditVisible[resourceId] = true;
      }
    },

    hideResourceVisibleFn(resourceId) {
      this.resourceAddVisible = false;
      this.$set(this.resourceEditVisible, resourceId, false);
    },

    resourceDelete(resourceId) {
      if (this.isLoginFn()) {
        this.$confirm({
          centered: true,
          title: this.$t("common.deleteResourceTitle"),
          content: this.$t("common.deletePrompt"),
          onOk: () => {
            resourceService.resourceDelete(resourceId)
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

    updateResourceEditVisible() {
      const resourceEditVisibleNew = {};
      this.data.forEach(value => {
        resourceEditVisibleNew[value.id] = false;
      });
      this.resourceEditVisible = resourceEditVisibleNew;
    },

    handleCategoryClick(value) {
      this.size = value;
      if (value === "all") {
        this.$emit("refresh", null);
      } else {
        this.$emit("refresh", value);
      }
    },

    routerLink(link) {
      window.open(link, "_blank");
    }
  },

  mounted() {
    this.getCategorys();
    this.updateResourceEditVisible();
  },

  watch: {
    data: {
      handler() {
        this.updateResourceEditVisible();
      }
    },
  }
};
</script>

<style lang="less" scoped>
#resource-content {
  .tabs {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 16px;
    padding: 12px 8px 26px;
  }

  .tab-group {
    display: flex;
    flex-wrap: wrap;
    gap: 10px;
  }

  .tab-chip {
    height: 44px;
    padding: 0 18px;
    border: 1px solid rgba(183, 166, 142, 0.16);
    border-radius: 16px;
    background: rgba(255, 252, 246, 0.86);
    color: #7f786c;
    cursor: pointer;
    box-shadow: 0 10px 24px rgba(143, 120, 88, 0.06);
    transition: all 0.2s ease;
  }

  .tab-chip.active {
    color: #fffaf2;
    border-color: transparent;
    background: linear-gradient(135deg, #d7b06f 0%, #8fb791 100%);
    box-shadow: 0 14px 28px rgba(149, 126, 89, 0.18);
  }

  .add-item {
    height: 46px;
    padding: 0 22px;
    border: none;
    border-radius: 16px;
    background: linear-gradient(135deg, #d7b06f 0%, #8fb791 100%);
    box-shadow: 0 14px 30px rgba(149, 126, 89, 0.16);
  }

  .resource-grid {
    display: grid;
    grid-template-columns: repeat(4, minmax(0, 1fr));
    gap: 18px;
  }

  .info-box {
    min-height: 178px;
    padding: 20px;
    display: flex;
    flex-direction: column;
    border-radius: 24px;
    background: rgba(255, 252, 247, 0.9);
    border: 1px solid rgba(186, 169, 143, 0.14);
    box-shadow: 0 16px 36px rgba(126, 104, 72, 0.08);
    cursor: pointer;
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

  .head-name {
    display: flex;
    align-items: center;
    gap: 14px;
    min-width: 0;
  }

  .avatar {
    flex-shrink: 0;
    border-radius: 18px;
    box-shadow: 0 12px 26px rgba(120, 102, 82, 0.12);
  }

  .title-group {
    min-width: 0;
  }

  .title {
    font-size: 18px;
    line-height: 1.4;
    color: #334232;
    font-weight: 600;
  }

  .category-pill {
    display: inline-flex;
    margin-top: 8px;
    padding: 4px 10px;
    border-radius: 999px;
    background: rgba(245, 238, 227, 0.95);
    color: #9b7a4b;
    font-size: 12px;
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

  .meta-article {
    margin-top: 18px;
    color: #8d8478;
    font-size: 14px;
    line-height: 1.8;
  }

  .meta-line {
    width: 100%;
    height: 1px;
    margin: 16px 0 12px;
    background: linear-gradient(90deg, rgba(213, 193, 164, 0.34), rgba(213, 193, 164, 0));
  }

  .meta-link {
    color: #a39a8f;
    font-size: 12px;
    line-height: 1.7;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  @media (max-width: 1100px) {
    .resource-grid {
      grid-template-columns: repeat(3, minmax(0, 1fr));
    }
  }

  @media (max-width: 900px) {
    .resource-grid {
      grid-template-columns: repeat(2, minmax(0, 1fr));
    }
  }

  @media (max-width: 768px) {
    .tabs {
      flex-direction: column;
      align-items: stretch;
      padding: 8px 0 18px;
    }

    .resource-grid {
      grid-template-columns: 1fr;
      gap: 12px;
    }

    .info-box {
      min-height: 168px;
      padding: 18px 16px;
      border-radius: 20px;
    }
  }
}
</style>
