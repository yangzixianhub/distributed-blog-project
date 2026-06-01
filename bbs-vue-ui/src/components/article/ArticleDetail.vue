<template>
  <div id="article-detail" ref="scrollDiv" :class="{ collapsed: $store.state.collapsed }">
    <div v-if="finish" class="article-detail-inner">
      <div class="article-headline">
        <div class="headline-copy">
          <h1>{{ data.title }}</h1>
        </div>
      </div>

      <div
        class="article-user-card"
        style="display:flex;justify-content:space-between;align-items:center;gap:18px;padding:18px 20px;margin-bottom:8px;background:linear-gradient(135deg, rgba(250,246,238,0.96), rgba(244,239,228,0.88));border:1px solid rgba(166,145,112,0.12);border-radius:22px;"
      >
        <div class="author-info-box" style="display:flex;align-items:center;min-width:0;flex:1;">
          <a-avatar
            class="avatar"
            :src="data.picture ? data.picture : require('@/assets/img/default_avatar.png')"
            :size="56"
            @click="routerUserCenter(data.createUser)"
          />
          <div class="author-identity" style="display:flex;align-items:center;gap:12px;min-width:0;">
            <img
              class="level-badge"
              :src="require('@/assets/img/level/' + data.articleCountDTO.level + '.svg')"
              alt="level"
              @click.stop="routerBook"
            />
            <div class="author-main" style="min-width:0;padding-left:14px;">
              <div class="author-name-row" style="display:flex;align-items:center;gap:8px;">
                <a target="_blank" class="username" @click="routerUserCenter(data.createUser)">
                  <span class="name" style="font-size:18px;font-weight:600;color:#2b392d;">{{ data.createUserName }}</span>
                </a>
              </div>
            </div>
          </div>
        </div>

        <div class="article-actions" style="display:flex;align-items:center;justify-content:flex-end;gap:12px;margin-left:auto;">
          <div class="meta-box" style="display:flex;justify-content:flex-end;">
            <span
              class="meta-pill"
              style="display:inline-flex;align-items:center;min-height:38px;padding:0 14px;border-radius:14px;background:linear-gradient(135deg, #f7e3b5 0%, #f1d291 100%);color:#7b6130;font-size:13px;line-height:1;font-weight:600;border:1px solid rgba(214,177,104,0.42);box-shadow:0 10px 22px rgba(215,181,117,0.2);"
            >{{ data.createTime }}　{{ $t('common.read') }} {{ data.pv }}</span>
          </div>

          <div class="follow-box">
            <div class="edit" v-if="$store.state.userId === data.createUser">
              <a-button
                v-if="!data.articleCountDTO.isFollow"
                class="action-btn action-btn-outline"
                style="min-width:88px;height:38px;border-radius:14px;color:#7b6130;background:linear-gradient(135deg, #f7e3b5 0%, #f1d291 100%);border:1px solid rgba(214,177,104,0.42);box-shadow:0 10px 22px rgba(215,181,117,0.2);font-size:14px;font-weight:600;"
                @click="routerArticleEdit(data.id)"
              >
                {{ $t('common.edit') }}
              </a-button>
            </div>
            <div class="follow" v-else>
              <a-button
                v-if="!data.articleCountDTO.isFollow"
                class="action-btn action-btn-outline"
                @click="updateFollowState(data.createUser)"
              >
                {{ $t('common.follow') }}
              </a-button>
              <a-button
                v-if="data.articleCountDTO.isFollow"
                class="action-btn action-btn-solid"
                @click="updateFollowState(data.createUser)"
              >
                {{ $t('common.haveFollowed') }}
              </a-button>
            </div>
          </div>
        </div>
      </div>

      <div class="article-titleMap" v-if="data.titleMap && showTitleMap">
        <img :src="data.titleMap" alt="cover" @error="showTitleMap = false" />
      </div>

      <div class="article-content" v-if="data.markdown">
        <mavon-editor
          :value="data.markdown"
          :subfield="false"
          defaultOpen="preview"
          :toolbarsFlag="false"
          boxShadowStyle="0"
          previewBackground="#fffaf3"
          codeStyle="obsidian"
          :xssOptions="false"
        ></mavon-editor>
      </div>
    </div>
    <CustomEmpty v-else />
  </div>
</template>

<script>
import articleService from "@/service/articleService";
import userService from "@/service/userService";
import CustomEmpty from "@/components/utils/CustomEmpty";

