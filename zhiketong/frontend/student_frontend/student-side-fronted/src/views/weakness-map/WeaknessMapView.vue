<template>
  <AppMobileFrame :time="currentTime" frame-class="weakness-frame" scroll-class="weakness-scroll">
    <template #topbar>
      <div class="weakness-topbar">
        <button type="button" class="icon-button" @click="goBack" aria-label="返回">
          <span class="icon-button__arrow"></span>
        </button>
        <div class="weakness-topbar__titles">
          <p>学习界面</p>
          <h1>薄弱点图谱</h1>
        </div>
        <button type="button" class="weakness-topbar__action" @click="goPractice">去练习</button>
      </div>
    </template>

    <section class="subject-strip page-card">
      <button
        v-for="subject in subjects"
        :key="subject.key"
        type="button"
        class="subject-strip__item"
        :class="{ 'subject-strip__item--active': subject.key === selectedSubjectKey }"
        @click="selectSubject(subject.key)"
      >
        {{ subject.label }}
      </button>
    </section>

    <section class="mode-strip page-card">
      <button
        v-for="tab in tabs"
        :key="tab.key"
        type="button"
        class="mode-strip__item"
        :class="{ 'mode-strip__item--active': tab.key === activeTab }"
        @click="activeTab = tab.key"
      >
        {{ tab.label }}
      </button>
    </section>

    <section class="radar-card page-card">
      <div class="radar-card__head">
        <div>
          <p class="radar-card__eyebrow">{{ selectedSubject.label }}</p>
          <h2>{{ selectedSubject.chart.title }}</h2>
        </div>
        <span class="radar-card__tag">{{ selectedSubject.chart.subtitle }}</span>
      </div>

      <div class="radar-card__chart-wrap">
        <svg class="radar-card__chart" viewBox="0 0 320 320" role="img" :aria-label="selectedSubject.chart.title">
          <defs>
            <linearGradient :id="chartGradientId" x1="0%" y1="0%" x2="100%" y2="100%">
              <stop offset="0%" stop-color="#7C6CFF" stop-opacity="0.28" />
              <stop offset="100%" stop-color="#00B894" stop-opacity="0.2" />
            </linearGradient>
          </defs>

          <circle v-for="ring in radarRings" :key="ring" cx="160" cy="160" :r="ring" class="radar-ring" />

          <line
            v-for="axis in radarAxes"
            :key="axis.label"
            x1="160"
            y1="160"
            :x2="axis.outer.x"
            :y2="axis.outer.y"
            class="radar-axis"
          />

          <polygon :points="classPolygonPoints" class="radar-polygon radar-polygon--class" />
          <polygon :points="myPolygonPoints" class="radar-polygon radar-polygon--mine" />

          <g v-for="(axis, index) in radarAxes" :key="axis.label" class="radar-point-group" @click="selectAxis(index)">
            <circle :cx="axis.mine.x" :cy="axis.mine.y" r="5" class="radar-point radar-point--mine" />
            <circle :cx="axis.classPoint.x" :cy="axis.classPoint.y" r="4" class="radar-point radar-point--class" />
          </g>
        </svg>

        <div class="radar-labels">
          <button
            v-for="(axis, index) in radarAxes"
            :key="axis.name"
            type="button"
            class="radar-labels__item"
            :class="{ 'radar-labels__item--active': index === selectedAxisIndex }"
            :style="{ left: axis.labelPos.x + 'px', top: axis.labelPos.y + 'px' }"
            @click="selectAxis(index)"
          >
            <span>{{ axis.name }}</span>
            <strong>{{ axis.my }}%</strong>
          </button>
        </div>

        <div class="radar-center">
          <p>综合趋势</p>
          <strong>{{ radarOverallScore }}%</strong>
          <span>{{ selectedAxis.name }}维度</span>
        </div>
      </div>

      <div class="radar-insight">
        <div>
          <p class="radar-insight__title">当前维度</p>
          <strong>{{ selectedAxis.name }}</strong>
        </div>
        <div>
          <p class="radar-insight__title">我的分数</p>
          <strong>{{ selectedAxis.my }}%</strong>
        </div>
        <div>
          <p class="radar-insight__title">班级平均</p>
          <strong>{{ selectedAxis.classValue }}%</strong>
        </div>
      </div>

      <p class="radar-desc">{{ selectedAxis.detail }}</p>

      <div class="radar-legend">
        <span><i class="radar-legend__dot radar-legend__dot--mine"></i>我的能力</span>
        <span><i class="radar-legend__dot radar-legend__dot--class"></i>班级平均</span>
      </div>
    </section>

    <section class="weak-list page-card">
      <div class="weak-list__head">
        <div>
          <p class="radar-card__eyebrow">急需突破</p>
          <h2>薄弱知识点</h2>
        </div>
        <button type="button" class="weak-list__action" @click="goPractice">去练习</button>
      </div>

      <article v-for="item in weakPoints" :key="item.name" class="weak-item">
        <div class="weak-item__meta">
          <span class="weak-item__dot" :class="`weak-item__dot--${item.level}`"></span>
          <div>
            <p class="weak-item__name">{{ item.name }}</p>
            <p class="weak-item__detail">{{ item.detail }}</p>
          </div>
        </div>

        <button type="button" class="weak-item__button" @click="targetBoost(item)">
          {{ item.buttonLabel }}
        </button>
      </article>
    </section>

    <section v-if="activeTab === 'level'" class="level-card page-card">
      <div class="weak-list__head">
        <div>
          <p class="radar-card__eyebrow">能力分布</p>
          <h2>层级占比</h2>
        </div>
        <span class="level-card__subject">{{ selectedSubject.label }}</span>
      </div>

      <div class="level-bars">
        <div v-for="level in levelDistribution" :key="level.label" class="level-bar">
          <div class="level-bar__top">
            <span>{{ level.label }}</span>
            <strong>{{ level.value }}%</strong>
          </div>
          <div class="level-bar__track">
            <div class="level-bar__fill" :style="{ width: `${level.value}%`, background: level.color }"></div>
          </div>
        </div>
      </div>
    </section>

    <template #footer>
      <nav class="weakness-bottom-nav" aria-label="底部导航">
        <button type="button" class="bottom-tab" @click="router.push('/home')">
          <span class="bottom-tab__icon bottom-tab__icon--home"></span>
          <span class="bottom-tab__label">首页</span>
        </button>
        <button type="button" class="bottom-tab bottom-tab--active">
          <span class="bottom-tab__icon bottom-tab__icon--study"></span>
          <span class="bottom-tab__label">学习</span>
        </button>
        <button type="button" class="bottom-tab" @click="router.push('/wrong-book')">
          <span class="bottom-tab__icon bottom-tab__icon--wrong"></span>
          <span class="bottom-tab__label">错题本</span>
        </button>
        <button type="button" class="bottom-tab" @click="router.push('/profile')">
          <span class="bottom-tab__icon bottom-tab__icon--profile"></span>
          <span class="bottom-tab__label">我的</span>
        </button>
      </nav>
    </template>
  </AppMobileFrame>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import AppMobileFrame from '../../components/layout/AppMobileFrame.vue'
