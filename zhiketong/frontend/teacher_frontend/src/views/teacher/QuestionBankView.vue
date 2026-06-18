<script setup>
import { computed, nextTick, onMounted, ref, watch } from 'vue'
import { fetchQuestions, createQuestion, updateQuestion, deleteQuestion, importQuestions } from '../../api/questions'
import { fetchKnowledgeTree } from '../../api/knowledge'
import { request } from '../../utils/request'

// ---- 状态 ----
const loading = ref(false)
const questions = ref([])
const total = ref(0)
const page = ref(1)
const pageSize = ref(10)
const search = ref('')
const filterType = ref('')
const filterDifficulty = ref('')
const filterKpId = ref(null)
const knowledgePoints = ref([])

// 创建/编辑弹窗
const showModal = ref(false)
const editTarget = ref(null)
const form = ref({ type: 'single_choice', title: '', difficulty: 'medium', knowledgePointId: null, options: '', answer: '', explanation: '' })
const formError = ref('')
const formSubmitting = ref(false)
const uploadingImage = ref(false)

// AI 生成知识点
const showAiKp = ref(false)
const aiKpSubject = ref('math')
const aiKpTopic = ref('')
const aiKpLoading = ref(false)
const aiKpError = ref('')

async function generateKpByAI() {
  aiKpError.value = ''; aiKpLoading.value = true
  try {
    const r = await request('/knowledge-points/generate', { method: 'POST', body: { subject: aiKpSubject.value, topic: aiKpTopic.value || aiKpSubject.value } })
    if (r?.code === 200) { showAiKp.value = false; loadKp() }
    else aiKpError.value = r?.message || '生成失败'
  } catch (e) { aiKpError.value = e.message || 'AI调用失败' }
  finally { aiKpLoading.value = false }
}

// 查看详情
const showDetail = ref(false)
const detailItem = ref(null)

// Excel
const importFile = ref(null)
const importName = ref('')
const importing = ref(false)

// 统计
const stats = computed(() => [
  { label: '题库总量', value: total.value, hint: '后端实时统计' },
  { label: '当前页', value: page.value, hint: `每页 ${pageSize.value} 条` },
])

// ---- 加载 ----
async function load() {
  loading.value = true
  try {
    const res = await fetchQuestions({
      page: page.value, size: pageSize.value,
      keyword: search.value || undefined,
      type: filterType.value || undefined,
      difficulty: filterDifficulty.value || undefined,
      knowledgePointId: filterKpId.value || undefined,
    })
    if (res?.code === 200 && res.data) { questions.value = res.data.records || []; total.value = res.data.total || 0 }
  } catch (e) { console.error(e) } finally { loading.value = false }
}

function findKpInTree(tree, id) {
  for (const node of tree) {
    if (node.id === id) return node
    if (node.children && node.children.length) {
      const found = findKpInTree(node.children, id)
      if (found) return found
    }
  }
  return null
}

async function loadKp() {
  try {
    const r = await fetchKnowledgeTree()
    if (r && r.code === 200) knowledgePoints.value = r.data || []
  } catch (e) { console.error('知识点加载异常:', e) }
}

watch([page, filterType, filterDifficulty, filterKpId], () => load())
function onSearch() { page.value = 1; load() }

// ---- 图片粘贴 ----
async function onPaste(e, field) {
  const items = e.clipboardData?.items; if (!items) return
  for (const item of items) {
    if (!item.type.startsWith('image/')) continue
    e.preventDefault(); uploadingImage.value = true
    try {
      const blob = item.getAsFile()
      const base64 = await new Promise(r => { const rd = new FileReader(); rd.onload = () => r(rd.result); rd.readAsDataURL(blob) })
      const res = await request('/upload/base64', { method: 'POST', body: { base64 } })
      if (res?.code === 200 && res.data?.url) {
        const md = `![](${res.data.url})`
        const el = e.target; const s = el.selectionStart, end = el.selectionEnd
        form.value[field] = (form.value[field] || '').slice(0, s) + md + (form.value[field] || '').slice(end)
        await nextTick(); el.focus(); el.setSelectionRange(s + md.length, s + md.length)
      }
    } catch (err) { alert('图片上传失败: ' + err.message) } finally { uploadingImage.value = false }
    break
  }
}

