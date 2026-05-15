<template>
  <div id="friend-donate">
    <header class="user-block-header">{{ $t("common.latestComment") }}</header>
  </div>
</template>

<script>
export default {
  name: "FriendDonate"
};
</script>

<style lang="less">
#friend-donate {
  padding-bottom: 10px;

  .user-block-header {
    padding: 15px 10px 0 15px;
  }
}
</style>
