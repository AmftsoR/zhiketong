<template>
  <section class="mobile-screen">
    <div class="mobile-screen__frame" :class="[`mobile-screen__frame--${theme}`, frameClass]">
      <header class="mobile-screen__status" :class="`mobile-screen__status--${theme}`">
        <span class="mobile-screen__time">{{ time }}</span>

        <div class="mobile-screen__indicators" aria-hidden="true">
          <span class="status-icon status-icon--signal"></span>
          <span class="status-icon status-icon--wifi"></span>
          <span class="status-icon status-icon--battery"></span>
        </div>
      </header>

      <slot name="banner" />
      <slot name="topbar" />

      <div class="mobile-screen__scroll" :class="scrollClass">
        <slot />
      </div>

      <slot name="floating" />
      <slot name="footer" />
    </div>
  </section>
</template>

<script setup>
defineProps({
  time: {
    type: String,
    default: '9:41',
  },
  theme: {
    type: String,
    default: 'light',
  },
  frameClass: {
    type: [String, Array, Object],
    default: '',
  },
  scrollClass: {
    type: [String, Array, Object],
    default: '',
  },
})
</script>

<style scoped>
.mobile-screen {
  width: 100%;
}

.mobile-screen__frame {
  max-width: 375px;
  margin: 0 auto;
  height: 812px;
  max-height: calc(100dvh - 48px);
  background: #f5f7fa;
  border-radius: 40px;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.1);
  overflow: hidden;
  border: 10px solid #fff;
  position: relative;
  display: flex;
  flex-direction: column;
}

.mobile-screen__frame--dark .mobile-screen__status,
.mobile-screen__frame--dark .mobile-screen__time,
.mobile-screen__frame--dark .status-icon {
  color: #fff;
  background: #fff;
}

.mobile-screen__status {
  height: 44px;
  padding: 0 20px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  color: #333;
  font-size: 0.875rem;
  font-weight: 600;
  flex: 0 0 auto;
}

.mobile-screen__status--dark {
  background: #6c5ce7;
}

.mobile-screen__status--light {
  background: #f5f7fa;
}

.mobile-screen__indicators {
  display: inline-flex;
  gap: 7px;
  align-items: center;
}

.mobile-screen__time {
  position: relative;
  z-index: 1;
}

.status-icon {
  display: inline-block;
  background: #333;
  border-radius: 999px;
}

.status-icon--signal {
  width: 14px;
  height: 14px;
}

.status-icon--wifi {
  width: 16px;
  height: 12px;
}

.status-icon--battery {
  width: 18px;
  height: 10px;
}

.mobile-screen__scroll {
  flex: 1 1 0;
  min-height: 0;
  overflow: auto;
  padding: 0 0 12px;
}

.mobile-screen__scroll::-webkit-scrollbar {
  width: 0;
  height: 0;
}

@media (max-width: 420px) {
  .mobile-screen__frame {
    max-width: 100%;
    height: auto;
    min-height: 100dvh;
    border-radius: 0;
    border-width: 0;
  }
}
</style>