export default {
  components: { CustomEmpty },

  data() {
    return {
      finish: false,
      data: {},
      showTitleMap: true,
    };
  },

  methods: {
    getArticleById() {
      this.finish = false;
      this.showTitleMap = true;
      articleService
        .getArticleById({ id: this.$route.params.id, isPv: true })
        .then((res) => {
          this.data = res.data;
          this.finish = true;
          const labelIds = [];
          res.data.labelDTOS.forEach((label) => {
            labelIds.push(label.id);
          });
          this.$emit(
            "initLabelIds",
            labelIds,
            this.finish,
            res.data.createUser,
            this.$utils.toToc(res.data.html),
          );

          if (res.data.html) {
            setTimeout(() => {
              this.monitorScrollForTopicHighlight();
              this.$nextTick(() => {
                clearInterval(this.timer);
                this.getCodes();
              });
            }, 800);
          }
        })
        .catch((err) => {
          this.finish = true;
          if (err.code === 4) {
            this.$router.push({
              name: "404",
              params: { pathMatch: this.$route.path.substring(1).split("/") },
            });
          } else {
            this.$message.error(err.desc);
          }
        });
    },

    monitorScrollForTopicHighlight() {
      const toc = document.querySelector("#markdown-toc");
      if (!toc) return;
      const articleContent = document.querySelector(".article-content");
      const topics = articleContent.querySelectorAll("h1,h2,h3,h4,h5,h6");
      const lis = toc.querySelectorAll("li");
      const removeActive = () => {
        for (let i = 0; i < lis.length; i += 1) {
          lis[i].classList.remove("active");
        }
      };

      const tocClickEventCB = (ev) => {
        if (ev.target.nodeName === "A") {
          const parentNode = ev.target.parentNode;
          if (parentNode && parentNode.nodeName === "LI") {
            removeActive();
            parentNode.classList.add("active");
          }
        }
      };
      toc.addEventListener("click", tocClickEventCB);

      const hash = location.hash;
      if (hash) {
        const activeAnchor = toc.querySelector(`a[href="${hash}"]`);
        if (!activeAnchor) {
          lis[0].classList.add("active");
        } else {
          const activeAnchorParent = activeAnchor.parentNode;
          removeActive();
          activeAnchorParent.classList.add("active");
        }
      } else {
        lis[0].classList.add("active");
      }

      const observer = new IntersectionObserver(
        (entries) => {
          for (const entry of entries) {
            if (entry.intersectionRatio > 0) {
              const anchor = entry.target.firstElementChild;
              if (!anchor) return;
              const id = anchor.getAttribute("id");
              const activeAnchor = toc.querySelector(`a[href="#${id}"]`);
              const activeAnchorParent = activeAnchor.parentNode;
              if (activeAnchor) {
                removeActive();
                if (!entry.isIntersecting && entry.intersectionRect.top > 0) {
                  const preTopic = activeAnchorParent.previousSibling;
                  if (preTopic) {
                    preTopic.classList.add("active");
                    break;
                  }
                }
                activeAnchorParent.classList.add("active");
              }
              break;
            }
          }
        },
        {
          rootMargin: "0% 0% -90% 0%",
          threshold: 0.6,
        },
      );

      Array.prototype.forEach.call(topics, (target) => {
        observer.observe(target);
      });

      this.$once("hook:beforeDestroy", () => {
        observer.disconnect();
        toc.removeEventListener("click", tocClickEventCB);
      });
    },

    updateFollowState(toUser) {
      userService
        .updateFollowState({ toUser })
        .then(() => {
          this.getArticleById();
        })
        .catch((err) => {
          this.$message.error(err.desc);
        });
    },

    routerArticleEdit(articleId) {
      this.$router.push("/edit/" + articleId);
    },

    routerUserCenter(userId) {
      const routeData = this.$router.resolve("/user/" + userId);
      window.open(routeData.href, "_blank");
    },

    routerBook() {
      const routeData = this.$router.resolve("/book");
      window.open(routeData.href, "_blank");
    },

    getCodes() {
      this.codes = document.querySelectorAll("pre code");
      if (this.codes.length > 0) {
        for (let i = 0; i < this.codes.length; i += 1) {
          if (this.codes[i].offsetHeight !== 0) {
            return this.init();
          }
          this.timer = setInterval(() => {
            for (let j = 0; j < this.codes.length; j += 1) {
              if (this.codes[j].offsetHeight !== 0) {
                clearInterval(this.timer);
                return this.init();
              }
            }
          }, 500);
          return;
        }
      }
    },

    init() {
      const thisTemp = this;
      this.$nextTick(() => {
        clearInterval(this.timer);
        this.codes.forEach((item) => {
          const pre = item.parentElement;
          const icon =
            '<div class="code-icon">' +
            '<i class="iconfont icon-copy copy-button"></i>' +
            '</div>';

          pre.insertAdjacentHTML("afterbegin", icon);
          const copyButton = pre.firstElementChild.getElementsByClassName("copy-button")[0];
          copyButton.onclick = function () {
            thisTemp.$copyText(pre.lastElementChild.innerText)
              .then(() => {
                thisTemp.$message.success("复制成功", 1);
              })
              .catch(() => {
                thisTemp.$message.error("复制失败", 1);
              });
          };
        });
      });
    },
  },

  mounted() {
    this.getArticleById();
  },

  watch: {
    data() {
      this.$nextTick(() => {
        let hash = location.hash;
        if (hash) {
          this.$nextTick(() => {
            setTimeout(() => {
              hash = "[id='" + hash.substring(1, hash.length) + "']";
              if (!hash.includes("reply-")) {
                document.querySelector(hash).scrollIntoView({ behavior: "smooth" });
              } else {
                document.querySelector(hash).scrollIntoView({ behavior: "smooth", block: "center" });
                document.querySelector(hash).setAttribute("class", "selectedComment");
              }
            }, 400);
          });
        }
      });
    },
  },
};
</script>

