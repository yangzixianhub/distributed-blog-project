<template>
  <div id="friend-donate">
    <div class="donate-inner">
      <span class="block-kicker">SUPPORT</span>
      <h3 class="block-title">一起让社区更有生命力</h3>
      <p class="block-desc">这里可以放赞助、友链、活动招募或社区公告。现在先用作首页的品牌氛围卡片。</p>
      <div class="donate-tags">
        <span>分享</span>
        <span>交流</span>
        <span>创作</span>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: "FriendDonate"
};
</script>

<style lang="less">
#friend-donate {
  padding: 18px;

  .donate-inner {
    padding: 18px;
    border-radius: 20px;
    background: linear-gradient(135deg, #172033 0%, #213a63 100%);
    box-shadow: 0 22px 44px rgba(23, 32, 51, 0.22);
  }

  .block-kicker {
    display: inline-flex;
    padding: 5px 10px;
    border-radius: 999px;
    background: rgba(255, 255, 255, 0.14);
    color: #9fd5ff;
    font-size: 11px;
    font-weight: 700;
    letter-spacing: 0.12em;
  }

  .block-title {
    margin: 16px 0 10px;
    color: #fff;
    font-size: 22px;
    line-height: 1.35;
  }

  .block-desc {
    margin: 0;
    color: rgba(255, 255, 255, 0.74);
    line-height: 1.8;
    font-size: 13px;
  }

  .donate-tags {
    display: flex;
    flex-wrap: wrap;
    gap: 10px;
    margin-top: 18px;

    span {
      padding: 7px 12px;
      border-radius: 999px;
      background: rgba(255, 255, 255, 0.12);
      color: #fff;
      font-size: 12px;
    }
  }
}
</style>