// ---- CRUD ----
function openCreate() {
  editTarget.value = null
  form.value = { type: 'single_choice', title: '', difficulty: 'medium', knowledgePointId: filterKpId.value || null, options: '', answer: '', explanation: '' }
  formError.value = ''; formSubmitting.value = false; showModal.value = true
}
function openEdit(item) {
  editTarget.value = item
  form.value = {
    type: item.type || 'single_choice', title: item.title || '',
    difficulty: item.difficulty || 'medium', knowledgePointId: item.knowledgePointId || null,
    options: typeof item.options === 'string' ? item.options : JSON.stringify(item.options || []),
    answer: typeof item.answer === 'string' ? item.answer : JSON.stringify(item.answer || ''),
    explanation: item.explanation || '',
  }
  formError.value = ''; formSubmitting.value = false; showModal.value = true
}
async function handleSubmit() {
  formError.value = ''
  if (!form.value.title.trim()) { formError.value = '题干不能为空'; return }
  if (!form.value.answer.trim()) { formError.value = '正确答案不能为空'; return }
  formSubmitting.value = true
  try {
    const payload = { type: form.value.type, title: form.value.title.trim(), difficulty: form.value.difficulty, knowledgePointId: form.value.knowledgePointId, options: form.value.options || '[]', answer: form.value.answer.trim(), explanation: form.value.explanation || '' }
    editTarget.value ? await updateQuestion(editTarget.value.id, payload) : await createQuestion(payload)
    showModal.value = false; load()
  } catch (e) { formError.value = e.message || '操作失败' } finally { formSubmitting.value = false }
}
async function handleDelete(item) {
  if (!confirm(`删除「${item.title.slice(0, 30)}」？`)) return
  try { await deleteQuestion(item.id); load() } catch (e) { alert('删除失败: ' + e.message) }
}

// ---- Excel ----
function onFileChange(e) { const f = e.target.files?.[0]; importFile.value = f || null; importName.value = f ? f.name : '' }
async function handleImport() {
  if (!importFile.value) return; importing.value = true
  try { await importQuestions(importFile.value); alert('导入成功'); importFile.value = null; importName.value = ''; load() }
  catch (e) { alert('导入失败: ' + e.message) } finally { importing.value = false }
}

// ---- 详情 ----
function openDetail(item) { detailItem.value = item; showDetail.value = true }

// ---- 工具 ----
function diffLabel(d) { return d === 'easy' ? '基础' : d === 'hard' ? '困难' : '中等' }
function diffClass(d) { return d === 'easy' ? 'diff-easy' : d === 'hard' ? 'diff-hard' : 'diff-mid' }
function typeLabel(t) { const m = { single_choice: '单选', multi_choice: '多选', true_false: '判断', fill_blank: '填空', short_answer: '简答' }; return m[t] || t || '-' }
function kpName(id) { if (!id) return '未归类'; const f = findKpInTree(knowledgePoints.value, id); return f ? f.name : '#' + id }
function flatKpList(tree) { const r = []; for (const n of tree) { r.push(n); if (n.children) r.push(...flatKpList(n.children)) } return r }
function renderMd(t) { if (!t) return ''; return t.replace(/!\[([^\]]*)\]\(([^)]+)\)/g, '<img src="$2" alt="$1" style="max-width:100%;border-radius:8px;margin:4px 0">').replace(/\n/g, '<br>') }
function fmtJson(s) { try { const a = typeof s === 'string' ? JSON.parse(s) : s; return Array.isArray(a) ? a.join('\n') : String(s) } catch { return String(s) } }

onMounted(() => { loadKp(); load() })
</script>

