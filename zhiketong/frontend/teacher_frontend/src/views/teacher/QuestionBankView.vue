<script setup>
import { computed, ref } from 'vue'

const search = ref('')
const activeFilters = ref(['数学', '函数'])
const importName = ref('')

const allFilters = ['数学', '函数', '高频错题']

const questions = ref([
  { id: 1, title: '函数单调性综合题', point: '函数 · 单调性', diff: '困难', usage: 1284, acc: 32 },
  { id: 2, title: '立体几何空间向量', point: '立体几何', diff: '中等', usage: 932, acc: 58 },
  { id: 3, title: '导数切线方程应用', point: '导数', diff: '困难', usage: 1506, acc: 27 },
  { id: 4, title: '集合与逻辑基础', point: '集合', diff: '基础', usage: 624, acc: 82 },
])

const stats = [
  { label: '题库总量', value: '12,486', hint: '本周新增 248 题' },
  { label: '高频错题', value: '326', hint: '函数模块占比较高' },
  { label: 'AI推荐题', value: '1,284', hint: '根据学情动态生成' },
  { label: '共享题库', value: '68%', hint: '跨班级协同使用' },
]

const filteredQuestions = computed(() => {
  const q = search.value.trim()

  return questions.value.filter((item) => {
    const byKeyword = !q || item.title.includes(q) || item.point.includes(q)
    const bySubject = !activeFilters.value.includes('数学') || item.point.includes('函数') || item.point.includes('导数') || item.point.includes('集合') || item.point.includes('几何')
    const byTopic = !activeFilters.value.includes('函数') || item.point.includes('函数')
    const byWrong = !activeFilters.value.includes('高频错题') || item.acc < 60

    return byKeyword && bySubject && byTopic && byWrong
  })
})

function toggleFilter(name) {
  if (activeFilters.value.includes(name)) {
    activeFilters.value = activeFilters.value.filter((f) => f !== name)
    return
  }
  activeFilters.value = [...activeFilters.value, name]
}

function addQuestion() {
  const id = Date.now()
  questions.value.unshift({
    id,
    title: `新建题目 ${String(id).slice(-4)}`,
    point: '函数 · 新增',
    diff: '中等',
    usage: 0,
    acc: 0,
  })
}

function onFileChange(event) {
  const file = event.target.files?.[0]
  importName.value = file ? file.name : ''
}

function diffClass(diff) {
  if (diff === '基础') return 'diff-easy'
  if (diff === '中等') return 'diff-mid'
  return 'diff-hard'
}
</script>

<template>
  <section class="page">
    <div class="page-head">
      <h1>题库管理</h1>
      <div class="actions">
        <input v-model="search" placeholder="搜索题目 / 知识点 / 章节" />
        <button @click="addQuestion">+ 新建题目</button>
      </div>
    </div>

    <div class="stats-grid">
      <article v-for="item in stats" :key="item.label" class="stat-card">
        <p>{{ item.label }}</p>
        <h3>{{ item.value }}</h3>
        <small>{{ item.hint }}</small>
      </article>
    </div>

    <div class="content-grid">
      <article class="card table-card">
        <div class="table-head">
          <h3>题目列表</h3>
          <div class="filter-group">
            <button
              v-for="item in allFilters"
              :key="item"
              :class="{ active: activeFilters.includes(item) }"
              @click="toggleFilter(item)"
            >
              {{ item }}
            </button>
          </div>
        </div>

        <div class="table-wrap">
          <table>
            <thead>
              <tr>
                <th>题目名称</th>
                <th>知识点</th>
                <th>难度</th>
                <th>使用次数</th>
                <th>正确率</th>
                <th>操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="item in filteredQuestions" :key="item.id">
                <td>{{ item.title }}</td>
                <td>{{ item.point }}</td>
                <td>
                  <span class="diff" :class="diffClass(item.diff)">{{ item.diff }}</span>
                </td>
                <td>{{ item.usage }}</td>
                <td>{{ item.acc }}%</td>
                <td class="op">
                  <button>编辑</button>
                  <button>分析</button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </article>

      <article class="card import-card">
        <h3>批量导入题库</h3>
        <label class="upload-box">
          <div class="icon">上传</div>
          <p class="title">拖拽上传题库文件</p>
          <p class="desc">支持 Word / Excel / PDF / 图片OCR导题</p>
          <span class="btn">选择文件</span>
          <input type="file" @change="onFileChange" />
        </label>
        <p v-if="importName" class="file-name">已选择：{{ importName }}</p>
      </article>
    </div>
  </section>
