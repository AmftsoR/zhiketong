<template>
  <button class="app-button" :class="`app-button--${variant}`">
    <slot />
  </button>
</template>

<script setup>
defineProps({
  variant: {
    type: String,
    default: 'primary',
  },
})
</script>

<style scoped>
.app-button {
  border: 0;
  border-radius: 999px;
  padding: 0.8rem 1.2rem;
  cursor: pointer;
  transition: transform 0.2s ease, box-shadow 0.2s ease, opacity 0.2s ease;
}

.app-button:hover {
  transform: translateY(-1px);
}

.app-button--primary {
  color: #fff;
  background: linear-gradient(135deg, var(--app-primary), var(--app-primary-strong));
  box-shadow: 0 14px 28px rgba(37, 99, 235, 0.28);
}

.app-button--ghost {
  color: var(--app-text);
  background: var(--app-primary-soft);
}
</style>