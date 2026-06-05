<template>
  <Teleport to="body">
    <div v-if="open" class="app-modal-mask" @click.self="$emit('close')">
      <section class="app-modal">
        <header class="app-modal__header">
          <slot name="header" />
        </header>

        <div class="app-modal__body">
          <slot />
        </div>
      </section>
    </div>
  </Teleport>
</template>

<script setup>
defineProps({
  open: {
    type: Boolean,
    default: false,
  },
})

defineEmits(['close'])
</script>

<style scoped>
.app-modal-mask {
  position: fixed;
  inset: 0;
  display: grid;
  place-items: center;
  padding: 24px;
  background: rgba(15, 23, 42, 0.45);
}

.app-modal {
  width: min(560px, 100%);
  border-radius: var(--app-radius-lg);
  background: var(--app-surface-solid);
  box-shadow: var(--app-shadow);
  overflow: hidden;
}

.app-modal__header,
.app-modal__body {
  padding: 20px 24px;
}
</style>