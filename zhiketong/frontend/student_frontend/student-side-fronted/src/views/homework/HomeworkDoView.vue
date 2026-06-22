<template>
  <AppMobileFrame :time="currentTime" frame-class="hw-frame" scroll-class="hw-scroll">
    <template #topbar>
      <div class="hw-topbar">
        <button class="icon-button" @click="goBack"><span class="icon-button__arrow"></span></button>
        <div class="hw-topbar__center">
          <h1 class="hw-topbar__title">{{ homework.title }}</h1>
          <span class="hw-topbar__sub">{{ homework.className }}</span>
        </div>
        <div class="hw-topbar__spacer"></div>
      </div>
    </template>

    <section class="progress-card page-card">
      <div class="progress-card__head">
        <p class="progress-card__title">{{ currentIdx + 1 }} / {{ questions.length }} 题</p>
        <p class="progress-card__sub">{{ homework.difficulty === 'easy' ? '基础' : homework.difficulty === 'hard' ? '困难' : '中等' }}</p>
      </div>
      <div class="progress-bar"><div class="progress-bar__fill" :style="{ width: ((currentIdx + 1) / questions.length * 100) + '%' }"></div></div>
    </section>

    <section class="q-card page-card" v-if="currentQ">
      <div class="q-card__head">
        <span class="q-badge">{{ typeLabel(currentQ.type) }}</span>
        <span class="q-badge" :class="'q-badge--' + (currentQ.difficulty || 'medium')">{{ diffLabel(currentQ.difficulty) }}</span>
      </div>
      <div class="q-stem" v-html="renderMd(currentQ.title || '')"></div>
      <div class="q-options" v-if="parsedOptions.length">
        <button v-for="opt in parsedOptions" :key="opt.key" class="q-opt" :class="{ 'q-opt--sel': selected === opt.key, 'q-opt--right': showResult && opt.key === correctKey, 'q-opt--wrong': showResult && selected === opt.key && opt.key !== correctKey }" :disabled="showResult" @click="selected = opt.key">{{ opt.key }}. {{ opt.text }}</button>
      </div>
      <div class="q-fill" v-else>
        <input v-model="fillAnswer" class="q-fill-input" placeholder="输入你的答案" :disabled="showResult" />
      </div>
      <div v-if="showResult && isCorrect" class="result result--ok">✓ 回答正确！</div>
      <div v-if="showResult && !isCorrect" class="result result--err">
        <span>✗ 正确答案：{{ correctKey }}</span>
        <div class="analysis" v-if="currentQ.explanation">{{ currentQ.explanation }}</div>
      </div>
    </section>

    <template #footer>
      <footer class="hw-footer">
        <button v-if="!showResult" class="hw-btn" @click="checkAnswer" :disabled="!answer">提交答案</button>
        <button v-else class="hw-btn" @click="nextQuestion">{{ currentIdx < questions.length - 1 ? '下一题' : '完成作业' }}</button>
        <p class="hw-footer__hint">{{ currentIdx + 1 }} / {{ questions.length }}</p>
      </footer>
    </template>
  </AppMobileFrame>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import AppMobileFrame from '../../components/layout/AppMobileFrame.vue'
import { request } from '../../utils/request'
import { useUserStore } from '../../stores/userStore'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const currentTime = ref('9:41')
const homework = ref({ title: '', className: '', difficulty: 'medium', questions: [] })
const questions = computed(() => homework.value.questions || [])
const currentIdx = ref(0)
const selected = ref('')
const fillAnswer = ref('')
const showResult = ref(false)
const submitted = ref(false)

const currentQ = computed(() => questions.value[currentIdx.value] || null)
const parsedOptions = computed(() => {
  if (!currentQ.value) return []
  try { const opts = typeof currentQ.value.options === 'string' ? JSON.parse(currentQ.value.options) : (currentQ.value.options || []); return Array.isArray(opts) ? opts.map(function(o, i) { const key = typeof o === 'string' ? o.charAt(0) : (o.key || String.fromCharCode(65 + i)); const text = typeof o === 'string' ? o.substring(2).trim() : (o.text || o.value || ''); return { key: key, text: text || o } }) : [] } catch { return [] }
})
const isCorrect = computed(() => {
  if (!currentQ.value) return false
  try { const a = typeof currentQ.value.answer === 'string' ? JSON.parse(currentQ.value.answer) : currentQ.value.answer; const correct = Array.isArray(a) ? a[0] : String(a); return (selected.value || fillAnswer.value).trim().toUpperCase() === correct.trim().toUpperCase() } catch { return (selected.value || fillAnswer.value).trim().toUpperCase() === String(currentQ.value.answer || '').trim().toUpperCase() }
})
const correctKey = computed(() => {
  if (!currentQ.value) return ''
  try { const a = typeof currentQ.value.answer === 'string' ? JSON.parse(currentQ.value.answer) : currentQ.value.answer; return Array.isArray(a) ? a[0] : String(a || '') } catch { return String(currentQ.value.answer || '') }
})
const answer = computed(() => selected.value || fillAnswer.value)

