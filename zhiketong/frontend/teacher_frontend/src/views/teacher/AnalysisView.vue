<script setup>
import { computed, onMounted, ref } from 'vue'
import { fetchKnowledgeTree } from '../../api/knowledge'
import { fetchQuestions } from '../../api/questions'

const nodes = [
  { id: 1, name: '函数基本概念', level: 'good', x: 22, y: 45, type: '基础' },
  { id: 2, name: '定义域与值域', level: 'good', x: 10, y: 18, type: '必考核心' },
  { id: 3, name: '复合函数单调性', level: 'mid', x: 42, y: 46, type: '必考核心' },
  { id: 4, name: '含参分离判定', level: 'weak', x: 68, y: 30, type: '高频错点' },
  { id: 5, name: '绝对值不等式', level: 'weak', x: 50, y: 8, type: '高频错点' },
  { id: 6, name: '极值点求导', level: 'good', x: 77, y: 46, type: '基础' },
  { id: 7, name: '奇偶性判断', level: 'good', x: 58, y: 62, type: '基础' },
]

const selectedNodeId = ref(3)
const selectedExamScope = ref('近5次测验')

const riskStudents = ref([
  { name: '赵小刚', reason: '连续3次不及格', level: '高风险', handled: false },
  { name: '孙浩宇', reason: '作业完成率低', level: '关注', handled: false },
])

// ---- 后端知识点树 + 相关题目 ----
const kpTree = ref([])
const relatedQuestions = ref([])
const loadingQuestions = ref(false)
const showQuestions = ref(false)

async function loadKpTree() {
  try { const r = await fetchKnowledgeTree(); if (r?.code === 200) kpTree.value = r.data || [] } catch {}
}
async function loadRelatedQuestions(kpId) {
  loadingQuestions.value = true; showQuestions.value = true
  try { const r = await fetchQuestions({ knowledgePointId: kpId, page: 1, size: 10 }); relatedQuestions.value = r?.data?.records || [] }
  catch { relatedQuestions.value = [] } finally { loadingQuestions.value = false }
}
function toggleQuestions() {
  if (!showQuestions.value) { showQuestions.value = true; loadRelatedQuestions(selectedNode.value.id) }
  else showQuestions.value = false
}
function diffLabel(d) { return d === 'easy' ? '基础' : d === 'hard' ? '困难' : '中等' }
function typeLabel(t) { const m = { single_choice:'单选', multi_choice:'多选', true_false:'判断', fill_blank:'填空', short_answer:'简答' }; return m[t] || t || '-' }

onMounted(() => loadKpTree())

const selectedNode = computed(() => nodes.find((n) => n.id === selectedNodeId.value) || nodes[0])

const mastery = computed(() => {
  const base = {
    good: 78,
    mid: 45,
    weak: 23,
  }
  return base[selectedNode.value.level]
})

const wrongReason = computed(() => {
  if (selectedNode.value.id === 3) return '内层函数单调性忽视'
  if (selectedNode.value.id === 4) return '参数区间分类遗漏'
  if (selectedNode.value.id === 5) return '绝对值分段讨论错误'
  return '审题步骤遗漏'
})

function nodeClass(level) {
  if (level === 'good') return 'node-good'
  if (level === 'mid') return 'node-mid'
  return 'node-weak'
}

function trendPath(name) {
  if (name === 'avg') return '0,100 75,90 150,72 225,64 300,50'
  if (selectedExamScope.value === '近5次测验') return '0,120 75,130 150,96 225,82 300,70'
  return '0,128 60,132 120,125 180,98 240,88 300,78'
}

function markHandled(index) {
  riskStudents.value[index].handled = true
}
</script>

