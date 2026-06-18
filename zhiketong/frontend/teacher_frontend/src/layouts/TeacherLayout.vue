<script setup>
import { computed, ref } from 'vue'
import { RouterLink, RouterView, useRoute } from 'vue-router'
import { useUserStore } from '../stores/userStore'

const route = useRoute()
const userStore = useUserStore()

const classOptions = ['高一（1）班', '高一（2）班', '高二（3）班']
const currentClass = ref(classOptions[0])
const headerSearch = ref('')

const teacherName = computed(() => userStore.user?.realName || '教师')
const teacherInitial = computed(() => teacherName.value.slice(0, 1))

const navItems = [
  { label: '数据总览', path: '/overview', icon: '总' },
  { label: '班级管理', path: '/class', icon: '班' },
  { label: '学情分析', path: '/analysis', icon: '析' },
  { label: '题库管理', path: '/bank', icon: '题' },
  { label: '作业管理', path: '/homework', icon: '业' },
  { label: '设置中心', path: '/settings', icon: '设' },
]

const pageTitle = computed(() => route.meta.title || '教师端')
const showHeaderSearch = computed(() => route.meta.showHeaderSearch === true)
</script>

<template>
  <div class="teacher-shell">
    <aside class="sidebar">
      <div class="brand">
        <div class="brand-mark">智</div>
        <div class="brand-title">智课通教师端</div>
      </div>

      <nav class="menu">
        <RouterLink
          v-for="item in navItems"
          :key="item.path"
          :to="item.path"
          class="menu-item"
          active-class="menu-item-active"
        >
          <span class="menu-icon">{{ item.icon }}</span>
          <span>{{ item.label }}</span>
        </RouterLink>
      </nav>
    </aside>

    <div class="main">
      <header class="topbar">
        <div class="topbar-left">
          <select v-model="currentClass" class="class-select">
            <option v-for="cls in classOptions" :key="cls" :value="cls">{{ cls }}</option>
          </select>

          <div v-if="showHeaderSearch" class="search-box">
            <input v-model="headerSearch" placeholder="搜索学生姓名" />
          </div>

          <div v-else class="page-chip">{{ currentClass }} · {{ pageTitle }}</div>
        </div>

        <div class="profile">
          <div class="avatar">{{ teacherInitial }}</div>
          <span>{{ teacherName }}</span>
        </div>
      </header>

      <main class="page-content">
        <RouterView />
      </main>
    </div>
  </div>
</template>

<style scoped>
.teacher-shell {
  display: grid;
  grid-template-columns: 230px 1fr;
  height: 100vh;
  background: radial-gradient(circle at 30% 0%, #ffffff 0%, #f6f8fd 45%, #edf2f8 100%);
}

.sidebar {
  background: linear-gradient(180deg, #243b78 0%, #1e2f60 100%);
  color: #fff;
  border-right: 1px solid rgba(255, 255, 255, 0.08);
  position: sticky;
  top: 0;
  height: 100vh;
  overflow: auto;
}

.brand {
  height: 70px;
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 0 20px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
}

.brand-mark {
  width: 36px;
  height: 36px;
  display: grid;
  place-items: center;
  border-radius: 10px;
  background: linear-gradient(135deg, #60a5fa 0%, #2563eb 100%);
  font-weight: 700;
}

.brand-title {
  font-size: 20px;
  font-weight: 700;
}

.menu {
  display: grid;
  gap: 2px;
  padding: 10px 0;
}

.menu-item {
  display: flex;
  align-items: center;
  gap: 12px;
  height: 52px;
  padding: 0 20px;
  color: rgba(255, 255, 255, 0.75);
  text-decoration: none;
  border-left: 4px solid transparent;
  transition: background-color 0.2s ease;
}

.menu-item:hover {
  background: rgba(255, 255, 255, 0.06);
}

.menu-item-active {
  background: #1d2f61;
  color: #fff;
  border-left-color: #60a5fa;
  font-weight: 700;
}

.menu-icon {
  width: 22px;
  height: 22px;
  display: grid;
  place-items: center;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.08);
  font-size: 12px;
}

.main {
  display: flex;
  flex-direction: column;
  min-width: 0;
  height: 100vh;
  overflow: auto;
}

.topbar {
  height: 70px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #fff;
  border-bottom: 1px solid #e2e8f0;
  padding: 0 24px;
}

.topbar-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.class-select {
  border: 1px solid #d9e0eb;
  background: #f1f5f9;
  border-radius: 10px;
  padding: 8px 12px;
  color: #0f172a;
  font-weight: 600;
}

.search-box input {
  width: 260px;
  border: 1px solid #e2e8f0;
  background: #f8fafc;
  border-radius: 10px;
  padding: 10px 12px;
}

.page-chip {
  background: #f1f5f9;
  border-radius: 10px;
  padding: 10px 14px;
  font-weight: 600;
  color: #0f172a;
}

.profile {
  display: flex;
  align-items: center;
  gap: 10px;
  color: #1f2937;
}

.avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  display: grid;
  place-items: center;
  background: #e2e8f0;
  font-weight: 700;
}

.page-content {
  padding: 24px;
}

@media (max-width: 980px) {
  .teacher-shell {
    grid-template-columns: 1fr;
  }

  .sidebar {
    position: sticky;
    top: 0;
    z-index: 10;
  }

  .menu {
    grid-template-columns: repeat(5, minmax(0, 1fr));
    padding: 0;
  }

  .menu-item {
    border-left: 0;
    border-bottom: 3px solid transparent;
    justify-content: center;
    padding: 0 6px;
    font-size: 12px;
  }

  .menu-item-active {
    border-left: 0;
    border-bottom-color: #60a5fa;
  }

  .brand {
    display: none;
  }

  .topbar {
    height: auto;
    flex-direction: column;
    align-items: stretch;
    gap: 10px;
    padding: 12px;
  }

  .topbar-left {
    flex-direction: column;
    align-items: stretch;
  }

  .search-box input {
    width: 100%;
  }

  .profile {
    justify-content: flex-end;
  }
}
</style>
