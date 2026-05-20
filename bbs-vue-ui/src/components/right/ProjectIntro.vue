<template>
  <div class="project-intro-card">
    <div class="todo-note">
      <div class="note-head">
        <span class="note-badge">TODO LIST</span>
        <span class="note-count">{{ finishedCount }}/{{ todos.length }}</span>
      </div>

      <h3 class="note-title">把今天想做的事记下来</h3>
      <p class="note-desc">写一点待办，做完就打勾，让这个角落真正用起来。</p>

      <div class="todo-input-wrap">
        <input
            v-model.trim="draft"
            class="todo-input"
            type="text"
            maxlength="40"
            placeholder="写下一件待办事项..."
            @keyup.enter="addTodo"
        />
        <button class="todo-add" @click="addTodo">添加</button>
      </div>

      <div v-if="todos.length" class="todo-list">
        <button
            v-for="item in todos"
            :key="item.id"
            class="todo-item"
            :class="{ done: item.done }"
            @click="toggleTodo(item.id)"
        >
          <span class="todo-check">
            <span class="todo-check-inner"></span>
          </span>
          <span class="todo-text">{{ item.text }}</span>
        </button>
      </div>

      <div v-else class="todo-empty">现在这里还空着，写下第一件想完成的小事吧。</div>
    </div>

    <div class="stats-grid">
      <div class="stat-cell">
        <span class="stat-label">{{ $t("common.article") }}</span>
        <span class="stat-value">{{ data.articleCount || 0 }}</span>
      </div>
      <div class="stat-cell">
        <span class="stat-label">{{ $t("common.comment") }}</span>
        <span class="stat-value">{{ data.commentCount || 0 }}</span>
      </div>
      <div class="stat-cell">
        <span class="stat-label">{{ $t("common.visit") }}</span>
        <span class="stat-value">{{ data.visitCount || 0 }}</span>
      </div>
      <div class="stat-cell stat-switch">
        <span class="stat-label">{{ $t("common.carousel") }}</span>
        <a-switch default-checked @change="carouselSwitch" v-if="$store.state.isCarousel"/>
        <a-switch @change="carouselSwitch" v-else/>
      </div>
    </div>
  </div>
</template>

<script>
import articleService from "@/service/articleService";

const TODO_STORAGE_KEY = "bbs_life_todo_note";

export default {
  data() {
    return {
      data: {},
      draft: "",
      todos: []
    };
  },
  computed: {
    finishedCount() {
      return this.todos.filter(item => item.done).length;
    }
  },
  methods: {
    getArticleCommentVisitTotal() {
      articleService.getArticleCommentVisitTotal()
          .then(res => {
            this.data = res.data;
          })
          .catch(err => {
            this.$message.error(err.desc);
          });
    },
    carouselSwitch(checked) {
      this.$store.state.isCarousel = checked ? 1 : 0;
      window.localStorage.isCarousel = checked ? 1 : 0;
    },
    loadTodos() {
      const localTodos = window.localStorage.getItem(TODO_STORAGE_KEY);
      if (localTodos) {
        try {
          this.todos = JSON.parse(localTodos);
          return;
        } catch (e) {
          window.localStorage.removeItem(TODO_STORAGE_KEY);
        }
      }
      this.todos = [
        {id: 1, text: "整理今天的待办重点", done: false},
        {id: 2, text: "读完一篇值得记录的文章", done: true}
      ];
      this.saveTodos();
    },
    saveTodos() {
      window.localStorage.setItem(TODO_STORAGE_KEY, JSON.stringify(this.todos));
    },
    addTodo() {
      if (!this.draft) {
        return;
      }
      this.todos.unshift({
        id: Date.now(),
        text: this.draft,
        done: false
      });
      this.draft = "";
      this.saveTodos();
    },
    toggleTodo(id) {
      this.todos = this.todos.map(item => {
        if (item.id === id) {
          return {
            ...item,
            done: !item.done
          };
        }
        return item;
      });
      this.saveTodos();
    }
  },
  mounted() {
    this.getArticleCommentVisitTotal();
    this.loadTodos();
  }
};
</script>

<style scoped>
.project-intro-card {
  padding: 20px;
}

