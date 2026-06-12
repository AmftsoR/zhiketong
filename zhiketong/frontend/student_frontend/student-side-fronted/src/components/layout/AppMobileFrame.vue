<template>
  <section class="mobile-screen">
    <div class="mobile-screen__frame" :class="[`mobile-screen__frame--${theme}`, frameClass]">
      <header class="mobile-screen__status" :class="`mobile-screen__status--${theme}`">
        <span class="mobile-screen__time">{{ time }}</span>

        <div class="mobile-screen__indicators" aria-hidden="true">
          <!-- 信号：4条阶梯竖条 -->
          <svg class="status-icon" viewBox="0 0 16 12" width="16" height="12">
            <rect x="0"  y="8"  width="3" height="4"  rx="0.75" fill="#333"/>
            <rect x="4"  y="5"  width="3" height="7"  rx="0.75" fill="#333"/>
            <rect x="8"  y="3"  width="3" height="9"  rx="0.75" fill="#333"/>
            <rect x="12" y="0"  width="3" height="12" rx="0.75" fill="#333"/>
          </svg>

          <!-- WiFi：底部圆点 + 三层对称弧形（以圆点为中心的上半圆弧） -->
          <svg class="status-icon" viewBox="0 0 18 16" width="18" height="16">
            <circle cx="9" cy="11.5" r="2" fill="#333"/>
            <path d="M 7 11.5 A 2 3 0 0 1 11 11.5"  stroke="#333" stroke-width="2.2" stroke-linecap="round" fill="none"/>
            <path d="M 5 11.5 A 4 6 0 0 1 13 11.5"  stroke="#333" stroke-width="2.2" stroke-linecap="round" fill="none"/>
            <path d="M 3 11.5 A 6 9 0 0 1 15 11.5"  stroke="#333" stroke-width="2.2" stroke-linecap="round" fill="none"/>
          </svg>

          <!-- 电池：空心外框 + 正极触点 + 内部填充 -->
          <svg class="status-icon" viewBox="0 0 25 12" width="25" height="12">
            <rect x="0" y="0.5" width="21" height="11" rx="2.5" fill="none" stroke="#333" stroke-width="1.2"/>
            <rect x="2" y="2.5" width="17" height="7" rx="1" fill="#333"/>
            <rect x="21.5" y="3.5" width="2.5" height="5" rx="1.25" fill="#333"/>
          </svg>
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
  gap: 8px;
  align-items: center;
}

.status-icon {
  display: block;
  flex-shrink: 0;
}

.mobile-screen__time {
  position: relative;
  z-index: 1;
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