import { useStudentStore } from '../../stores/studentStore'

const router = useRouter()
const studentStore = useStudentStore()

const currentTime = ref('9:41')
const selectedAxisIndex = ref(0)

const selectedSubjectKey = computed({
  get: () => studentStore.weaknessMap.selectedSubjectKey,
  set: (value) => {
    studentStore.weaknessMap.selectedSubjectKey = value
  },
})
const activeTab = computed({
  get: () => studentStore.weaknessMap.activeTab,
  set: (value) => {
    studentStore.weaknessMap.activeTab = value
  },
})

const subjects = computed(() => studentStore.weaknessMap.subjects)

const tabs = [
  { key: 'mastery', label: '雷达图' },
  { key: 'level', label: '能力分布' },
]

const subjectMap = {
  math: {
    label: '数学',
    chart: {
      title: '函数与三角综合雷达',
      subtitle: '近 7 天对比',
      axes: [
        { label: '思维', my: 86, class: 69, detail: '思维转换已经明显提升，但遇到新题型时仍需要更多审题。' },
        { label: '运算', my: 72, class: 61, detail: '基础运算稳定，复杂化简时容易丢步骤。' },
        { label: '应用', my: 64, class: 59, detail: '会做熟题，但遇到条件变化时容易漏掉限定条件。' },
        { label: '表达', my: 78, class: 63, detail: '书写比较完整，建议把结论表达得更标准。' },
        { label: '反思', my: 69, class: 58, detail: '错题回看开始见效，继续保持“错题回炉”节奏。' },
      ],
    },
    weakPoints: [
      { name: '函数的性质(单调性)', detail: '数学-函数 | 最近练习正确率 41%', buttonLabel: '靶向补弱', level: 'weak' },
      { name: '函数的图像变换', detail: '数学-函数 | 最近练习正确率 56%', buttonLabel: '去练习', level: 'medium' },
    ],
    levelDistribution: [
      { label: '掌握良好', value: 58, color: '#00B894' },
      { label: '基本掌握', value: 26, color: '#FDCB6E' },
      { label: '薄弱', value: 16, color: '#FF7675' },
    ],
  },
  physics: {
    label: '物理',
    chart: {
      title: '力学核心雷达',
      subtitle: '本周班级对照',
      axes: [
        { label: '概念', my: 74, class: 67, detail: '概念理解不错，但对综合题中的隐含条件还要更敏感。' },
        { label: '建模', my: 62, class: 57, detail: '建模速度正常，建议加强受力分析图的先行判断。' },
        { label: '计算', my: 77, class: 61, detail: '计算能力稳定，但多步骤题仍可能出现中途漏项。' },
        { label: '实验', my: 68, class: 59, detail: '实验题表述较完整，注意把数据和结论对应起来。' },
        { label: '迁移', my: 71, class: 60, detail: '能把课内方法迁移到相似题，跨章节迁移还需训练。' },
      ],
    },
    weakPoints: [
      { name: '牛顿第二定律公式误用', detail: '物理-动力学 | 最近练习正确率 38%', buttonLabel: '靶向补弱', level: 'weak' },
      { name: '受力分析图像建立', detail: '物理-力学 | 最近练习正确率 59%', buttonLabel: '去练习', level: 'medium' },
    ],
    levelDistribution: [
      { label: '掌握良好', value: 52, color: '#00B894' },
      { label: '基本掌握', value: 28, color: '#FDCB6E' },
      { label: '薄弱', value: 20, color: '#FF7675' },
    ],
  },
  english: {
    label: '英语',
    chart: {
      title: '阅读与语法雷达',
      subtitle: '连续练习趋势',
      axes: [
        { label: '词汇', my: 79, class: 67, detail: '词汇积累比较稳定，但同义替换题还可以更快判断。' },
        { label: '语法', my: 73, class: 62, detail: '语法填空正确率不错，复杂从句仍需要强化。' },
        { label: '阅读', my: 66, class: 61, detail: '阅读速度够用，长难句拆解需要再练。' },
        { label: '完形', my: 58, class: 56, detail: '完形靠上下文猜测较多，建议先抓主题词。' },
        { label: '写作', my: 70, class: 60, detail: '作文结构清晰，连接词和句式变化还能提升。' },
      ],
    },
    weakPoints: [
      { name: '长难句结构识别', detail: '英语-阅读 | 最近练习正确率 43%', buttonLabel: '靶向补弱', level: 'weak' },
      { name: '完形上下文判断', detail: '英语-综合 | 最近练习正确率 55%', buttonLabel: '去练习', level: 'medium' },
    ],
    levelDistribution: [
      { label: '掌握良好', value: 49, color: '#00B894' },
      { label: '基本掌握', value: 31, color: '#FDCB6E' },
      { label: '薄弱', value: 20, color: '#FF7675' },
    ],
  },
}