.todo-note {
  position: relative;
  overflow: hidden;
  padding: 20px 18px 18px;
  border-radius: 26px;
  background: linear-gradient(145deg, rgba(248, 228, 188, 0.58), rgba(220, 235, 214, 0.42));
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.58);
}

.note-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.note-badge,
.note-count {
  display: inline-flex;
  align-items: center;
  padding: 5px 12px;
  border-radius: 999px;
  background: rgba(255, 252, 247, 0.72);
  color: #9b6c3f;
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.08em;
}

.note-title {
  margin: 16px 0 8px;
  color: #384637;
  font-size: 19px;
  line-height: 1.35;
}

.note-desc {
  margin: 0 0 16px;
  color: #6f6f63;
  line-height: 1.8;
  font-size: 13px;
}

.todo-input-wrap {
  display: flex;
  gap: 10px;
  align-items: center;
}

.todo-input {
  flex: 1;
  min-width: 0;
  height: 40px;
  padding: 0 14px;
  border: 1px solid rgba(188, 159, 120, 0.18);
  border-radius: 14px;
  background: rgba(255, 252, 247, 0.85);
  color: #3f4738;
  outline: none;
  transition: border-color 0.2s ease, box-shadow 0.2s ease;
}

.todo-input:focus {
  border-color: rgba(169, 132, 84, 0.45);
  box-shadow: 0 0 0 4px rgba(240, 205, 158, 0.2);
}

.todo-add {
  height: 40px;
  padding: 0 16px;
  border: none;
  border-radius: 14px;
  background: linear-gradient(135deg, #d6b071, #c8b986);
  color: #fffaf2;
  font-weight: 700;
  cursor: pointer;
  transition: transform 0.18s ease, box-shadow 0.18s ease;
  box-shadow: 0 10px 20px rgba(183, 145, 84, 0.2);
}

.todo-add:hover {
  transform: translateY(-1px);
  box-shadow: 0 14px 22px rgba(183, 145, 84, 0.25);
}

.todo-list {
  display: grid;
  gap: 10px;
  margin-top: 16px;
}

.todo-item {
  display: flex;
  align-items: center;
  gap: 12px;
  width: 100%;
  padding: 12px 13px;
  border: 1px solid rgba(193, 170, 136, 0.16);
  border-radius: 16px;
  background: rgba(255, 251, 245, 0.76);
  color: #3f4738;
  text-align: left;
  cursor: pointer;
  transition: background 0.2s ease, transform 0.18s ease;
}

.todo-item:hover {
  transform: translateY(-1px);
  background: rgba(255, 253, 249, 0.9);
}

.todo-check {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 22px;
  height: 22px;
  border-radius: 50%;
  border: 1.5px solid rgba(171, 141, 101, 0.55);
  background: rgba(255, 250, 242, 0.95);
  flex-shrink: 0;
}

.todo-check-inner {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: transparent;
  transition: background 0.18s ease, transform 0.18s ease;
}

.todo-text {
  line-height: 1.55;
  font-size: 13px;
}

.todo-item.done {
  background: rgba(244, 247, 238, 0.88);
}

.todo-item.done .todo-check {
  border-color: rgba(104, 152, 103, 0.6);
  background: rgba(238, 246, 232, 0.96);
}

.todo-item.done .todo-check-inner {
  background: #7cab72;
  transform: scale(1.05);
}

.todo-item.done .todo-text {
  color: #7b7f73;
  text-decoration: line-through;
}

.todo-empty {
  margin-top: 16px;
  padding: 14px 12px;
  border-radius: 16px;
  background: rgba(255, 251, 245, 0.66);
  color: #867d71;
  line-height: 1.7;
  font-size: 13px;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
  margin-top: 16px;
}

.stat-cell {
  padding: 16px;
  border-radius: 20px;
  background: rgba(253, 249, 242, 0.96);
  border: 1px solid rgba(201, 186, 164, 0.14);
}

.stat-label {
  display: block;
  color: #8c8375;
  font-size: 12px;
}

.stat-value {
  display: block;
  margin-top: 10px;
  color: #3a4534;
  font-size: 22px;
  font-weight: 800;
}

.stat-switch {
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}
</style>