<template>
  <section class="page">
    <div class="page-head">
      <h1>题库管理</h1>
      <div class="actions">
        <input v-model="search" placeholder="搜索题目 / 知识点 / 章节" @keyup.enter="onSearch" />
        <button @click="onSearch">搜索</button>
        <button class="btn-primary" @click="openCreate">+ 新建题目</button>
      </div>
    </div>

    <div class="stats-grid">
      <article v-for="s in stats" :key="s.label" class="stat-card">
        <p>{{ s.label }}</p><h3>{{ s.value }}</h3><small>{{ s.hint }}</small>
      </article>
      <article class="stat-card">
        <p>筛选</p>
        <div style="display:flex;gap:8px;margin-top:4px;flex-wrap:wrap">
          <select v-model="filterType" class="mini-select"><option value="">全部题型</option><option v-for="t in ['single_choice','multi_choice','true_false','fill_blank','short_answer']" :key="t" :value="t">{{ typeLabel(t) }}</option></select>
          <select v-model="filterDifficulty" class="mini-select"><option value="">全部难度</option><option value="easy">基础</option><option value="medium">中等</option><option value="hard">困难</option></select>
          <button class="btn-ai" @click="showAiKp=true" title="AI 生成知识点">🤖 AI知识点</button>
        </div>
      </article>
    </div>

    <div class="content-grid">
      <article class="card table-card">
        <div class="table-head"><h3>题目列表 ({{ total }})</h3></div>
        <div class="table-wrap">
          <table v-if="!loading && questions.length">
            <thead><tr><th>#</th><th>题目</th><th>知识点</th><th>题型</th><th>难度</th><th>操作</th></tr></thead>
            <tbody>
              <tr v-for="(q, idx) in questions" :key="q.id" class="q-row" @click="openDetail(q)">
                <td>{{ (page-1)*pageSize + idx + 1 }}</td><td class="td-title">{{ q.title }}</td><td>{{ kpName(q.knowledgePointId) }}</td><td>{{ typeLabel(q.type) }}</td>
                <td><span class="diff" :class="diffClass(q.difficulty)">{{ diffLabel(q.difficulty) }}</span></td>
                <td class="op" @click.stop>
                  <button @click="openEdit(q)">编辑</button>
                  <button class="btn-danger" @click="handleDelete(q)">删除</button>
                </td>
              </tr>
            </tbody>
          </table>
          <p v-else-if="loading" class="empty">加载中...</p>
          <p v-else class="empty">暂无题目，点击"新建题目"添加</p>
        </div>
        <div class="pager" v-if="total > pageSize">
          <button :disabled="page<=1" @click="page--">上一页</button>
          <span>{{ page }} / {{ Math.ceil(total/pageSize) }}</span>
          <button :disabled="page>=Math.ceil(total/pageSize)" @click="page++">下一页</button>
        </div>
      </article>

      <article class="card import-card">
        <h3>批量导入题库</h3>
        <label class="upload-box">
          <div class="icon">上传</div>
          <p class="title">点击上传 Excel 文件</p>
          <p class="desc">格式: 知识点ID | 题型 | 难度 | 题干 | 选项(JSON) | 正确答案 | 解析</p>
          <span class="btn">选择文件</span>
          <input type="file" accept=".xlsx,.xls" @change="onFileChange" />
        </label>
        <p v-if="importName" class="file-name">已选择：{{ importName }}</p>
        <button v-if="importFile" class="btn-primary" style="width:100%;margin-top:8px" :disabled="importing" @click="handleImport">{{ importing ? '导入中...' : '确认导入' }}</button>
      </article>
    </div>

    <!-- 新建/编辑弹窗 -->
    <Teleport to="body">
      <div v-if="showModal" class="modal-mask" @click.self="showModal=false">
        <div class="modal-card">
          <div class="modal-head"><h3>{{ editTarget ? '编辑' : '新建' }}题目</h3><button class="modal-close" @click="showModal=false">&times;</button></div>
          <form class="modal-form" @submit.prevent="handleSubmit">
            <div class="form-group"><label>题干 * <small v-if="uploadingImage" style="color:#6366f1">上传中...</small></label><textarea v-model="form.title" class="form-input form-area" placeholder="题干内容，可直接 Ctrl+V 粘贴图片" required @paste="onPaste($event,'title')"></textarea></div>
            <div class="form-row-3">
              <div class="form-group"><label>题型</label><select v-model="form.type" class="form-input"><option value="single_choice">单选</option><option value="multi_choice">多选</option><option value="true_false">判断</option><option value="fill_blank">填空</option><option value="short_answer">简答</option></select></div>
              <div class="form-group"><label>难度</label><select v-model="form.difficulty" class="form-input"><option value="easy">基础</option><option value="medium">中等</option><option value="hard">困难</option></select></div>
              <div class="form-group"><label>知识点</label><select v-model="form.knowledgePointId" class="form-input"><option :value="null">不归类</option><option v-for="kp in flatKpList(knowledgePoints)" :key="kp.id" :value="kp.id">{{ kp.name }}</option></select></div>
            </div>
            <div class="form-group"><label>选项 (JSON)</label><input v-model="form.options" class="form-input" placeholder='["A. 选项1","B. 选项2"]' /></div>
            <div class="form-group"><label>正确答案 *</label><input v-model="form.answer" class="form-input" placeholder='["B"]' /></div>
            <div class="form-group"><label>解析</label><textarea v-model="form.explanation" class="form-input form-area" placeholder="解析（可粘贴图片）" @paste="onPaste($event,'explanation')"></textarea></div>
            <p v-if="formError" class="form-err">{{ formError }}</p>
            <div class="modal-actions"><button type="button" class="btn-cancel" @click="showModal=false">取消</button><button type="submit" class="btn-primary" :disabled="formSubmitting">{{ formSubmitting ? '提交中...' : '保存' }}</button></div>
          </form>
        </div>
      </div>
    </Teleport>

    <!-- 详情弹窗 -->
    <Teleport to="body">
      <div v-if="showDetail && detailItem" class="modal-mask" @click.self="showDetail=false">
        <div class="detail-modal">
          <div class="modal-head"><h3>题目详情 #{{ detailItem.id }}</h3><button class="modal-close" @click="showDetail=false">&times;</button></div>
          <div class="detail-body">
            <div class="detail-row"><span class="detail-label">题型</span><span>{{ typeLabel(detailItem.type) }}</span></div>
            <div class="detail-row"><span class="detail-label">难度</span><span class="diff" :class="diffClass(detailItem.difficulty)">{{ diffLabel(detailItem.difficulty) }}</span></div>
            <div class="detail-row"><span class="detail-label">知识点</span><span>{{ kpName(detailItem.knowledgePointId) }}</span></div>
            <div class="detail-section"><h4>题干</h4><div class="detail-content" v-html="renderMd(detailItem.title||'')"></div></div>
            <div class="detail-section" v-if="detailItem.options && detailItem.options!=='[]'"><h4>选项</h4><div class="detail-content" v-html="renderMd(fmtJson(detailItem.options))"></div></div>
            <div class="detail-section"><h4>正确答案</h4><div class="detail-content answer-box" v-html="renderMd(fmtJson(detailItem.answer))"></div></div>
            <div class="detail-section" v-if="detailItem.explanation"><h4>解析</h4><div class="detail-content" v-html="renderMd(detailItem.explanation||'')"></div></div>
          </div>
          <div class="modal-actions" style="margin-top:16px"><button class="btn-cancel" @click="showDetail=false">关闭</button><button class="btn-primary" @click="showDetail=false;openEdit(detailItem)">编辑</button></div>
        </div>
      </div>
    </Teleport>

    <!-- AI 生成知识点弹窗 -->
    <Teleport to="body">
      <div v-if="showAiKp" class="modal-mask" @click.self="showAiKp=false">
        <div class="modal-card" style="max-width:400px">
          <div class="modal-head"><h3>🤖 AI 生成知识点</h3><button class="modal-close" @click="showAiKp=false">&times;</button></div>
          <div class="modal-form">
            <div class="form-group"><label>学科</label><select v-model="aiKpSubject" class="form-input"><option value="math">数学</option><option value="physics">物理</option><option value="english">英语</option><option value="chinese">语文</option></select></div>
            <div class="form-group"><label>主题（可选）</label><input v-model="aiKpTopic" class="form-input" placeholder="如：高等数学、力学" /></div>
            <p style="font-size:12px;color:#9ca3af">将调用 DeepSeek 根据学科生成知识点树，自动存入数据库。</p>
            <p v-if="aiKpError" class="form-err">{{ aiKpError }}</p>
            <div class="modal-actions"><button class="btn-cancel" @click="showAiKp=false">取消</button><button class="btn-primary" :disabled="aiKpLoading" @click="generateKpByAI">{{ aiKpLoading ? '生成中...' : '生成' }}</button></div>
          </div>
        </div>
      </div>
    </Teleport>
  </section>
