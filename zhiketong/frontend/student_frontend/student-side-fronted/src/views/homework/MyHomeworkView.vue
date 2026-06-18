<template>
  <AppMobileFrame :time="currentTime" frame-class="hw-frame" scroll-class="hw-scroll">
    <template #topbar>
      <div class="hw-topbar">
        <button class="icon-button" @click="goBack"><span class="icon-button__arrow"></span></button>
        <h1 class="hw-topbar__title">我的作业</h1><div style="width:28px"></div>
      </div>
    </template>

    <div v-if="loading" class="empty-state">加载中...</div>
    <section v-else-if="homeworks.length===0" class="empty-state">
      <p style="font-size:1rem;margin:0">暂无作业</p>
      <small style="color:#bbb">教师发布作业后显示在这里</small>
    </section>

    <section v-for="h in homeworks" :key="h.id" class="hw-card page-card">
      <div class="hw-card__head">
        <span class="hw-diff" :class="'hw-diff--'+h.difficulty">{{ diffLabel(h.difficulty) }}</span>
        <span class="hw-card__title">{{ h.title }}</span>
      </div>
      <div class="hw-card__meta">
        <span>班级: {{ h.className || '-' }}</span>
        <span v-if="h.deadline">截止: {{ h.deadline }}</span>
      </div>
      <p v-if="h.description" class="hw-card__desc">{{ h.description }}</p>
      <div class="hw-card__actions">
        <button class="hw-btn" @click="openDetail(h)">查看详情</button>
      </div>
    </section>

    <template #footer><BottomNav activeTab="home" /></template>
  </AppMobileFrame>

  <!-- 详情弹窗 -->
  <Teleport to="body">
    <div v-if="showDetail && detail" class="modal-mask" @click.self="showDetail=false">
      <div class="detail-panel">
        <div class="detail-head"><h3>{{ detail.title }}</h3><button @click="showDetail=false">&times;</button></div>
        <div class="detail-info">
          <p><span>难度:</span> {{ diffLabel(detail.difficulty) }}</p>
          <p><span>班级:</span> {{ detail.className }}</p>
          <p v-if="detail.deadline"><span>截止:</span> {{ detail.deadline }}</p>
          <p v-if="detail.description"><span>描述:</span> {{ detail.description }}</p>
        </div>
        <button class="btn-close-detail" @click="showDetail=false">关闭</button>
      </div>
    </div>
  </Teleport>
</template>

<script setup>
import { onMounted, ref, onBeforeUnmount } from 'vue'
import { useRouter } from 'vue-router'
import AppMobileFrame from '../../components/layout/AppMobileFrame.vue'
import BottomNav from '../../components/layout/BottomNav.vue'
import { fetchMyHomework } from '../../api/homework'
import { useUserStore } from '../../stores/userStore'

const router = useRouter(); const userStore = useUserStore()
const currentTime = ref('9:41')
const loading = ref(false); const homeworks = ref([])
const showDetail = ref(false); const detail = ref(null)
let clockTimer = null

function diffLabel(d) { return d === 'easy' ? '基础' : d === 'hard' ? '困难' : '中等' }

async function load() {
  loading.value = true
  try {
    const className = userStore.user?.className || '高三(1)班'
    const r = await fetchMyHomework(className)
    homeworks.value = r?.data?.records || []
  } catch (e) { console.error(e) } finally { loading.value = false }
}

function openDetail(h) { detail.value = h; showDetail.value = true }
function goBack() { router.push('/home') }
function updateClock() { currentTime.value = new Date().toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' }) }

onMounted(() => { updateClock(); clockTimer = setInterval(updateClock, 30000); load() })
onBeforeUnmount(() => { if (clockTimer) clearInterval(clockTimer) })
</script>

<style scoped>
.hw-topbar{height:44.67px;padding:0 20px;display:grid;grid-template-columns:40px 1fr 40px;align-items:center}
.icon-button{width:28px;height:28px;border:0;background:transparent;cursor:pointer;display:grid;place-items:center}
.icon-button__arrow{width:9px;height:15px;border-left:2px solid #333;border-bottom:2px solid #333;transform:rotate(45deg);margin-left:6px}
.hw-topbar__title{margin:0;font-size:1.0625rem;font-weight:600;text-align:center}
.hw-scroll{overflow:auto;padding:0 0 16px}
.hw-scroll::-webkit-scrollbar{width:0;height:0}
.empty-state{text-align:center;color:#9ca3af;padding:60px 20px}
.hw-card{width:calc(100% - 40px);margin:0 20px 12px;padding-bottom:18px}
.hw-card__head{display:flex;align-items:center;gap:10px}
.hw-diff{border-radius:4px;padding:3px 8px;font-size:.625rem;font-weight:700}
.hw-diff--easy{background:#dcfce7;color:#16a34a}
.hw-diff--medium{background:#fef3c7;color:#d97706}
.hw-diff--hard{background:#fee2e2;color:#dc2626}
.hw-card__title{font-size:.9rem;font-weight:600;color:#333;flex:1}
.hw-card__meta{display:flex;gap:16px;margin-top:10px;color:#999;font-size:.75rem}
.hw-card__desc{margin-top:8px;color:#666;font-size:.8125rem}
.hw-card__actions{margin-top:12px}
.hw-btn{border:0;border-radius:10px;padding:8px 20px;background:linear-gradient(135deg,#6c5ce7,#a29bfe);color:#fff;font-size:.8125rem;font-weight:600;cursor:pointer}
/* 详情弹窗 */
.modal-mask{position:fixed;inset:0;background:rgba(0,0,0,.45);z-index:9999;display:grid;place-items:center;padding:20px}
.detail-panel{width:100%;max-width:380px;background:#fff;border-radius:16px;padding:22px 20px}
.detail-head{display:flex;justify-content:space-between;align-items:center}
.detail-head h3{margin:0;font-size:1rem}
.detail-head button{width:30px;height:30px;border:0;background:#f5f5f5;border-radius:50%;font-size:1.2rem;cursor:pointer}
.detail-info{margin:16px 0;display:grid;gap:8px;font-size:.875rem}
.detail-info p{margin:0}
.detail-info span{color:#999;margin-right:6px}
.btn-close-detail{width:100%;border:0;border-radius:10px;padding:10px;background:#f3f4f6;color:#6b7280;font-size:.875rem;cursor:pointer}
</style>