<template>
  <section class="page">
    <div class="title-row">
      <h1>班级学情洞察</h1>
    </div>

    <article class="card graph-card">
      <div class="card-head">
        <h3>班级图谱薄弱点热力图</h3>
        <span>红黄绿状态（量化评估）</span>
      </div>

      <div class="graph-area">
        <aside class="graph-sidebar">
          <div class="ring">
            <div class="ring-inner">
              <b>28</b>
              <small>/35 掌握知识点</small>
            </div>
          </div>

          <div class="node-info">
            <h4>{{ selectedNode.name }}</h4>
            <p>图谱类型：<em>{{ selectedNode.type }}</em></p>
            <p>掌握概率：<em>{{ mastery }}%</em></p>
            <p>高频错因：<em>{{ wrongReason }}</em></p>

            <div class="related-section">
              <div class="related-title" @click="toggleQuestions">
                <span>相关题目</span>
                <span class="related-arrow" :class="{open:showQuestions}">&#9662;</span>
              </div>
              <div v-if="showQuestions" class="related-list">
                <div v-if="loadingQuestions" class="q-loading">加载中...</div>
                <div v-else-if="!relatedQuestions.length" class="q-empty">暂无相关题目</div>
                <div v-for="q in relatedQuestions" :key="q.id" class="q-item">
                  <div class="q-item-head">
                    <span class="q-diff" :class="'q-diff-'+q.difficulty">{{ diffLabel(q.difficulty) }}</span>
                    <span class="q-type">{{ typeLabel(q.type) }}</span>
                  </div>
                  <p class="q-title">{{ q.title }}</p>
                </div>
              </div>
            </div>
          </div>
        </aside>

        <div class="graph-plot">
          <div
            v-for="item in nodes"
            :key="item.id"
            class="node"
            :class="[nodeClass(item.level), { active: selectedNodeId === item.id }]"
            :style="{ left: `${item.x}%`, top: `${item.y}%` }"
            @click="selectedNodeId = item.id"
          >
            <span class="dot"></span>
            <span class="label">{{ item.name }}</span>
          </div>

          <div class="legend">
            <span><i class="lg good"></i>掌握考点</span>
            <span><i class="lg mid"></i>一般弱项</span>
            <span><i class="lg weak"></i>严重薄弱区</span>
          </div>
        </div>
      </div>
    </article>

    <div class="grid-2">
      <article class="card trend-card">
        <div class="card-head">
          <h3>班级成绩趋势（测评闭环）</h3>
          <select v-model="selectedExamScope">
            <option>近5次测验</option>
            <option>近6次测验</option>
          </select>
        </div>

        <svg viewBox="0 0 320 170" preserveAspectRatio="none">
          <g class="grid-line">
            <line x1="0" y1="20" x2="320" y2="20" />
            <line x1="0" y1="60" x2="320" y2="60" />
            <line x1="0" y1="100" x2="320" y2="100" />
            <line x1="0" y1="140" x2="320" y2="140" />
          </g>
          <polyline class="line-blue" :points="trendPath('avg')" />
          <polyline class="line-green" :points="trendPath('cls')" />
        </svg>

        <p class="hint">趋势解读：近三次随堂测成绩持续高于年级均分，整体提分明显。</p>
      </article>

      <article class="card risk-card">
        <div class="card-head">
          <h3>重点关注名单</h3>
          <span>红线预警</span>
        </div>

        <ul class="risk-list">
          <li v-for="(item, index) in riskStudents" :key="item.name" :class="{ done: item.handled }">
            <div>
              <b>{{ item.name }}</b>
              <p>{{ item.reason }}</p>
            </div>
            <button :disabled="item.handled" @click="markHandled(index)">
              {{ item.handled ? '已处理' : item.level }}
            </button>
          </li>
        </ul>
      </article>
    </div>
  </section>
</template>

<style scoped>
.page {
  display: grid;
  gap: 16px;
}

.title-row h1 {
  color: #0f172a;
  font-size: 30px;
}

.card {
  background: #fff;
  border-radius: 14px;
  border: 1px solid #e2e8f0;
  box-shadow: 0 4px 16px rgba(15, 23, 42, 0.04);
  padding: 16px;
}

.card-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-head h3 {
  color: #111827;
  font-size: 16px;
  font-weight: 700;
}

.card-head span,
.card-head select {
  color: #94a3b8;
  font-size: 12px;
}

.card-head select {
  border: 1px solid #d7e0ec;
  border-radius: 8px;
  padding: 6px 8px;
  background: #fff;
}

.graph-area {
  margin-top: 12px;
  display: grid;
  grid-template-columns: 230px 1fr;
  gap: 12px;
}

.graph-sidebar {
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 10px;
  padding: 12px;
}

