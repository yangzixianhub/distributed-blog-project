<template>
  <div id="markdown-toc">
    <header class="article-toc-header">
      {{ $t("common.toc") }}
    </header>
    <a-divider style="margin: 10px 0 0 0;"/>
    <div class="toc-content" v-html="articleHtml"></div>
  </div>
</template>

<script>
export default {
  name: "MarkdownToc",

  props: {
    articleHtml: {type: String, default: ""}
  }
};
</script>

<style lang="less" scoped>
#markdown-toc {
  width: 100%;
  padding-bottom: 10px;

  .toc-content {
    max-height: calc(100vh - 190px);
    overflow: auto;
  }
}

#markdown-toc .article-toc-header {
  padding: 15px 10px 0 15px;
}

#markdown-toc {
  /deep/ * {
    .catalog-list {
      font-weight: 600;
      position: relative;
      font-size: 15px;
    }

    & > li > a {
      position: relative;
      padding-left: 15px;
      line-height: 20px;
    }

    ul,
    li {
      padding: 0;
      margin: 0;
      list-style: none;
    }

    ul > li > a {
      font-size: 14px;
      color: #333333;
      padding-left: 15px;
      font-weight: 500;
      position: relative;
    }

    ul > ul > li > a {
      line-height: 20px;
      font-size: 14px;
      color: #333333;
      padding-left: 30px;
      font-weight: normal;
    }

    ul > ul > ul > li > a {
      line-height: 20px;
      font-size: 14px;
      color: #333333;
      padding-left: 45px;
      font-weight: normal;
    }

    ul > ul > ul > ul > li > a {
      line-height: 20px;
      font-size: 14px;
      color: #333333;
      padding-left: 60px;
      font-weight: normal;
    }

    ul > ul > ul > ul > ul > li > a {
      line-height: 20px;
      font-size: 14px;
      color: #333333;
      padding-left: 75px;
      font-weight: normal;
    }

    ul > ul > ul > ul > ul > ul > li > a {
      line-height: 20px;
      font-size: 14px;
      color: #333333;
      padding-left: 90px;
      font-weight: normal;
    }

    ul > .active > a {
      color: #0366d6;
      font-size: 18px;
      font-weight: 900;
    }

    a {
      color: #000;
      display: block;
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
      padding: 6px 0;

      &:hover {
        background-color: #8b87870a;
      }
    }
  }
}
</style>