</template>

<style scoped>
.page {
  display: grid;
  gap: 16px;
}

.page-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
}

.page-head h1 {
  color: #1f2937;
  font-size: 30px;
}

.actions {
  display: flex;
  gap: 10px;
}

.actions input {
  width: 270px;
  border: 1px solid #e5e7eb;
  border-radius: 10px;
  background: #fff;
  padding: 10px 12px;
}

.actions button {
  border: 0;
  border-radius: 10px;
  padding: 10px 14px;
  color: #fff;
  background: linear-gradient(135deg, #6366f1 0%, #4f46e5 100%);
  box-shadow: 0 8px 20px rgba(79, 70, 229, 0.2);
  font-weight: 600;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 12px;
}

.stat-card {
  background: #fff;
  border: 1px solid #e2e8f0;
  border-radius: 14px;
  box-shadow: 0 4px 14px rgba(15, 23, 42, 0.04);
  padding: 16px;
}

.stat-card p {
  color: #6b7280;
  font-size: 13px;
}

.stat-card h3 {
  margin-top: 8px;
  color: #111827;
  font-size: 29px;
}

.stat-card small {
  margin-top: 8px;
  display: block;
  color: #10b981;
  font-size: 12px;
}

.content-grid {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 12px;
}

.card {
  background: #fff;
  border: 1px solid #e2e8f0;
  border-radius: 14px;
  box-shadow: 0 4px 14px rgba(15, 23, 42, 0.04);
  padding: 16px;
}

.table-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 8px;
}

.table-head h3,
.import-card h3 {
  color: #1f2937;
  font-size: 17px;
  font-weight: 700;
}

.filter-group {
  display: flex;
  gap: 8px;
}

.filter-group button {
  border: 1px solid #e5e7eb;
  background: #fff;
  border-radius: 8px;
  padding: 6px 10px;
  color: #374151;
  font-size: 12px;
}

.filter-group button.active {
  border-color: #6366f1;
  background: #eef2ff;
  color: #4338ca;
}

.table-wrap {
  overflow: auto;
  max-height: 430px;
  margin-top: 12px;
}

table {
  width: 100%;
  border-collapse: collapse;
  min-width: 700px;
}

th,
td {
  text-align: left;
  padding: 12px;
  border-bottom: 1px solid #f3f4f6;
  font-size: 13px;
}

th {
  color: #6b7280;
  background: #f9fafb;
  position: sticky;
  top: 0;
}

.diff {
  border-radius: 999px;
  padding: 4px 8px;
  font-size: 12px;
  font-weight: 700;
}

.diff-easy {
  background: #dcfce7;
  color: #16a34a;
}

.diff-mid {
  background: #fef3c7;
  color: #d97706;
}

.diff-hard {
  background: #fee2e2;
  color: #dc2626;
}

.op {
  display: flex;
  gap: 8px;
}

.op button {
  border: 0;
  background: #f3f4f6;
  border-radius: 8px;
  padding: 6px 10px;
  font-size: 12px;
}

.import-card {
  display: grid;
  align-content: start;
  gap: 12px;
}

.upload-box {
  background: #f8faff;
  border: 1px dashed #c7d2fe;
  border-radius: 14px;
  min-height: 320px;
  display: grid;
  place-items: center;
  text-align: center;
  gap: 8px;
  padding: 18px;
  cursor: pointer;
}

.upload-box .icon {
  width: 68px;
  height: 68px;
  border-radius: 50%;
  display: grid;
  place-items: center;
  background: #e0e7ff;
  color: #4338ca;
  font-weight: 700;
}

.upload-box .title {
  color: #1f2937;
  font-weight: 700;
}

.upload-box .desc {
  color: #6b7280;
  font-size: 12px;
}

.upload-box .btn {
  margin-top: 6px;
  border-radius: 8px;
  padding: 8px 12px;
  background: #4f46e5;
  color: #fff;
  font-size: 12px;
}

.upload-box input {
  display: none;
}

.file-name {
  color: #475569;
  font-size: 12px;
}

@media (max-width: 1100px) {
  .stats-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .content-grid {
    grid-template-columns: 1fr;
  }

  .page-head {
    flex-direction: column;
    align-items: stretch;
  }

  .actions {
    width: 100%;
  }

  .actions input {
    flex: 1;
  }
}
</style>