function diffLabel(d) { return d === 'easy' ? '基础' : d === 'hard' ? '困难' : '中等' }
function typeLabel(t) { if (!t) return '-'; return t === 'single_choice' || t === 'single' ? '单选' : t === 'multi_choice' || t === 'multi' ? '多选' : t === 'true_false' ? '判断' : t === 'fill_blank' ? '填空' : t === 'short_answer' ? '简答' : t }
function renderMd(t) { if (!t) return ''; return t.replace(/!\[([^\]]*)\]\(([^)]+)\)/g, '<img src="$2" style="max-width:100%;border-radius:8px">').replace(/\n/g, '<br>') }
function goBack() { router.push('/home') }
function checkAnswer() { showResult.value = true }
function nextQuestion() {
  if (currentIdx.value < questions.value.length - 1) { currentIdx.value++; selected.value = ''; fillAnswer.value = ''; showResult.value = false }
  else { submitAll() }
}
async function submitAll() {
  if (submitted.value) { router.push('/home'); return }
  submitted.value = true
  try { await request('/homeworks/submit', { method: 'POST', body: { homeworkId: homework.value.id, studentId: userStore.user?.id || 1, answers: 'completed' } }) }
  catch {} finally { setTimeout(function() { router.push('/home') }, 500) }
}

let clockTimer = null
function updateClock() { currentTime.value = new Date().toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' }) }

onMounted(async function() {
  updateClock(); clockTimer = setInterval(updateClock, 30000)
  try {
    const hwId = route.query.id
    if (hwId) { const r = await request('/homeworks/' + hwId); if (r && r.code === 200 && r.data) homework.value = r.data }
  } catch (e) { console.error(e) }
})
onBeforeUnmount(function() { if (clockTimer) clearInterval(clockTimer) })
</script>

<style scoped>
.hw-topbar{height:44.67px;padding:0 20px;display:grid;grid-template-columns:40px 1fr 40px;align-items:center}
.icon-button{width:28px;height:28px;border:0;background:transparent;display:grid;place-items:center;cursor:pointer}
.icon-button__arrow{width:9px;height:15px;border-left:2px solid #333;border-bottom:2px solid #333;transform:rotate(45deg);margin-left:6px}
.hw-topbar__center{display:flex;flex-direction:column;align-items:center;gap:2px}
.hw-topbar__title{margin:0;font-size:.9375rem;font-weight:600;text-align:center}
.hw-topbar__sub{font-size:.6875rem;color:#6c5ce7;background:#f0efff;padding:1px 10px;border-radius:999px;font-weight:600}
.hw-topbar__spacer{width:40px}
.hw-scroll{overflow:auto;padding:0 0 16px}
.hw-scroll::-webkit-scrollbar{width:0;height:0}
.progress-card,.q-card{width:calc(100% - 40px);margin:0 20px 12px}
.progress-card{padding-bottom:20px}
.progress-card__title{margin:0;color:#000;font-size:.875rem;font-weight:700}
.progress-card__sub{margin:4px 0 0;color:#999;font-size:.75rem}
.progress-bar{height:6px;margin-top:10px;border-radius:999px;background:#eee;overflow:hidden}
.progress-bar__fill{height:100%;border-radius:inherit;background:#6c5ce7;transition:width .3s}
.q-card{padding-bottom:20px}
.q-card__head{display:flex;align-items:center;gap:8px}
.q-badge{padding:2px 8px;border-radius:4px;font-size:.625rem;font-weight:700;background:#f0efff;color:#6c5ce7}
.q-badge--easy{background:#dcfce7;color:#16a34a}
.q-badge--medium{background:#fef3c7;color:#d97706}
.q-badge--hard{background:#fee2e2;color:#dc2626}
.q-stem{margin-top:14px;color:#333;font-size:1rem;line-height:1.8}
.q-stem :deep(img){max-width:100%;border-radius:8px}
.q-options{display:grid;gap:14px;margin-top:14px}
.q-opt{width:100%;border:1px solid #eee;border-radius:8px;background:#fafafa;padding:12px 16px;text-align:left;cursor:pointer;color:#333;font-size:.9375rem;transition:transform .15s}
.q-opt:hover{transform:translateY(-1px)}
.q-opt--sel{border-color:#6c5ce7;background:#f0efff}
.q-opt--right{border-color:#00b894;background:#f0fff4}
.q-opt--wrong{border-color:#ff7675;background:#fff5f5}
.q-opt:disabled{cursor:default;pointer-events:none}
.q-fill{margin-top:14px}
.q-fill-input{width:100%;height:48px;border:1px solid #e0e0e0;border-radius:8px;padding:0 12px;font-size:.9375rem;box-sizing:border-box}
.result{margin-top:16px;padding:12px 16px;border-radius:8px;font-size:.875rem;font-weight:600}
.result--ok{background:#f0fff4;color:#00b894}
.result--err{background:#fff5f5;color:#ff7675}
.analysis{margin-top:8px;padding:10px;background:#f8f9fa;border-radius:6px;color:#666;font-size:.8125rem;line-height:1.5;font-weight:400}
.hw-footer{padding:0 20px 14px}
.hw-btn{width:100%;height:51px;border:0;border-radius:25px;background:#6c5ce7;color:#fff;font-size:1rem;font-weight:700;cursor:pointer;box-shadow:0 8px 20px rgba(108,92,231,.3)}
.hw-btn:disabled{opacity:.5}
.hw-footer__hint{margin:12px 0 0;text-align:center;color:#999;font-size:.75rem}
@media(max-width:420px){.progress-card,.q-card{width:calc(100% - 24px);margin-left:12px;margin-right:12px}}
</style>
