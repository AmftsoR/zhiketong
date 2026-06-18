<script setup>
import { computed, ref } from 'vue'
import { useUserStore } from '../../stores/userStore'

const userStore = useUserStore()
const teacherName = computed(() => userStore.user?.realName || '教师')
const teacherInitial = computed(() => teacherName.value.slice(0, 1))

const sections = ref([
  {
    title: '学情诊断与AI辅助',
    items: [
      {
        key: 'neo',
        label: '错题智能归因（知识图谱）',
        desc: '自动将学生错题溯源至根本知识盲区，避免头痛医头。',
        enabled: true,
      },
      {
        key: 'profile',
        label: '薄弱点可视化学情画像',
        desc: '通过红黄绿标记学生知识点掌握度，支持分层教学。',
        enabled: true,
      },
      {
        key: 'assistant',
        label: 'AI引导式答疑助手',
        desc: '只提供解题思路引导，不直接给出完整主观题答案。',
        enabled: true,
      },
    ],
  },
  {
    title: '课堂互动与作业推送',
    items: [
      {
        key: 'realtime',
        label: '课堂答题无延迟通信',
        desc: '保障班级抢答与提交无延迟，课中互动稳定。',
        enabled: true,
      },
      {
        key: 'targeted',
        label: '靶向变式练习定时推送',
        desc: '课后自动推送 5-10 道分层练习题。',
        enabled: true,
      },
      {
        key: 'reminder',
        label: '作业逾期自动微干预',
        desc: '对超时未交作业学生触发提醒并下发基础包。',
        enabled: false,
      },
    ],
  },
  {
    title: 'AI教研报告与数据脱敏',
    items: [
      {
        key: 'weeklyReport',
        label: '自动生成AI教研报告（周/月）',
        desc: '系统自动汇总学情亮点与改课建议，减轻重复劳动。',
        enabled: true,
      },
      {
        key: 'examReport',
        label: '考前薄弱点紧急诊断报告',
        desc: '在关键节点向学生生成备考清单。',
        enabled: true,
      },
      {
        key: 'share',
        label: '校级教研数据共享授权',
        desc: '允许教研组匿名调取班级部分测验数据。',
        enabled: false,
      },
      {
        key: 'masking',
        label: '学生隐私与数据脱敏导出',
        desc: '向校外导出时自动隐去学生姓名与敏感状态。',
        enabled: true,
      },
    ],
  },
])

const saveMessage = ref('')

function toggleItem(sectionIndex, itemIndex) {
  const target = sections.value[sectionIndex].items[itemIndex]
  target.enabled = !target.enabled
}

function saveSettings() {
  saveMessage.value = `已保存 ${new Date().toLocaleTimeString('zh-CN', { hour12: false })}`
}
</script>

<template>
  <section class="page">
    <header class="head-card">
      <div class="teacher-avatar">{{ teacherInitial }}</div>
      <div>
        <h1>{{ teacherName }}</h1>
        <p>示范性高级中学 · 高三数学教研组 · 当前授课：高三（1）班、高三（2）班</p>
      </div>
      <button class="save-btn" @click="saveSettings">保存配置</button>
    </header>

    <section class="section-card" v-for="(section, sIdx) in sections" :key="section.title">
      <div class="section-head">
        <h3>{{ section.title }}</h3>
      </div>

      <div class="setting-grid">
        <div class="setting-row" v-for="(item, iIdx) in section.items" :key="item.key">
          <div>
            <h4>{{ item.label }}</h4>
            <p>{{ item.desc }}</p>
          </div>

          <button
            class="switch"
            :class="{ on: item.enabled }"
            @click="toggleItem(sIdx, iIdx)"
            :aria-label="item.enabled ? '关闭' : '开启'"
          >
            <span></span>
          </button>
        </div>
      </div>
    </section>

    <p v-if="saveMessage" class="save-msg">{{ saveMessage }}</p>
  </section>
</template>

<style scoped>
.page {
  display: grid;
  gap: 14px;
}

.head-card {
  background: #fff;
  border: 1px solid #e2e8f0;
  border-radius: 14px;
  box-shadow: 0 4px 16px rgba(15, 23, 42, 0.04);
  padding: 18px;
  display: grid;
  grid-template-columns: auto 1fr auto;
  gap: 14px;
  align-items: center;
}

.teacher-avatar {
  width: 68px;
  height: 68px;
  border-radius: 18px;
  display: grid;
  place-items: center;
  font-size: 32px;
  font-weight: 700;
  background: #eff6ff;
  color: #3b82f6;
}

.head-card h1 {
  color: #0f172a;
  font-size: 27px;
}

.head-card p {
  margin-top: 4px;
  color: #64748b;
  font-size: 13px;
}

.save-btn {
  border: 0;
  border-radius: 10px;
  background: #2563eb;
  color: #fff;
  padding: 10px 14px;
  font-weight: 700;
}

.section-card {
  background: #fff;
  border: 1px solid #e2e8f0;
  border-radius: 14px;
  box-shadow: 0 4px 16px rgba(15, 23, 42, 0.04);
}

.section-head {
  background: #f8fafc;
  border-bottom: 1px solid #e2e8f0;
  padding: 12px 16px;
}

.section-head h3 {
  color: #0f172a;
  font-size: 16px;
  font-weight: 700;
}

.setting-grid {
  display: grid;
}

.setting-row {
  padding: 14px 16px;
  border-bottom: 1px solid #eef2f7;
  display: grid;
  grid-template-columns: 1fr auto;
  gap: 8px;
  align-items: center;
}

.setting-row:last-child {
  border-bottom: 0;
}

.setting-row h4 {
  color: #1e293b;
  font-size: 14px;
  font-weight: 700;
}

.setting-row p {
  margin-top: 4px;
  color: #64748b;
  font-size: 12px;
  line-height: 1.5;
}

.switch {
  width: 46px;
  height: 24px;
  border: 0;
  border-radius: 999px;
  background: #cbd5e1;
  padding: 2px;
  transition: background-color 0.2s ease;
}

.switch span {
  display: block;
  width: 20px;
  height: 20px;
  border-radius: 50%;
  background: #fff;
  transform: translateX(0);
  transition: transform 0.2s ease;
}

.switch.on {
  background: #3b82f6;
}

.switch.on span {
  transform: translateX(22px);
}

.save-msg {
  justify-self: end;
  color: #2563eb;
  font-size: 13px;
}

@media (max-width: 980px) {
  .head-card {
    grid-template-columns: 1fr;
    justify-items: start;
  }
}
</style>
