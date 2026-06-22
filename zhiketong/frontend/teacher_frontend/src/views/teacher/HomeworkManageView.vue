<script setup>
import { computed, onMounted, ref } from 'vue'
import { fetchHomeworkList, createHomework, getHomework, deleteHomework, publishHomework } from '../../api/homework'
import { fetchQuestions } from '../../api/questions'
import { fetchKnowledgeTree } from '../../api/knowledge'
import { fetchTeacherClasses } from '../../api/class'
import { useUserStore } from '../../stores/userStore'

const userStore = useUserStore()
const teacherId = computed(() => (userStore.user && userStore.user.role === 'teacher') ? userStore.user.id : 3)

const loading = ref(true)
const homeworks = ref([])
const hwTotal = ref(0)

const showCreate = ref(false)
const allQuestions = ref([])
const knowledgePoints = ref([])
const classOptions = ref([])
const form = ref({ title: '', difficulty: 'medium', className: '', questionIds: [], deadline: '' })
const formSearch = ref('')
const qFilterDiff = ref('')
const qFilterKpId = ref(null)
const formError = ref('')
const formSubmitting = ref(false)

const showDetail = ref(false)
const detail = ref(null)

const autoKpIds = computed(() => {
  const ids = []
  const seen = {}
  for (const q of allQuestions.value) {
    if (form.value.questionIds.includes(q.id) && q.knowledgePointId && !seen[q.knowledgePointId]) {
      seen[q.knowledgePointId] = true; ids.push(q.knowledgePointId)
    }
  }
  return ids
})

const filteredQuestions = computed(() => {
  let list = allQuestions.value || []
  if (formSearch.value.trim()) { const kw = formSearch.value.trim().toLowerCase(); list = list.filter(q => q.title && q.title.toLowerCase().includes(kw)) }
  if (qFilterDiff.value) list = list.filter(q => q.difficulty === qFilterDiff.value)
  if (qFilterKpId.value) list = list.filter(q => q.knowledgePointId === qFilterKpId.value)
  return list
})

function findKpInTree(tree, id) { if (!tree) return null; for (const n of tree) { if (n.id === id) return n; if (n.children && n.children.length) { const f = findKpInTree(n.children, id); if (f) return f } } return null }
function flatKpList(tree) { const r = []; if (!tree) return r; for (const n of tree) { r.push(n); if (n.children) r.push(...flatKpList(n.children)) } return r }
function parseIds(v) { if (!v) return []; if (Array.isArray(v)) return v; try { return JSON.parse(v) || [] } catch { return [] } }
function kpName(id) { if (!id) return '-'; const f = findKpInTree(knowledgePoints.value, id); return f ? f.name : '#' + id }
function diffLabel(d) { return d === 'easy' ? '基础' : d === 'hard' ? '困难' : '中等' }
function typeLabel(t) { if (!t) return '-'; return t === 'single_choice' || t === 'single' ? '单选' : t === 'multi_choice' || t === 'multi' ? '多选' : t === 'true_false' ? '判断' : t === 'fill_blank' ? '填空' : t === 'short_answer' ? '简答' : t }
function toggleQ(id) { const i = form.value.questionIds.indexOf(id); if (i >= 0) form.value.questionIds.splice(i, 1); else form.value.questionIds.push(id) }

async function load() {
  loading.value = true
  try {
    const r = await fetchHomeworkList({ teacherId: teacherId.value, page: 1, size: 50 })
    if (r && r.code === 200 && r.data) { homeworks.value = r.data.records || []; hwTotal.value = r.data.total || 0 }
    else { homeworks.value = [] }
  } catch (e) { console.error(e); homeworks.value = [] } finally { loading.value = false }
}

async function loadFormData() {
  try {
    const qr = await fetchQuestions({ page: 1, size: 500 })
    console.log('题库加载:', qr)
    if (qr && qr.code === 200) { allQuestions.value = qr.data.records || []; console.log('题库数量:', allQuestions.value.length) }
    else { console.error('题库加载失败:', qr) }
  } catch (e) { console.error('题库异常:', e) }

  try {
    const kr = await fetchKnowledgeTree()
    if (kr && kr.code === 200) knowledgePoints.value = kr.data || []
  } catch (e) { console.error('知识点异常:', e) }

  try {
    const cls = await fetchTeacherClasses(teacherId.value)
    if (cls && cls.data && cls.data.length) classOptions.value = cls.data.map(function(c) { return c.name || c.className || '' }).filter(Boolean)
  } catch (e) { console.error('班级异常:', e) }
  if (!classOptions.value.length) classOptions.value = ['高三(1)班', '高三(2)班', '高二(1)班']
}