const selectedSubject = computed(() => subjectMap[selectedSubjectKey.value])
const levelDistribution = computed(() => selectedSubject.value.levelDistribution)
const weakPoints = computed(() => selectedSubject.value.weakPoints)
const chart = computed(() => selectedSubject.value.chart)

const radarRings = [28, 52, 76, 100]
const chartGradientId = 'weakness-radar-gradient'

const radarAxes = computed(() => {
  const axes = chart.value.axes
  const angleStep = (Math.PI * 2) / axes.length
  const center = { x: 160, y: 160 }
  const chartRadius = 100

  return axes.map((axis, index) => {
    const angle = -Math.PI / 2 + angleStep * index
    const outer = polarToCartesian(center.x, center.y, chartRadius, angle)
    const mine = polarToCartesian(center.x, center.y, (axis.my / 100) * chartRadius, angle)
    const classPoint = polarToCartesian(center.x, center.y, (axis.class / 100) * chartRadius, angle)
    const labelPoint = polarToCartesian(center.x, center.y, 132, angle)

    return {
      name: axis.label,
      my: axis.my,
      classValue: axis.class,
      detail: axis.detail,
      outer,
      mine,
      classPoint,
      labelPos: {
        x: labelPoint.x,
        y: labelPoint.y,
      },
    }
  })
})

