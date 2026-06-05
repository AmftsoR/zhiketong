<script setup>
import { computed, ref } from 'vue'

const search = ref('')

const students = ref([
  {
    name: '李明宇',
    status: '在线',
    completion: 100,
    accuracy: 92,
    tag: '潜力型',
    trend: '连续3次提升，进步明显',
    weak: '函数定义域、复合函数',
    suggestion: '建议增加变式练习训练',
  },
  {
    name: '王思涵',
    status: '学习中',
    completion: 95,
    accuracy: 82,
    tag: '稳定型',
    trend: '波动较小，学习习惯良好',
    weak: '三角恒等变换',
    suggestion: '可增加综合题训练',
  },
  {
    name: '赵小刚',
    status: '需关注',
    completion: 48,
    accuracy: 32,
    tag: '预警',
    trend: '连续下滑，需要家校协同',
    weak: '函数单调性、绝对值不等式',
    suggestion: '先补基础，再分层追赶',
  },
  {
    name: '李小龙',
    status: '在线',
    completion: 100,
    accuracy: 92,
    tag: '潜力型',
    trend: '状态稳定，理解速度快',
    weak: '立体几何空间想象',
    suggestion: '可尝试拔高题',
  },
  {
    name: '孟欣然',
    status: '学习中',
    completion: 95,
    accuracy: 86,
    tag: '稳定型',
    trend: '小幅提升，需保持节奏',
    weak: '解析几何计算精度',
    suggestion: '建议限时专项训练',
  },
  {
    name: '朱欣然',
    status: '学习中',
    completion: 100,
    accuracy: 79,
    tag: '稳定型',
    trend: '完成率高，正确率偏低',
    weak: '题干条件提取',
    suggestion: '强化审题与复盘',
  },
])

const selectedName = ref(students.value[0].name)

const filteredStudents = computed(() => {
  const q = search.value.trim()
  if (!q) return students.value
  return students.value.filter((s) => s.name.includes(q))
})

const selectedStudent = computed(() => {
  return students.value.find((s) => s.name === selectedName.value) || students.value[0]
})

function pickStudent(name) {
  selectedName.value = name
}

function statusClass(status) {
  if (status === '在线') return 'status-online'
  if (status === '学习中') return 'status-study'
  return 'status-alert'
}

function tagClass(tag) {
  if (tag === '潜力型') return 'tag-potential'
  if (tag === '稳定型') return 'tag-stable'
  return 'tag-warning'
}
</script>

<template>
  <section class="page">
    <div class="title-row">
      <h1>班级学生管理</h1>
    </div>

    <div class="stats-grid">
      <article class="stat-card">
        <p>班级人数</p>
        <h3>50</h3>
      </article>
      <article class="stat-card">
        <p>在线人数</p>
        <h3>32</h3>
      </article>
      <article class="stat-card">
        <p>预警学生</p>
        <h3>6</h3>
      </article>
      <article class="stat-card">
        <p>班级均分</p>
        <h3>86</h3>
      </article>
    </div>

    <div class="content-grid">
      <article class="card table-card">
        <div class="card-head">
          <h3>学生列表</h3>
          <span>共 {{ students.length }} 名学生</span>
        </div>

        <div class="search-row">
          <input v-model="search" placeholder="搜索学生姓名" />
        </div>

        <div class="table-wrap">
          <table>
            <thead>
              <tr>
                <th>姓名</th>
                <th>学习状态</th>
                <th>作业完成率</th>
                <th>作业正确率</th>
                <th>标签</th>
              </tr>
            </thead>
            <tbody>
              <tr
                v-for="item in filteredStudents"
                :key="item.name"
                :class="{ active: selectedName === item.name }"
                @click="pickStudent(item.name)"
              >
                <td>{{ item.name }}</td>
                <td>
                  <span class="pill" :class="statusClass(item.status)">{{ item.status }}</span>
                </td>
                <td>{{ item.completion }}%</td>
                <td>{{ item.accuracy }}%</td>
                <td>
                  <span class="pill" :class="tagClass(item.tag)">{{ item.tag }}</span>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </article>

      <article class="card detail-card">
        <div class="card-head">
          <h3>学生详情</h3>
          <span>AI学情画像</span>
        </div>

        <div class="profile">
          <div class="avatar">{{ selectedStudent.name.slice(0, 1) }}</div>
          <h4>{{ selectedStudent.name }}</h4>
          <span class="pill" :class="tagClass(selectedStudent.tag)">{{ selectedStudent.tag }}</span>
        </div>

        <div class="insight-list">
          <section>
            <h5>近期成绩趋势</h5>
            <p>{{ selectedStudent.trend }}</p>
          </section>
          <section>
            <h5>薄弱知识点</h5>
            <p>{{ selectedStudent.weak }}</p>
          </section>
          <section>
            <h5>AI建议</h5>
            <p>{{ selectedStudent.suggestion }}</p>
          </section>
        </div>
      </article>
    </div>
  </section>
