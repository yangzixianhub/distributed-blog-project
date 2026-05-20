<template>
  <a-layout>
    <a-layout id="about-index">
      <IndexHeader class="header"/>
      <a-layout-content>
        <main class="about_content" :style="$store.state.collapsed ? 'width: 100%;' : 'width: 800px;'">
          <div class="about-panel">
            <section class="mission">
              <div class="inner">
                <div class="mission-content">
                  <div class="title">关于</div>
                  <div class="sub">这里是一个分布式小组作业，用于内容发布、评论互动、标签管理和资源导航的社区系统。</div>
                  <div class="sub">本项目由小组成员协作完成，旨在深刻理解分布式系统的设计与实现。</div>
                </div>
              </div>
            </section>
          </div>
        </main>
      </a-layout-content>
      <FooterButtons v-if="!$store.state.collapsed"/>
    </a-layout>
  </a-layout>
</template>

<script>
import IndexHeader from "@/components/index/head/IndexHeader";
import FooterButtons from "@/components/utils/FooterButtons";

export default {
  name: "About",

  components: {IndexHeader, FooterButtons},

  mounted() {
    // 滚动条恢复初始位置的方法
    this.$nextTick(() => {
      let dom = document.querySelector('#app');
      if (dom !== null) {
        dom.scrollTop = 0;
      }
    })
  }
};
</script>


<style lang="less">
#about-index {
  .header {
    position: fixed;
    width: 100%;
    z-index: 999;
    border-bottom: 1px solid #00000021;
  }

  .about_content {
    margin-top: 64px;
  }

  .ant-layout-header, .ant-layout-content {
    display: flex;
    align-items: center;
    justify-content: center;
  }

  .ant-layout-header {
    height: auto;
    line-height: 2.3;
  }

  .about-panel {
    margin-bottom: 14px;
    border-radius: 28px;
    overflow: hidden;
  }

  .mission {
    padding: 38px 36px;
    box-sizing: border-box;

    .title {
      font-size: 32px;
      font-weight: bold;
    }

    .sub {
      margin: 10px 0;
      font-size: 16px;
      color: #999;
    }
  }
}

</style>
