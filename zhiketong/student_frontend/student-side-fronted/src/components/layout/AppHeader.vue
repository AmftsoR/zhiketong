<template>
  <header class="app-header page-card">
    <div>
      <p class="app-header__label">学生端</p>
      <h1 class="app-header__title">{{ title }}</h1>
    </div>
  </header>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()
const title = computed(() => route.meta.title || '首页界面')
</script>

<style scoped>
.app-header {
  margin: 24px 24px 0;
}

.app-header__label {
  margin: 0 0 8px;
  color: var(--app-text-soft);
  font-size: 0.85rem;
}

.app-header__title {
  margin: 0;
  font-size: 1.5rem;
}
</style>