<template>
  <div class="app-shell">
    <router-view />
  </div>
</template>

<style scoped>
.app-shell {
  min-height: 100vh;
  padding: 16px;
  display: flex;
  justify-content: center;
  align-items: flex-start;
}

@media (max-width: 420px) {
  .app-shell {
    padding: 0;
  }
}
</style>