function openCreate() {
  form.value = { title: '', difficulty: 'medium', className: classOptions.value[0] || '高三(1)班', questionIds: [], deadline: '' }
  formSearch.value = ''; qFilterDiff.value = ''; qFilterKpId.value = null; formError.value = ''; formSubmitting.value = false
  loadFormData(); showCreate.value = true
}

async function handleCreate() {
  formError.value = ''
  if (!form.value.title.trim()) { formError.value = '标题不能为空'; return }
  if (!form.value.questionIds.length) { formError.value = '请至少选择一道题目'; return }
  if (!form.value.className) { formError.value = '请选择班级'; return }
  formSubmitting.value = true
  try {
    await createHomework({
      teacherId: teacherId.value, title: form.value.title.trim(), difficulty: form.value.difficulty,
      className: form.value.className, knowledgePointIds: autoKpIds.value, questionIds: form.value.questionIds,
      studentIds: null, deadline: form.value.deadline || null, description: ''
    })
    showCreate.value = false; load()
  } catch (e) { formError.value = e.message || '创建失败' } finally { formSubmitting.value = false }
}

async function openDetail(id) { try { const r = await getHomework(id); if (r && r.data) { detail.value = r.data; showDetail.value = true } } catch {} }
async function handlePublish(item) { try { await publishHomework(item.id); load() } catch (e) { alert('发布失败: ' + (e.message || '')) } }
async function handleDelete(item) { if (!confirm('删除 ' + (item.title || '此作业') + '？')) return; try { await deleteHomework(item.id); load() } catch {} }

onMounted(function() { load(); loadFormData() })
</script>