</template>

<style scoped>
.page{display:grid;gap:16px}
.page-head{display:flex;align-items:center;justify-content:space-between;gap:10px;flex-wrap:wrap}
.page-head h1{color:#1f2937;font-size:30px;margin:0}
.actions{display:flex;gap:10px;align-items:center}
.actions input{width:240px;border:1px solid #e5e7eb;border-radius:10px;padding:10px 12px;font-size:14px}
.actions button{border:0;border-radius:10px;padding:10px 16px;background:#f3f4f6;color:#374151;font-weight:600;cursor:pointer;white-space:nowrap}
.btn-primary{background:linear-gradient(135deg,#6366f1,#4f46e5)!important;color:#fff!important}
.btn-danger{color:#dc2626!important}
.stats-grid{display:grid;grid-template-columns:repeat(4,minmax(0,1fr));gap:12px}
.stat-card{background:#fff;border:1px solid #e2e8f0;border-radius:14px;box-shadow:0 4px 14px rgba(15,23,42,.04);padding:16px}
.stat-card p{color:#6b7280;font-size:13px;margin:0}
.stat-card h3{margin-top:8px;color:#111827;font-size:29px}
.stat-card small{display:block;margin-top:8px;color:#10b981;font-size:12px}
.mini-select{border:1px solid #e5e7eb;border-radius:8px;padding:6px 8px;font-size:12px}
.btn-ai{border:1px dashed #6366f1;border-radius:8px;padding:6px 12px;font-size:12px;background:#fff;color:#6366f1;cursor:pointer;white-space:nowrap}
.btn-ai:hover{background:#eef2ff}
.content-grid{display:grid;grid-template-columns:2fr 1fr;gap:12px}
.card{background:#fff;border:1px solid #e2e8f0;border-radius:14px;box-shadow:0 4px 14px rgba(15,23,42,.04);padding:16px}
.table-head{margin-bottom:8px}
.table-head h3,.import-card h3{color:#1f2937;font-size:17px;font-weight:700;margin:0}
.table-wrap{overflow:auto;max-height:460px}
table{width:100%;border-collapse:collapse;min-width:700px}
th,td{text-align:left;padding:10px 12px;border-bottom:1px solid #f3f4f6;font-size:13px}
th{color:#6b7280;background:#f9fafb;position:sticky;top:0}
.td-title{max-width:200px;overflow:hidden;text-overflow:ellipsis;white-space:nowrap}
.q-row{cursor:pointer;transition:background .15s}
.q-row:hover{background:#f8faff}
.diff{border-radius:999px;padding:4px 8px;font-size:12px;font-weight:700}
.diff-easy{background:#dcfce7;color:#16a34a}
.diff-mid{background:#fef3c7;color:#d97706}
.diff-hard{background:#fee2e2;color:#dc2626}
.op{display:flex;gap:6px}
.op button{border:0;background:#f3f4f6;border-radius:6px;padding:4px 10px;font-size:12px;cursor:pointer}
.empty{text-align:center;color:#9ca3af;padding:40px 0}
.pager{display:flex;justify-content:center;align-items:center;gap:12px;margin-top:12px}
.pager button{border:1px solid #e5e7eb;background:#fff;border-radius:8px;padding:6px 14px;cursor:pointer}
.pager button:disabled{opacity:.4;cursor:default}
.import-card{display:grid;align-content:start;gap:12px}
.upload-box{background:#f8faff;border:1px dashed #c7d2fe;border-radius:14px;min-height:200px;display:grid;place-items:center;text-align:center;gap:8px;padding:18px;cursor:pointer}
.upload-box .icon{width:60px;height:60px;border-radius:50%;display:grid;place-items:center;background:#e0e7ff;color:#4338ca;font-weight:700}
.upload-box .title{color:#1f2937;font-weight:700;margin:0}
.upload-box .desc{color:#6b7280;font-size:12px;margin:0}
.upload-box .btn{margin-top:4px;border-radius:8px;padding:8px 12px;background:#4f46e5;color:#fff;font-size:12px}
.upload-box input{display:none}
.file-name{color:#475569;font-size:12px}
/* 弹窗 */
.modal-mask{position:fixed;inset:0;background:rgba(0,0,0,.45);z-index:9999;display:grid;place-items:center;padding:20px}
.modal-card{width:100%;max-width:560px;max-height:85vh;overflow-y:auto;background:#fff;border-radius:14px;padding:24px;box-shadow:0 12px 40px rgba(0,0,0,.18)}
.modal-head{display:flex;justify-content:space-between;align-items:center;margin-bottom:18px}
.modal-head h3{margin:0;font-size:1.1rem}
.modal-close{width:30px;height:30px;border:0;background:#f5f5f5;border-radius:50%;font-size:1.2rem;cursor:pointer}
.modal-form{display:grid;gap:14px}
.form-group{display:grid;gap:6px}
.form-group label{font-size:13px;font-weight:600;color:#555}
.form-input{width:100%;border:1px solid #e0e0e0;border-radius:8px;padding:8px 10px;font-size:13px;box-sizing:border-box}
.form-area{height:80px;resize:vertical}
.form-row-3{display:grid;grid-template-columns:repeat(3,1fr);gap:12px}
.form-err{color:#dc2626;font-size:13px;text-align:center;margin:0;padding:8px;background:#fef2f2;border-radius:6px}
.modal-actions{display:flex;gap:12px;justify-content:flex-end}
.btn-cancel{border:0;border-radius:8px;padding:8px 20px;background:#f3f4f6;color:#6b7280;cursor:pointer}
/* 详情 */
.detail-modal{width:100%;max-width:660px;max-height:85vh;overflow-y:auto;background:#fff;border-radius:14px;padding:24px;box-shadow:0 12px 40px rgba(0,0,0,.18)}
.detail-body{display:grid;gap:12px}
.detail-row{display:flex;align-items:center;gap:10px;font-size:13px}
.detail-label{font-weight:600;color:#6b7280;min-width:60px}
.detail-section{margin-top:4px}
.detail-section h4{margin:0 0 6px;font-size:13px;color:#6b7280;font-weight:600}
.detail-content{padding:10px 14px;background:#f9fafb;border-radius:8px;font-size:14px;line-height:1.7;color:#1f2937;word-break:break-word}
.detail-content :deep(img){max-width:100%;border-radius:8px;margin:6px 0}
.answer-box{background:#f0fdf4;border:1px solid #bbf7d0}
@media(max-width:1100px){.stats-grid{grid-template-columns:repeat(2,1fr)}.content-grid{grid-template-columns:1fr}.form-row-3{grid-template-columns:1fr}}
</style>