</template>

<style scoped>
.page {
  display: grid;
  gap: 18px;
}

.title-row h1 {
  color: #0f172a;
  font-size: 30px;
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
  color: #64748b;
  font-size: 13px;
}

.stat-card h3 {
  margin-top: 8px;
  color: #0f172a;
  font-size: 30px;
}

.content-grid {
  display: grid;
  grid-template-columns: 1.7fr 1fr;
  gap: 12px;
}

.card {
  background: #fff;
  border: 1px solid #e2e8f0;
  border-radius: 14px;
  box-shadow: 0 4px 14px rgba(15, 23, 42, 0.04);
  padding: 16px;
}

.card-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.card-head h3 {
  color: #1e293b;
  font-size: 16px;
  font-weight: 700;
}

.card-head span {
  font-size: 12px;
  color: #94a3b8;
}

.search-row {
  margin-top: 12px;
}

.search-row input {
  width: 100%;
  border: 1px solid #e2e8f0;
  border-radius: 10px;
  background: #f8fafc;
  padding: 9px 12px;
}

.table-wrap {
  margin-top: 12px;
  overflow: auto;
  max-height: 430px;
}

table {
  width: 100%;
  border-collapse: collapse;
  min-width: 620px;
}

th,
td {
  text-align: left;
  padding: 12px;
  border-bottom: 1px solid #f1f5f9;
  font-size: 13px;
}

th {
  color: #64748b;
  background: #f8fafc;
  position: sticky;
  top: 0;
}

tbody tr {
  cursor: pointer;
}

tbody tr:hover {
  background: #f8fafc;
}

tbody tr.active {
  background: #eef6ff;
}

.pill {
  display: inline-block;
  border-radius: 999px;
  padding: 4px 10px;
  font-size: 12px;
  font-weight: 700;
}

.status-online {
  background: #dcfce7;
  color: #15803d;
}

.status-study {
  background: #dbeafe;
  color: #2563eb;
}

.status-alert {
  background: #fee2e2;
  color: #dc2626;
}

.tag-potential {
  background: #fef3c7;
  color: #d97706;
}

.tag-stable {
  background: #dcfce7;
  color: #15803d;
}

.tag-warning {
  background: #fee2e2;
  color: #dc2626;
}

.profile {
  margin-top: 14px;
  display: grid;
  justify-items: center;
  gap: 8px;
}

.avatar {
  width: 84px;
  height: 84px;
  border-radius: 50%;
  display: grid;
  place-items: center;
  font-size: 34px;
  font-weight: 700;
  background: #f1f5f9;
  color: #243b78;
}

.profile h4 {
  font-size: 20px;
  font-weight: 700;
  color: #111827;
}

.insight-list {
  margin-top: 16px;
  display: grid;
  gap: 10px;
}

.insight-list section {
  background: #f8fafc;
  border-left: 4px solid #cbd5e1;
  border-radius: 10px;
  padding: 12px;
}

.insight-list h5 {
  color: #0f172a;
  font-size: 13px;
  font-weight: 700;
}

.insight-list p {
  margin-top: 6px;
  color: #64748b;
  font-size: 12px;
  line-height: 1.6;
}

@media (max-width: 1100px) {
  .stats-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .content-grid {
    grid-template-columns: 1fr;
  }
}
</style>
