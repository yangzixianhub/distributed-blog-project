<template>
  <div id="friend-donate">
    <div class="donate-inner">
      <span class="block-kicker">WEEKEND</span>
      <h3 class="block-title">留点松弛感给这个角落</h3>
      <p class="block-desc">若无闲事挂心头，便是人间好时节。</p>
      <div class="donate-tags">
        <span>随笔</span>
        <span>生活</span>
        <span>分享</span>
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
    border-radius: 24px;
    background: linear-gradient(135deg, #f5e3c8 0%, #e8efe2 100%);
    box-shadow: 0 20px 40px rgba(112, 95, 71, 0.14);
  }

  .block-kicker {
    display: inline-flex;
    padding: 5px 10px;
    border-radius: 999px;
    background: rgba(255, 255, 255, 0.52);
    color: #9b6d42;
    font-size: 11px;
    font-weight: 700;
    letter-spacing: 0.1em;
  }

  .block-title {
    margin: 16px 0 10px;
    color: #3d4735;
    font-size: 22px;
    line-height: 1.45;
  }

  .block-desc {
    margin: 0;
    color: #726b60;
    line-height: 1.9;
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
      background: rgba(255, 255, 255, 0.48);
      color: #6e6558;
      font-size: 12px;
    }
  }
}
</style>
