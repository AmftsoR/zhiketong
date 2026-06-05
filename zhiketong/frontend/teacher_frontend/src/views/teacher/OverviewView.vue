<script setup>
import { computed, ref } from 'vue'

const selectedRange = ref('近7天')
const selectedClass = ref('高一（1）班')

const ranges = ['近7天', '近30天']
const classes = ['高一（1）班', '高一（2）班']

const metricMap = {
  '近7天': [
    { label: '班级平均分', value: '78.5', trend: '+5.2' },
    { label: '知识点掌握率', value: '68%', trend: '+8%' },
    { label: '优秀率', value: '32%', trend: '+6%' },
    { label: '待关注学生', value: '7人', trend: '-2人' },
  ],
  '近30天': [
    { label: '班级平均分', value: '74.1', trend: '+2.1' },
    { label: '知识点掌握率', value: '63%', trend: '+3%' },
    { label: '优秀率', value: '28%', trend: '+2%' },
    { label: '待关注学生', value: '9人', trend: '-1人' },
  ],
}

const trendMap = {
  '近7天': {
    avg: [65, 80, 78, 85, 80, 88, 92],
    cls: [55, 70, 68, 74, 64, 80, 78],
  },
  '近30天': {
    avg: [58, 60, 66, 70, 72, 75, 74],
    cls: [55, 58, 60, 65, 67, 70, 71],
  },
}

const heatRows = ['函数与导数', '数列', '三角函数', '立体几何', '解析几何', '不等式']
const heatCols = ['知识点1', '知识点2', '知识点3', '知识点4', '知识点5', '知识点6', '知识点7', '知识点8']

const heatValues = [
  [2, 3, 2, 2, 4, 2, 3, 2],
  [3, 4, 2, 3, 2, 4, 3, 1],
  [2, 2, 4, 1, 3, 2, 2, 4],
  [2, 1, 3, 2, 2, 4, 3, 2],
  [4, 3, 2, 4, 1, 2, 0, 3],
  [3, 2, 1, 3, 2, 2, 4, 2],
]

const distribution = [
  { label: '90-100', count: 10, color: '#4f46e5' },
  { label: '80-89', count: 15, color: '#3b82f6' },
  { label: '70-79', count: 12, color: '#10b981' },
  { label: '60-69', count: 7, color: '#fcd34d' },
  { label: '0-59', count: 6, color: '#ef4444' },
]

const totalStudents = computed(() => distribution.reduce((s, i) => s + i.count, 0))
const donutStyle = computed(() => {
  const stops = []
  let start = 0

  distribution.forEach((item) => {
    const part = (item.count / totalStudents.value) * 360
    const end = start + part
    stops.push(`${item.color} ${start}deg ${end}deg`)
    start = end
  })

  return { background: `conic-gradient(${stops.join(', ')})` }
})

const metrics = computed(() => metricMap[selectedRange.value])

const chartPoints = computed(() => {
  const values = trendMap[selectedRange.value]
  const toPoints = (arr) =>
    arr
      .map((v, i) => {
        const x = i * 90
        const y = 170 - (v / 100) * 150
        return `${x},${y}`
      })
      .join(' ')

  return {
    avg: toPoints(values.avg),
    cls: toPoints(values.cls),
  }
})

const focusStudents = [
  { name: '李明宇', score: 60, reason: '函数单调性薄弱' },
  { name: '王思涵', score: 62, reason: '立体几何薄弱' },
  { name: '张雨桐', score: 65, reason: '基础知识不牢固' },
  { name: '陈佳乐', score: 68, reason: '计算能力薄弱' },
  { name: '刘浩然', score: 69, reason: '学习积极性低' },
]

function levelClass(v) {
  if (v >= 4) return 'level-good'
  if (v === 3) return 'level-mid'
  if (v === 2) return 'level-fair'
  if (v === 1) return 'level-light'
  return 'level-weak'
}
</script>