const myPolygonPoints = computed(() => toPolygonPoints(radarAxes.value.map((axis) => axis.mine)))
const classPolygonPoints = computed(() => toPolygonPoints(radarAxes.value.map((axis) => axis.classPoint)))
const selectedAxis = computed(() => radarAxes.value[selectedAxisIndex.value] || radarAxes.value[0])
const radarOverallScore = computed(() => Math.round(chart.value.axes.reduce((sum, axis) => sum + axis.my, 0) / chart.value.axes.length))

let clockTimer = null

function updateClock() {
  const now = new Date()
  currentTime.value = now.toLocaleTimeString('zh-CN', {
    hour: '2-digit',
    minute: '2-digit',
  })
}

function goBack() {
  router.back()
}

function selectSubject(key) {
  selectedSubjectKey.value = key
  selectedAxisIndex.value = 0
}

function selectAxis(index) {
  selectedAxisIndex.value = index
}

function targetBoost(item) {
  router.push({ path: '/target-practice', query: { source: item.name } })
}

function goPractice() {
  router.push('/target-practice')
}

function polarToCartesian(cx, cy, radius, angle) {
  return {
    x: cx + radius * Math.cos(angle),
    y: cy + radius * Math.sin(angle),
  }
}

function toPolygonPoints(points) {
  return points.map((point) => `${point.x},${point.y}`).join(' ')
}

onMounted(() => {
  updateClock()
  clockTimer = window.setInterval(updateClock, 30000)
})

onBeforeUnmount(() => {
  if (clockTimer) {
    window.clearInterval(clockTimer)
  }
})
</script>

<style scoped>
.weakness-scroll {
  display: grid;
  gap: 12px;
  padding-bottom: 16px;
}

.weakness-topbar {
  height: 52px;
  padding: 0 14px 0 12px;
  display: grid;
  grid-template-columns: 36px 1fr auto;
  align-items: center;
  gap: 8px;
}

.weakness-topbar__titles {
  display: grid;
  gap: 2px;
  justify-items: center;
}

.weakness-topbar__titles p,
.weakness-topbar__titles h1 {
  margin: 0;
}

.weakness-topbar__titles p {
  color: #8f90a6;
  font-size: 0.72rem;
}

.weakness-topbar__titles h1 {
  color: #171728;
  font-size: 1rem;
  font-weight: 700;
}

