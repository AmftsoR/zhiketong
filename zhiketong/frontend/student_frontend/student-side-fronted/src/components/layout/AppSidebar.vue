<template>
  <aside class="app-sidebar">
    <div class="app-sidebar__brand page-card">
      <p class="app-sidebar__subtitle">ZhikeTong</p>
      <h2 class="app-sidebar__title">学生端框架</h2>
    </div>

    <nav class="app-sidebar__nav page-card">
      <router-link
        v-for="item in navItems"
        :key="item.path"
        :to="item.path"
        class="app-sidebar__link"
        active-class="app-sidebar__link--active"
      >
        {{ item.label }}
      </router-link>
    </nav>
  </aside>
</template>

<script setup>
const navItems = [
  { label: '首页界面', path: '/home' },
  { label: '学习界面-薄弱点图谱', path: '/weakness-map' },
  { label: '靶向练习推送', path: '/target-practice' },
  { label: '个人错题本', path: '/wrong-book' },
  { label: '我的作业', path: '/my-homework' },
  { label: '学习分析报告', path: '/analysis-report' },
  { label: 'AI启发式答题助手', path: '/ai-assistant' },
  { label: '我的界面', path: '/profile' },
]
</script>

<style scoped>
.app-sidebar {
  padding: 24px;
  display: grid;
  gap: 16px;
}

.app-sidebar__brand {
  background: linear-gradient(180deg, rgba(37, 99, 235, 0.16), rgba(255, 255, 255, 0.9));
}

.app-sidebar__subtitle {
  margin: 0 0 4px;
  color: var(--app-text-soft);
  font-size: 0.82rem;
}

.app-sidebar__title {
  margin: 0;
  font-size: 1.2rem;
}

.app-sidebar__nav {
  display: grid;
  gap: 8px;
}

.app-sidebar__link {
  padding: 12px 14px;
  border-radius: var(--app-radius-sm);
  color: var(--app-text-soft);
  transition: background-color 0.2s ease, color 0.2s ease, transform 0.2s ease;
}

.app-sidebar__link:hover,
.app-sidebar__link--active {
  color: var(--app-text);
  background: var(--app-primary-soft);
  transform: translateX(2px);
}

@media (max-width: 960px) {
  .app-sidebar {
    padding-bottom: 0;
  }
}
</style>