<template>
  <section class="page">
    <div class="page-head">
      <h1>数据总览</h1>
      <div class="filters">
        <select v-model="selectedClass">
          <option v-for="item in classes" :key="item" :value="item">{{ item }}</option>
        </select>
        <select v-model="selectedRange">
          <option v-for="item in ranges" :key="item" :value="item">{{ item }}</option>
        </select>
      </div>
    </div>

    <div class="metrics-grid">
      <article v-for="item in metrics" :key="item.label" class="metric-card">
        <p>{{ item.label }}</p>
        <h3>{{ item.value }}</h3>
        <small :class="item.trend.includes('-') ? 'down' : 'up'">{{ item.trend }}</small>
      </article>
    </div>

    <div class="content-grid">
      <article class="panel panel-wide">
        <div class="panel-head">
          <h3>知识点掌握热力图</h3>
          <div class="legend">
            <span><i class="dot good"></i>掌握较好</span>
            <span><i class="dot mid"></i>一般</span>
            <span><i class="dot weak"></i>薄弱</span>
          </div>
        </div>

        <div class="heatmap">
          <div class="row head-row">
            <div class="corner"></div>
            <div v-for="col in heatCols" :key="col" class="cell head">{{ col }}</div>
          </div>
          <div v-for="(row, r) in heatRows" :key="row" class="row">
            <div class="label">{{ row }}</div>
            <div
              v-for="(value, c) in heatValues[r]"
              :key="`${row}-${c}`"
              class="cell"
              :class="levelClass(value)"
            ></div>
          </div>
        </div>
      </article>

      <article class="panel panel-side">
        <h3>成绩分布</h3>
        <div class="donut-wrap">
          <div class="donut" :style="donutStyle">
            <div class="hole">{{ totalStudents }}人</div>
          </div>
          <ul class="dist-list">
            <li v-for="item in distribution" :key="item.label">
              <i :style="{ background: item.color }"></i>
              {{ item.label }}（{{ item.count }}人）
            </li>
          </ul>
        </div>
      </article>
    </div>

    <div class="content-grid second">
      <article class="panel panel-wide">
        <div class="panel-head">
          <h3>学生成绩趋势</h3>
          <div class="legend">
            <span><i class="line avg"></i>平均分</span>
            <span><i class="line cls"></i>班级平均</span>
          </div>
        </div>
        <div class="chart">
          <svg viewBox="0 0 560 210" preserveAspectRatio="none">
            <g class="grid-line">
              <line x1="0" y1="20" x2="560" y2="20" />
              <line x1="0" y1="58" x2="560" y2="58" />
              <line x1="0" y1="96" x2="560" y2="96" />
              <line x1="0" y1="134" x2="560" y2="134" />
              <line x1="0" y1="172" x2="560" y2="172" />
            </g>
            <polyline class="line-avg" :points="chartPoints.avg" />
            <polyline class="line-cls" :points="chartPoints.cls" />
          </svg>
        </div>
      </article>

      <article class="panel panel-side">
        <h3>待关注学生 TOP5</h3>
        <ul class="focus-list">
          <li v-for="item in focusStudents" :key="item.name">
            <span class="name">{{ item.name }}</span>
            <b>{{ item.score }}分</b>
            <small>{{ item.reason }}</small>
          </li>
        </ul>
      </article>
    </div>
  </section>
</template>

<style scoped>
.page {
  display: grid;
  gap: 18px;
}

.page-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.page-head h1 {
  font-size: 30px;
  color: #111827;
}

.filters {
  display: flex;
  gap: 10px;
}

.filters select {
  background: #fff;
  border: 1px solid #d9e0eb;
  border-radius: 8px;
  padding: 8px 10px;
}

.metrics-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 14px;
}

.metric-card {
  background: #fff;
  border-radius: 14px;
  border: 1px solid #e5eaf2;
  box-shadow: 0 4px 14px rgba(15, 23, 42, 0.04);
  padding: 16px;
}