<template>
  <section class="page">
    <div class="page-head"><h1>作业管理</h1><button class="btn-primary" @click="openCreate">+ 新建作业</button></div>

    <div class="card">
      <div class="table-head"><h3>作业列表 ({{ hwTotal }})</h3></div>
      <div class="table-wrap">
        <table v-if="!loading && homeworks.length">
          <thead><tr><th>标题</th><th>班级</th><th>难度</th><th>状态</th><th>截止</th><th>操作</th></tr></thead>
          <tbody>
            <tr v-for="h in homeworks" :key="h.id" style="cursor:pointer" @click="openDetail(h.id)">
              <td class="td-title">{{ h.title || '-' }}</td>
              <td>{{ h.className || '-' }}</td>
              <td><span class="diff-tag" :class="'diff-' + (h.difficulty || 'medium')">{{ diffLabel(h.difficulty) }}</span></td>
              <td><span :style="{color:h.status==='published'?'#16a34a':'#d97706'}">{{ h.status==='published'?'已发布':'草稿' }}</span></td>
              <td>{{ h.deadline || '-' }}</td>
              <td class="op" @click.stop>
                <button v-if="h.status!=='published'" class="btn-pub" @click="handlePublish(h)">发布</button>
                <button @click="openDetail(h.id)">详情</button>
                <button class="btn-danger" @click="handleDelete(h)">删除</button></td>
            </tr>
          </tbody>
        </table>
        <p v-else-if="loading" class="empty">加载中...</p>
        <p v-else class="empty">暂无作业，点击"+ 新建作业"创建</p>
      </div>
    </div>

    <Teleport to="body">
      <div v-if="showCreate" class="modal-mask" @click.self="showCreate=false">
        <div class="modal-card">
          <div class="modal-head"><h3>新建分层作业</h3><button class="modal-close" @click="showCreate=false">&times;</button></div>
          <form class="modal-form" @submit.prevent="handleCreate">
            <div class="form-row-3">
              <div class="form-group"><label>标题 *</label><input v-model="form.title" class="fi" placeholder="如：函数章节练习" required /></div>
              <div class="form-group"><label>难度</label><select v-model="form.difficulty" class="fi"><option value="easy">基础</option><option value="medium">中等</option><option value="hard">困难</option></select></div>
              <div class="form-group"><label>截止时间</label><input v-model="form.deadline" type="datetime-local" class="fi" /></div>
            </div>
            <div class="form-group">
              <label>推送班级 *</label>
              <select v-if="classOptions.length" v-model="form.className" class="fi"><option v-for="c in classOptions" :key="c" :value="c">{{ c }}</option></select>
              <input v-else v-model="form.className" class="fi" placeholder="如：高三(1)班" />
            </div>
            <div class="form-group">
              <label>关联知识点 <small style="font-weight:400;color:#9ca3af">（选题后自动生成）</small></label>
              <div v-if="autoKpIds.length" class="chip-row"><span v-for="id in autoKpIds" :key="id" class="chip on">{{ kpName(id) }}</span></div>
              <p v-else style="color:#9ca3af;font-size:12px;margin:0">请在下方选择题库题目</p>
            </div>
            <div class="form-group">
              <label>选择题库题目 * <small style="font-weight:400;color:#9ca3af">（已选 {{ form.questionIds.length }} 道）</small></label>
              <div class="q-toolbar">
                <input v-model="formSearch" class="fi" placeholder="搜索题目..." style="flex:2" />
                <select v-model="qFilterDiff" class="fi" style="flex:1"><option value="">全部难度</option><option value="easy">基础</option><option value="medium">中等</option><option value="hard">困难</option></select>
                <select v-model="qFilterKpId" class="fi" style="flex:1"><option :value="null">全部知识点</option><option v-for="kp in flatKpList(knowledgePoints)" :key="kp.id" :value="kp.id">{{ kp.name }}</option></select>
              </div>
              <div class="q-list">
                <label v-for="q in filteredQuestions" :key="q.id" class="q-row" :class="{ 'on': form.questionIds.includes(q.id) }">
                  <input type="checkbox" :checked="form.questionIds.includes(q.id)" @change="toggleQ(q.id)" />
                  <span class="q-title">{{ q.title || '' }}</span>
                  <span class="q-tag" :class="'q-' + (q.difficulty || 'medium')">{{ diffLabel(q.difficulty) }}</span>
                  <span class="q-tag q-type">{{ typeLabel(q.type) }}</span>
                </label>
                <p v-if="!filteredQuestions.length" style="text-align:center;color:#9ca3af;padding:16px;font-size:13px">没有匹配的题目</p>
              </div>
            </div>
            <p v-if="formError" class="form-err">{{ formError }}</p>
            <div class="modal-actions"><button type="button" class="btn-cancel" @click="showCreate=false">取消</button><button type="submit" class="btn-primary" :disabled="formSubmitting">{{ formSubmitting ? '创建中...' : '创建作业' }}</button></div>
          </form>
        </div>
      </div>
    </Teleport>

    <Teleport to="body">
      <div v-if="showDetail && detail" class="modal-mask" @click.self="showDetail=false">
        <div class="detail-card">
          <div class="modal-head"><h3>{{ detail.title || '作业详情' }}</h3><button class="modal-close" @click="showDetail=false">&times;</button></div>
          <div class="detail-grid">
            <div><span class="dl">班级</span>{{ detail.className || '-' }}</div>
            <div><span class="dl">难度</span>{{ diffLabel(detail.difficulty) }}</div>
            <div><span class="dl">截止</span>{{ detail.deadline || '无' }}</div>
            <div><span class="dl">知识点</span>{{ parseIds(detail.knowledgePointIds).map(kpName).join(', ') || '-' }}</div>
            <div><span class="dl">题目数</span>{{ Array.isArray(detail.questionIds) ? detail.questionIds.length : 0 }} 道</div>
          </div>
        </div>
      </div>
    </Teleport>
  </section>
</template>