.weakness-topbar__action {
  border: 0;
  border-radius: 999px;
  padding: 8px 12px;
  background: linear-gradient(135deg, #6c5ce7, #8f7dff);
  color: #fff;
  font-size: 0.75rem;
  font-weight: 700;
  box-shadow: 0 10px 24px rgba(108, 92, 231, 0.2);
}

.icon-button {
  width: 30px;
  height: 30px;
  border: 0;
  border-radius: 50%;
  background: rgba(108, 92, 231, 0.08);
  display: grid;
  place-items: center;
}

.icon-button__arrow {
  width: 9px;
  height: 9px;
  border-left: 2px solid #6c5ce7;
  border-bottom: 2px solid #6c5ce7;
  transform: rotate(45deg);
  margin-left: 4px;
}

.subject-strip,
.mode-strip,
.radar-card,
.weak-list,
.level-card {
  width: calc(100% - 24px);
  margin: 0 12px;
}

.subject-strip,
.mode-strip {
  display: flex;
  gap: 10px;
  overflow: auto;
  padding: 4px 2px;
}

.subject-strip::-webkit-scrollbar,
.mode-strip::-webkit-scrollbar {
  width: 0;
  height: 0;
}

.subject-strip__item,
.mode-strip__item {
  border: 0;
  border-radius: 999px;
  background: #f4f5fb;
  color: #6a6b88;
  padding: 10px 14px;
  font-size: 0.82rem;
  font-weight: 700;
  white-space: nowrap;
}

.subject-strip__item--active,
.mode-strip__item--active {
  color: #6c5ce7;
  background: rgba(108, 92, 231, 0.12);
  box-shadow: inset 0 0 0 1px rgba(108, 92, 231, 0.1);
}

.radar-card {
  padding: 16px;
  border-radius: 26px;
  background:
    radial-gradient(circle at top left, rgba(124, 108, 255, 0.14), transparent 45%),
    radial-gradient(circle at bottom right, rgba(0, 184, 148, 0.12), transparent 42%),
    #fff;
  box-shadow: 0 18px 40px rgba(25, 32, 72, 0.08);
}

.radar-card__head,
.weak-list__head {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 12px;
}

.radar-card__eyebrow {
  margin: 0 0 6px;
  color: #8f90a6;
  font-size: 0.72rem;
  font-weight: 700;
  letter-spacing: 0.06em;
  text-transform: uppercase;
}

.radar-card__head h2,
.weak-list__head h2 {
  margin: 0;
  color: #111320;
  font-size: 1.02rem;
  font-weight: 800;
}

.radar-card__tag,
.level-card__subject {
  color: #7d7f95;
  font-size: 0.75rem;
  font-weight: 700;
  white-space: nowrap;
}

.radar-card__chart-wrap {
  position: relative;
  margin-top: 14px;
  border-radius: 24px;
  background: linear-gradient(180deg, rgba(245, 246, 255, 0.92), rgba(255, 255, 255, 0.98));
  overflow: hidden;
}

.radar-card__chart {
  width: 100%;
  height: auto;
  aspect-ratio: 1;
}

.radar-ring {
  fill: none;
  stroke: rgba(135, 140, 171, 0.14);
  stroke-width: 1;
}

.radar-axis {
  stroke: rgba(135, 140, 171, 0.16);
  stroke-width: 1;
}

.radar-polygon {
  fill: url(#weakness-radar-gradient);
  stroke-linejoin: round;
}

.radar-polygon--class {
  fill-opacity: 0.28;
  stroke: #f3bd5f;
  stroke-width: 2;
}

.radar-polygon--mine {
  fill-opacity: 0.2;
  stroke: #6c5ce7;
  stroke-width: 2.5;
}

.radar-point--mine {
  fill: #6c5ce7;
  stroke: #fff;
  stroke-width: 2;
}

.radar-point--class {
  fill: #f3bd5f;
  stroke: #fff;
  stroke-width: 2;
}

.radar-point-group {
  cursor: pointer;
}

.radar-labels {
  position: absolute;
  inset: 0;
}

.radar-labels__item {
  position: absolute;
  transform: translate(-50%, -50%);
  border: 0;
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.92);
  padding: 8px 10px;
  display: grid;
  gap: 2px;
  min-width: 58px;
  box-shadow: 0 8px 18px rgba(25, 32, 72, 0.08);
}

.radar-labels__item span {
  color: #6a6b88;
  font-size: 0.72rem;
  font-weight: 700;
}

.radar-labels__item strong {
  color: #111320;
  font-size: 0.8rem;
  font-weight: 800;
}

.radar-labels__item--active {
  outline: 2px solid rgba(108, 92, 231, 0.28);
}

.radar-center {
  position: absolute;
  left: 50%;
  top: 50%;
  width: 98px;
  height: 98px;
  transform: translate(-50%, -50%);
  border-radius: 50%;
  background: linear-gradient(180deg, rgba(108, 92, 231, 0.96), rgba(85, 70, 219, 0.92));
  color: #fff;
  display: grid;
  place-items: center;
  text-align: center;
  box-shadow: 0 18px 36px rgba(108, 92, 231, 0.28);
}

.radar-center p,
.radar-center span,
.radar-center strong {
  margin: 0;
}

.radar-center p {
  font-size: 0.72rem;
  opacity: 0.86;
}

.radar-center strong {
  font-size: 1.4rem;
  line-height: 1;
}

.radar-center span {
  font-size: 0.7rem;
  opacity: 0.8;
}

.radar-insight {
  margin-top: 14px;
  padding: 12px 14px;
  border-radius: 20px;
  background: #f7f8ff;
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 10px;
}

.radar-insight > div {
  min-width: 0;
}

.radar-insight__title {
  margin: 0 0 6px;
  color: #8f90a6;
  font-size: 0.7rem;
  font-weight: 700;
}

.radar-insight strong {
  color: #111320;
  font-size: 0.94rem;
}

.radar-desc {
  margin: 12px 2px 0;
  color: #4f5471;
  font-size: 0.85rem;
  line-height: 1.55;
}

.radar-legend {
  margin-top: 12px;
  display: flex;
  gap: 14px;
  flex-wrap: wrap;
  color: #6a6b88;
  font-size: 0.75rem;
  font-weight: 700;
}

.radar-legend__dot {
  display: inline-block;
  width: 10px;
  height: 10px;
  border-radius: 50%;
  margin-right: 6px;
}

.radar-legend__dot--mine {
  background: #6c5ce7;
}

.radar-legend__dot--class {
  background: #f3bd5f;
}

.weak-list,
.level-card {
  padding: 16px;
  border-radius: 24px;
  background: #fff;
  box-shadow: 0 12px 28px rgba(25, 32, 72, 0.06);
}

.weak-list__action,
.weak-item__button {
  border: 0;
  border-radius: 999px;
  padding: 8px 12px;
  background: rgba(108, 92, 231, 0.12);
  color: #6c5ce7;
  font-size: 0.74rem;
  font-weight: 700;
}

.weak-list {
  display: grid;
  gap: 12px;
}

.weak-item {
  padding: 12px 0;
  border-top: 1px solid rgba(140, 145, 173, 0.12);
  display: flex;
  justify-content: space-between;
  gap: 12px;
}

.weak-item:first-of-type {
  border-top: 0;
  padding-top: 0;
}

.weak-item__meta {
  display: flex;
  gap: 10px;
  align-items: flex-start;
}

.weak-item__dot {
  width: 12px;
  height: 12px;
  margin-top: 4px;
  border-radius: 50%;
  flex: 0 0 auto;
}

.weak-item__dot--weak {
  background: #ff7675;
}

.weak-item__dot--medium {
  background: #f3bd5f;
}

.weak-item__dot--good {
  background: #00b894;
}

.weak-item__name {
  margin: 0;
  color: #111320;
  font-size: 0.9rem;
  font-weight: 700;
}

.weak-item__detail {
  margin: 6px 0 0;
  color: #8f90a6;
  font-size: 0.74rem;
}

.level-card {
  display: grid;
  gap: 12px;
}

.level-bars {
  display: grid;
  gap: 12px;
}

.level-bar__top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 6px;
  color: #404358;
  font-size: 0.84rem;
  font-weight: 700;
}

.level-bar__track {
  height: 9px;
  border-radius: 999px;
  background: #edf0f7;
  overflow: hidden;
}

.level-bar__fill {
  height: 100%;
  border-radius: inherit;
}

.weakness-bottom-nav {
  margin: 2px 12px 0;
  padding: 12px 10px 8px;
  border-radius: 22px 22px 0 0;
  background: rgba(255, 255, 255, 0.96);
  box-shadow: 0 -10px 24px rgba(25, 32, 72, 0.04);
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 6px;
}

.bottom-tab {
  border: 0;
  background: transparent;
  display: grid;
  justify-items: center;
  gap: 4px;
  color: #a0a3bb;
  font-size: 0.72rem;
  font-weight: 700;
}

.bottom-tab--active {
  color: #6c5ce7;
}

@media (max-width: 420px) {
  .subject-strip,
  .mode-strip,
  .radar-card,
  .weak-list,
  .level-card {
    width: calc(100% - 16px);
    margin-left: 8px;
    margin-right: 8px;
  }

  .radar-insight {
    grid-template-columns: 1fr;
  }

  .weakness-bottom-nav {
    margin-left: 8px;
    margin-right: 8px;
  }
}
</style>