.metric-card p {
  color: #64748b;
  font-size: 13px;
}

.metric-card h3 {
  margin-top: 8px;
  font-size: 28px;
  color: #0f172a;
}

.metric-card small {
  margin-top: 8px;
  display: block;
  font-size: 12px;
}

.metric-card small.up {
  color: #16a34a;
}

.metric-card small.down {
  color: #dc2626;
}

.content-grid {
  display: grid;
  grid-template-columns: 1.9fr 1fr;
  gap: 14px;
}

.panel {
  background: #fff;
  border-radius: 14px;
  border: 1px solid #e5eaf2;
  box-shadow: 0 4px 14px rgba(15, 23, 42, 0.04);
  padding: 16px;
}

.panel-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}

.panel-head h3,
.panel h3 {
  color: #111827;
  font-size: 16px;
  font-weight: 700;
}

.legend {
  display: flex;
  gap: 12px;
  font-size: 12px;
  color: #64748b;
}

.dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  display: inline-block;
  margin-right: 4px;
}

.dot.good {
  background: #64c598;
}

.dot.mid {
  background: #c7eed8;
}

.dot.weak {
  background: #ef4444;
}

.heatmap {
  overflow: auto;
}

.row {
  display: grid;
  grid-template-columns: 92px repeat(8, 1fr);
  gap: 6px;
  margin-bottom: 6px;
}

.cell,
.label,
.corner {
  height: 24px;
  border-radius: 4px;
}

.head {
  font-size: 12px;
  color: #64748b;
  display: grid;
  place-items: center;
}

.label {
  color: #64748b;
  display: flex;
  align-items: center;
  justify-content: flex-end;
  padding-right: 8px;
  font-size: 12px;
}

.level-good {
  background: #64c598;
}

.level-mid {
  background: #92d6b3;
}

.level-fair {
  background: #c1e7ce;
}

.level-light {
  background: #e5f5e0;
}

.level-weak {
  background: #fca5a5;
}

.donut-wrap {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-top: 8px;
}

.donut {
  width: 132px;
  height: 132px;
  border-radius: 50%;
  position: relative;
}

.hole {
  position: absolute;
  inset: 22px;
  border-radius: 50%;
  background: #fff;
  display: grid;
  place-items: center;
  font-weight: 700;
}

.dist-list {
  list-style: none;
  display: grid;
  gap: 8px;
  color: #475569;
  font-size: 12px;
}

.dist-list i {
  width: 9px;
  height: 9px;
  border-radius: 50%;
  display: inline-block;
  margin-right: 6px;
}

.chart {
  height: 210px;
}

.chart svg {
  width: 100%;
  height: 100%;
}

.grid-line line {
  stroke: #e2e8f0;
  stroke-width: 1;
}

.line-avg,
.line-cls {
  fill: none;
  stroke-width: 3;
  stroke-linecap: round;
  stroke-linejoin: round;
}

.line-avg {
  stroke: #3b82f6;
}

.line-cls {
  stroke: #818cf8;
  stroke-dasharray: 6 4;
}

.legend .line {
  width: 14px;
  height: 2px;
  display: inline-block;
  margin-right: 4px;
}

.legend .avg {
  background: #3b82f6;
}

.legend .cls {
  background: #818cf8;
}

.focus-list {
  list-style: none;
  display: grid;
  gap: 8px;
  margin-top: 10px;
}

.focus-list li {
  display: grid;
  grid-template-columns: 1fr auto;
  gap: 4px;
  padding: 10px;
  border-radius: 10px;
  background: #f8fafc;
}

.focus-list .name {
  font-weight: 600;
  color: #0f172a;
}

.focus-list b {
  color: #111827;
}

.focus-list small {
  grid-column: 1 / span 2;
  color: #64748b;
}

@media (max-width: 1100px) {
  .metrics-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .content-grid {
    grid-template-columns: 1fr;
  }

  .donut-wrap {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
