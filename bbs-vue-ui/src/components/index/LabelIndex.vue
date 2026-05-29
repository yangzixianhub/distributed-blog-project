<template>
  <a-layout id="label-index">
    <IndexHeader class="header"/>
    <a-layout-content>
      <main class="content">
        <section class="label-shell glass-card">
          <div class="label-hero">
            <div class="label-copy">
              <span class="label-kicker">TOPIC LIBRARY</span>
              <h1 class="label-title">把兴趣和内容整理成清晰标签</h1>
              <p class="label-subtitle">技术、生活、阅读和灵感都可以归进自己的小主题，找内容也会更轻松。</p>
            </div>
            <div class="label-summary">
              <div class="summary-card">
                <span class="summary-value">{{ listData.length }}</span>
                <span class="summary-label">当前标签</span>
              </div>
            </div>
          </div>

          <LabelContent v-if="finish"
                        :searchContent="searchContent"
                        :data="listData"
                        @refresh="refresh"/>
        </section>
      </main>
    </a-layout-content>
    <FooterButtons v-if="!$store.state.collapsed"/>
  </a-layout>
</template>

<script>
  import IndexHeader from "@/components/index/head/IndexHeader";
  import FooterButtons from "@/components/utils/FooterButtons";
  import CustomEmpty from "@/components/utils/CustomEmpty";
  import LabelContent from "@/components/label/LabelContent";
  import labelService from "@/service/labelService";

  export default {
    components: {IndexHeader, LabelContent, FooterButtons, CustomEmpty},
    data() {
      return {
        listData: [],
        // 子组件搜索框内容
        searchContent: '',
        hasNext: false,
        finish: false,
        params: {currentPage: 1, pageSize: 25},
      };
    },

    methods: {
      //加载更多（滚动加载）
      loadMore() {
        this.params.currentPage++;
        this.getLabelList(this.params, true);
      },

      // 获取标签
      getLabelList(params, isLoadMore) {
        if (!isLoadMore) {
          this.params.currentPage = 1;
        }
        labelService.getLabelList(params)
            .then(res => {
              if (isLoadMore) {
                this.listData = this.listData.concat(res.data.list);
              } else {
                this.listData = res.data.list;
              }
              this.finish = true;
              this.hasNext = res.data.list.length !== 0;
            })
            .catch(err => {
              this.finish = true;
              this.$message.error(err.desc);
            });
      },

      // 刷新列表
      refresh(labelName) {
        this.searchContent = labelName;
        this.params = {currentPage: 1, pageSize: 25};
        if (labelName) {
          this.params.labelName = labelName;
        }
        this.getLabelList(this.params);
      },
    },

    mounted() {
      this.getLabelList(this.params);
      // 监听滚动，做滚动加载
      this.$utils.scroll.call(this, document.querySelector('#app'));
    },

  };
</script>


<style>
  #label-index .header {
    position: fixed;
    z-index: 2100;
    width: 100%;
    background: rgba(255, 252, 247, 0.82);
    border-bottom: 1px solid rgba(190, 176, 156, 0.16);
    box-shadow: 0 10px 34px rgba(91, 74, 49, 0.06);
    backdrop-filter: blur(18px);
  }

  #label-index .content {
    margin-top: 88px;
    width: 100%;
    max-width: 1220px;
    padding: 0 16px 32px;
  }

  #label-index .ant-layout-header, .ant-layout-content {
    display: flex;
    align-items: center;
    justify-content: center;
  }

  #label-index .ant-layout-header {
    background: transparent;
    height: auto;
    line-height: 2.3;
  }

  #label-index .label-shell {
    overflow: hidden;
    padding: 22px;
    border-radius: 32px;
    background: linear-gradient(135deg, rgba(255, 251, 244, 0.96) 0%, rgba(249, 245, 238, 0.94) 48%, rgba(242, 247, 238, 0.96) 100%);
    box-shadow: 0 24px 60px rgba(94, 78, 58, 0.08);
  }

  #label-index .label-hero {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 22px;
    margin-bottom: 18px;
    padding: 14px 16px 10px;
  }

  #label-index .label-copy {
    max-width: 760px;
  }

  #label-index .label-kicker {
    display: inline-flex;
    align-items: center;
    padding: 7px 14px;
    border-radius: 999px;
    background: linear-gradient(135deg, rgba(245, 180, 111, 0.18), rgba(160, 201, 169, 0.14));
    color: #8c5e35;
    font-size: 13px;
    font-weight: 700;
    letter-spacing: 0.06em;
    box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.65);
  }

  #label-index .label-title {
    margin: 14px 0 10px;
    font-size: 42px;
    line-height: 1.08;
    color: #334232;
  }

  #label-index .label-subtitle {
    margin: 0;
    color: #7f7a6f;
    font-size: 16px;
    line-height: 1.85;
  }

  #label-index .label-summary {
    display: grid;
    grid-template-columns: repeat(2, minmax(0, 1fr));
    gap: 14px;
    min-width: 270px;
  }

  #label-index .summary-card {
    display: flex;
    flex-direction: column;
    justify-content: center;
    min-height: 148px;
    padding: 22px 24px;
    border-radius: 26px;
    background: rgba(255, 252, 247, 0.94);
    border: 1px solid rgba(184, 166, 139, 0.14);
    box-shadow: 0 14px 32px rgba(118, 98, 72, 0.08);
  }

  #label-index .summary-value {
    color: #334232;
    font-size: 52px;
    line-height: 1;
    font-weight: 700;
  }

  #label-index .summary-label {
    margin-top: 12px;
    color: #8b8377;
    font-size: 15px;
  }

  .index-drawer-wrap .ant-drawer-content-wrapper {
    width: 250px !important;
  }

  #components-layout-demo-custom-trigger .trigger {
    font-size: 18px;
    line-height: 64px;
    padding: 0 24px;
    cursor: pointer;
    transition: color 0.3s;
  }

  #components-layout-demo-custom-trigger .trigger:hover {
    color: #1890ff;
  }

  #components-layout-demo-custom-trigger .logo {
    height: 64px;
    background: rgba(255, 255, 255, 0.2);
    margin: 0;
  }

  @media (max-width: 960px) {
    #label-index .label-hero {
      flex-direction: column;
      align-items: flex-start;
    }

    #label-index .label-summary {
      width: 100%;
      min-width: 0;
    }
  }

  @media (max-width: 768px) {
    #label-index .content {
      padding: 0 10px 24px;
    }

    #label-index .label-shell {
      padding: 16px;
      border-radius: 24px;
    }

    #label-index .label-title {
      font-size: 30px;
    }

    #label-index .label-subtitle {
      font-size: 14px;
    }

    #label-index .summary-card {
      min-height: 120px;
      padding: 18px;
    }

    #label-index .summary-value {
      font-size: 40px;
    }
  }
</style>