<style lang="less">
#article-detail {
  padding: 28px 30px 34px;

  &.collapsed {
    padding: 18px 14px 24px;
  }

  .article-detail-inner {
    color: #3f4a3d;
  }

  .article-headline {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 16px;
    margin-bottom: 22px;
    text-align: center;
  }

  .headline-copy {
    width: 100%;
    min-width: 0;
    padding: 20px 0 26px;
  }

  .eyebrow {
    margin: 0 0 10px;
    font-size: 12px;
    letter-spacing: 0.18em;
    text-transform: uppercase;
    color: #97a086;
  }

  h1 {
    margin: 0;
    font-size: 52px;
    line-height: 1.18;
    font-weight: 400;
    letter-spacing: 0.02em;
    color: #243228;
    text-align: center;
  }

  .article-user-card {
    display: flex;
    justify-content: space-between;
    align-items: center;
    gap: 18px;
    padding: 18px 20px;
    margin-bottom: 8px;
    background: linear-gradient(135deg, rgba(250, 246, 238, 0.96), rgba(244, 239, 228, 0.88));
    border: 1px solid rgba(166, 145, 112, 0.12);
    border-radius: 22px;
  }

  .author-info-box {
    display: flex;
    align-items: center;
    min-width: 0;
    flex: 1;
  }

  .author-identity {
    display: flex;
    align-items: center;
    gap: 12px;
    min-width: 0;
  }

  .avatar {
    flex-shrink: 0;
    cursor: pointer;
    box-shadow: 0 12px 24px rgba(101, 115, 88, 0.18);
  }

  .author-main {
    min-width: 0;
    padding-left: 14px;
  }

  .author-name-row {
    display: flex;
    align-items: center;
    gap: 8px;
    margin-bottom: 8px;
  }

  .username {
    display: inline-flex;
    align-items: center;
    color: #314033;
  }

  .name {
    font-size: 18px;
    font-weight: 600;
    color: #2b392d;
  }

  .level-badge {
    height: 18px;
    width: auto;
    cursor: pointer;
    flex-shrink: 0;
  }

  .article-actions {
    display: flex;
    align-items: center;
    justify-content: flex-end;
    gap: 12px;
    margin-left: auto;
  }

  .meta-box {
    display: flex;
    flex-wrap: wrap;
    gap: 10px;
    justify-content: flex-end;
  }

  .meta-pill {
    display: inline-flex;
    align-items: center;
    min-height: 38px;
    padding: 0 14px;
    border-radius: 14px;
    background: linear-gradient(135deg, #f7e3b5 0%, #f1d291 100%);
    color: #7b6130;
    font-size: 13px;
    line-height: 1;
    font-weight: 600;
    border: 1px solid rgba(214, 177, 104, 0.42);
    box-shadow: 0 10px 22px rgba(215, 181, 117, 0.2);
  }

  .action-btn {
    min-width: 88px;
    height: 38px;
    border-radius: 999px;
    font-size: 14px;
    font-weight: 600;
    transition: all 0.22s ease;
  }

  .action-btn-outline {
    color: #7b6130;
    background: linear-gradient(135deg, #f7e3b5 0%, #f1d291 100%);
    border: 1px solid rgba(214, 177, 104, 0.42);
    box-shadow: 0 10px 22px rgba(215, 181, 117, 0.2);
  }

  .action-btn-outline:hover,
  .action-btn-outline:focus {
    color: #6a5227;
    background: linear-gradient(135deg, #f8e8c1 0%, #f3d79b 100%);
    border-color: rgba(214, 177, 104, 0.5);
    box-shadow: 0 12px 24px rgba(215, 181, 117, 0.24);
  }

  .action-btn-solid {
    color: #fff;
    background: linear-gradient(135deg, #8aa36f 0%, #718f61 100%);
    border: 1px solid transparent;
    box-shadow: 0 12px 24px rgba(124, 151, 103, 0.2);
  }

  .action-btn-solid:hover,
  .action-btn-solid:focus {
    color: #fff;
    background: linear-gradient(135deg, #94ac79 0%, #7b9968 100%);
    border-color: transparent;
  }

  .article-titleMap {
    padding-top: 18px;

    img {
      width: 100%;
      display: block;
      border-radius: 24px;
      object-fit: cover;
      box-shadow: 0 18px 45px rgba(161, 141, 114, 0.16);
    }
  }

  .article-content {
    width: 100%;
    padding-top: 26px;

    h1 > a,
    h2 > a,
    h3 > a,
    h4 > a,
    h5 > a,
    h6 > a {
      margin-top: -72px;
      padding-top: 72px;
    }
  }

  .markdown-body .highlight pre,
  .markdown-body pre {
    padding: 0 !important;
    border-radius: 18px;
    overflow: hidden;
  }

  .hljs {
    padding: 14px;
  }

  .v-note-wrapper .v-note-panel .v-note-show .v-show-content,
  .v-note-wrapper .v-note-panel .v-note-show .v-show-content-html {
    padding: 0;
  }

  .v-note-wrapper {
    z-index: 1;
    background: transparent;
  }

  .v-note-wrapper.markdown-body.shadow {
    min-height: 0;
    box-shadow: none !important;
  }

  .v-show-content {
    color: #495446;
    font-size: 16px;
    line-height: 1.95;
  }

  .v-show-content h1,
  .v-show-content h2,
  .v-show-content h3,
  .v-show-content h4,
  .v-show-content h5,
  .v-show-content h6 {
    color: #29362c;
  }

  .v-show-content blockquote {
    background: rgba(244, 237, 223, 0.68);
    border-left: 4px solid rgba(140, 161, 115, 0.66);
    border-radius: 0 16px 16px 0;
    color: #5f6657;
  }

  .v-show-content table tr {
    background-color: rgba(255, 252, 246, 0.92);
  }

  .v-show-content table tr:nth-child(2n) {
    background-color: rgba(247, 242, 233, 0.92);
  }

  .v-show-content img {
    border-radius: 18px;
    box-shadow: 0 16px 36px rgba(158, 141, 115, 0.12);
  }

  .code-icon {
    .copy-button {
      float: right;
      margin: 10px 10px 0 0;
      padding: 4px 10px;
      color: #fff;
      border-radius: 999px;
      background: rgba(121, 142, 100, 0.82);
      transition: all 0.2s ease;
    }

    .copy-button:hover {
      cursor: pointer;
      background: #556d49;
    }
  }

  @media (max-width: 768px) {
    padding: 22px 18px 26px;

    .article-headline {
      margin-bottom: 18px;
    }

    h1 {
      font-size: 29px;
    }

    .article-user-card {
      flex-direction: column;
      align-items: flex-start;
      padding: 16px;
    }

    .author-info-box {
      width: 100%;
      align-items: flex-start;
    }

    .author-main {
      padding-left: 12px;
    }

    .author-name-row {
      flex-wrap: wrap;
    }

    .author-identity,
    .article-actions,
    .meta-box {
      width: 100%;
      justify-content: flex-start;
    }

    .action-btn {
      height: 36px;
    }

    .article-titleMap img {
      border-radius: 18px;
    }

    .v-show-content {
      font-size: 15px;
      line-height: 1.85;
    }
  }
}