<style scoped>
.page{display:grid;gap:16px}
.page-head{display:flex;justify-content:space-between;align-items:center}
.page-head h1{color:#1f2937;font-size:30px;margin:0}
.btn-primary{border:0;border-radius:10px;padding:10px 18px;background:linear-gradient(135deg,#6366f1,#4f46e5);color:#fff;font-weight:600;cursor:pointer}
.btn-danger{color:#dc2626!important}
.btn-pub{border:0;background:#dcfce7;color:#16a34a;border-radius:6px;padding:4px 10px;font-size:12px;cursor:pointer}
.card{background:#fff;border:1px solid #e2e8f0;border-radius:14px;box-shadow:0 4px 14px rgba(15,23,42,.04);padding:16px}
.table-head{margin-bottom:8px}
.table-head h3{color:#1f2937;font-size:17px;font-weight:700;margin:0}
.table-wrap{overflow:auto;max-height:500px}
table{width:100%;border-collapse:collapse;min-width:600px}
th,td{text-align:left;padding:10px 12px;border-bottom:1px solid #f3f4f6;font-size:13px}
th{color:#6b7280;background:#f9fafb;position:sticky;top:0}
.td-title{max-width:220px;overflow:hidden;text-overflow:ellipsis;white-space:nowrap}
.diff-tag{border-radius:999px;padding:3px 8px;font-size:11px;font-weight:700}
.diff-easy{background:#dcfce7;color:#16a34a}
.diff-medium{background:#fef3c7;color:#d97706}
.diff-hard{background:#fee2e2;color:#dc2626}
.op{display:flex;gap:6px}
.op button{border:0;background:#f3f4f6;border-radius:6px;padding:4px 10px;font-size:12px;cursor:pointer}
.empty{text-align:center;color:#9ca3af;padding:40px 0}
/* modal */
.modal-mask{position:fixed;inset:0;background:rgba(0,0,0,.45);z-index:9999;display:grid;place-items:center;padding:20px}
.modal-card{width:100%;max-width:700px;max-height:90vh;overflow-y:auto;background:#fff;border-radius:14px;padding:24px;box-shadow:0 12px 40px rgba(0,0,0,.18)}
.modal-head{display:flex;justify-content:space-between;align-items:center;margin-bottom:18px}
.modal-head h3{margin:0;font-size:1.1rem}
.modal-close{width:30px;height:30px;border:0;background:#f5f5f5;border-radius:50%;font-size:1.2rem;cursor:pointer;line-height:1}
.modal-form{display:grid;gap:14px}
.form-group{display:grid;gap:6px}
.form-group label{font-size:13px;font-weight:600;color:#555}
.fi{width:100%;border:1px solid #e0e0e0;border-radius:8px;padding:8px 10px;font-size:13px;box-sizing:border-box}
.form-row-3{display:grid;grid-template-columns:1fr 1fr 1fr;gap:12px}
.form-err{color:#dc2626;font-size:13px;text-align:center;margin:0;padding:8px;background:#fef2f2;border-radius:6px}
.modal-actions{display:flex;gap:12px;justify-content:flex-end;margin-top:4px}
.btn-cancel{border:0;border-radius:8px;padding:8px 20px;background:#f3f4f6;color:#6b7280;cursor:pointer}
.chip-row{display:flex;flex-wrap:wrap;gap:8px}
.chip{border:1px solid #e0e0e0;border-radius:8px;padding:6px 12px;font-size:12px;background:#fff;color:#666}
.chip.on{border-color:#6366f1;background:#eef2ff;color:#4338ca;font-weight:600}
/* question picker */
.q-toolbar{display:flex;gap:8px;margin-bottom:6px}
.q-list{border:1px solid #e5e7eb;border-radius:8px;max-height:300px;overflow-y:auto}
.q-row{display:flex;align-items:center;gap:8px;padding:8px 10px;border-bottom:1px solid #f3f4f6;cursor:pointer;font-size:13px}
.q-row:nth-child(even){background:#fafafa}
.q-row:hover{background:#f5f3ff}
.q-row.on{background:#eef2ff!important}
.q-title{flex:1;overflow:hidden;text-overflow:ellipsis;white-space:nowrap}
.q-tag{border-radius:4px;padding:2px 6px;font-size:10px;font-weight:700;white-space:nowrap}
.q-easy{background:#dcfce7;color:#16a34a}
.q-medium{background:#fef3c7;color:#d97706}
.q-hard{background:#fee2e2;color:#dc2626}
.q-type{background:#f3f4f6;color:#9ca3af;font-weight:400}
/* detail */
.detail-card{width:100%;max-width:420px;max-height:80vh;overflow-y:auto;background:#fff;border-radius:14px;padding:24px;box-shadow:0 12px 40px rgba(0,0,0,.18)}
.detail-grid{display:grid;gap:10px}
.detail-grid div{font-size:13px}
.dl{color:#6b7280;margin-right:8px;font-weight:600}
@media(max-width:768px){.form-row-3{grid-template-columns:1fr}.q-toolbar{flex-direction:column}}
</style>