.ring {
  width: 96px;
  height: 96px;
  margin: 0 auto;
  border-radius: 50%;
  background: conic-gradient(#10b981 0deg 288deg, #e2e8f0 288deg 360deg);
  display: grid;
  place-items: center;
}

.ring-inner {
  width: 74px;
  height: 74px;
  border-radius: 50%;
  background: #f8fafc;
  display: grid;
  place-items: center;
  text-align: center;
}

.ring-inner b {
  color: #0f172a;
  font-size: 24px;
}

.ring-inner small {
  color: #64748b;
  font-size: 11px;
}

.node-info {
  margin-top: 12px;
  display: grid;
  gap: 8px;
}

.node-info h4 {
  color: #b45309;
  font-size: 15px;
}

.node-info p {
  color: #64748b;
  font-size: 12px;
}

.node-info em {
  color: #1e293b;
  font-style: normal;
  font-weight: 600;
}

/* 相关题目 */
.related-section { margin-top: 6px; border-top: 1px solid #e5e7eb; padding-top: 8px; }
.related-title { display: flex; justify-content: space-between; align-items: center; cursor: pointer; font-size: 12px; font-weight: 600; color: #6b7280; padding: 4px 0; user-select: none; }
.related-title:hover { color: #6366f1; }
.related-arrow { transition: transform .2s; }
.related-arrow.open { transform: rotate(180deg); }
.related-list { margin-top: 4px; max-height: 200px; overflow-y: auto; }
.q-loading, .q-empty { text-align: center; color: #9ca3af; font-size: 11px; padding: 12px 0; }
.q-item { padding: 6px 8px; border-radius: 6px; background: #fff; border: 1px solid #f3f4f6; margin-bottom: 4px; }
.q-item:hover { border-color: #c7d2fe; }
.q-item-head { display: flex; gap: 4px; margin-bottom: 2px; }
.q-diff { border-radius: 3px; padding: 1px 5px; font-size: 10px; font-weight: 700; }
.q-diff-easy { background: #dcfce7; color: #16a34a; }
.q-diff-medium { background: #fef3c7; color: #d97706; }
.q-diff-hard { background: #fee2e2; color: #dc2626; }
.q-type { font-size: 10px; color: #9ca3af; }
.q-title { margin: 0; font-size: 11px; color: #374151; line-height: 1.4; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; }

.graph-plot {
  position: relative;
  min-height: 320px;
  background: #fff;
  border: 1px solid #e2e8f0;
  border-radius: 10px;
  overflow: hidden;
}

.node {
  position: absolute;
  transform: translate(-50%, -50%);
  cursor: pointer;
  display: grid;
  justify-items: center;
  gap: 8px;
}

.node .dot {
  width: 22px;
  height: 22px;
  border-radius: 50%;
  box-shadow: 0 0 0 8px rgba(255, 255, 255, 0.92);
}

.node .label {
  background: rgba(255, 255, 255, 0.88);
  border-radius: 6px;
  padding: 2px 8px;
  font-size: 12px;
  color: #475569;
}

.node-good .dot {
  background: #10b981;
}

.node-mid .dot {
  background: #f59e0b;
}

.node-weak .dot {
  background: #ef4444;
}

.node.active .dot {
  transform: scale(1.15);
}

.node.active .label {
  color: #1e293b;
  font-weight: 700;
}

.legend {
  position: absolute;
  left: 50%;
  bottom: 12px;
  transform: translateX(-50%);
  background: #fff;
  box-shadow: 0 2px 8px rgba(15, 23, 42, 0.08);
  border-radius: 999px;
  padding: 6px 14px;
  display: flex;
  gap: 16px;
  font-size: 12px;
  color: #64748b;
}

.legend .lg {
  width: 8px;
  height: 8px;
  display: inline-block;
  border-radius: 50%;
  margin-right: 5px;
}

.legend .good {
  background: #10b981;
}

.legend .mid {
  background: #f59e0b;
}

.legend .weak {
  background: #ef4444;
}

.grid-2 {
  display: grid;
  grid-template-columns: 1.2fr 1fr;
  gap: 12px;
}

.trend-card svg {
  margin-top: 12px;
  width: 100%;
  height: 170px;
}

.grid-line line {
  stroke: #eef2f7;
  stroke-width: 1;
}

.line-blue,
.line-green {
  fill: none;
  stroke-width: 3;
  stroke-linecap: round;
  stroke-linejoin: round;
}

.line-blue {
  stroke: #3b82f6;
}

.line-green {
  stroke: #10b981;
  stroke-dasharray: 6 4;
}

.hint {
  margin-top: 8px;
  background: #f0f6ff;
  border-radius: 8px;
  color: #1e40af;
  font-size: 12px;
  padding: 9px;
}

.risk-list {
  margin-top: 12px;
  list-style: none;
  display: grid;
  gap: 10px;
}

.risk-list li {
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-radius: 10px;
  background: #f8fafc;
  padding: 10px;
}

.risk-list li.done {
  opacity: 0.65;
}

.risk-list b {
  color: #0f172a;
}

.risk-list p {
  color: #64748b;
  font-size: 12px;
}

.risk-list button {
  border: 0;
  border-radius: 999px;
  background: #fee2e2;
  color: #dc2626;
  font-size: 12px;
  font-weight: 700;
  padding: 6px 10px;
}

.risk-list button:disabled {
  background: #dcfce7;
  color: #15803d;
}

@media (max-width: 1100px) {
  .graph-area {
    grid-template-columns: 1fr;
  }

  .grid-2 {
    grid-template-columns: 1fr;
  }
}
</style>
