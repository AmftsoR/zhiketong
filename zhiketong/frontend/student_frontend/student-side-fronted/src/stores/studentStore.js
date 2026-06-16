import { defineStore } from 'pinia'
import { fetchMistakes, createMistake as createMistakeApi, deleteMistake, submitAnswer } from '../api/mistake'

const practiceQuestionBanks = {
  math: {
    basic: [
      {
        type: '单选题',
        source: '函数定义域',
        stem: '1. 函数 <span class=”math”>f(x) = 1 / (x - 1)</span> 的定义域是 ( )',
        options: [
          { key: 'A', text: 'x > 2' },
          { key: 'B', text: 'x < 2' },
          { key: 'C', text: 'x ≠ 2' },
          { key: 'D', text: 'x ∈ R，且 x ≠ 1' },
        ],
        correctAnswer: 'D',
        analysis:
          '你选择了B，说明没有掌握分式函数的基本性质。分母不能为0，因此 x - 1 ≠ 0，解得 x ≠ 1。这属于典型的”症状错题”。',
      },
      {
        type: '单选题',
        source: '函数定义域',
        stem: '2. 函数 <span class=”math”>g(x) = 1 / (x + 2)</span> 的定义域是 ( )',
        options: [
          { key: 'A', text: 'x ≠ -2' },
          { key: 'B', text: 'x > -2' },
          { key: 'C', text: 'x < -2' },
          { key: 'D', text: 'x ∈ R' },
        ],
        correctAnswer: 'A',
        analysis: '分母不能为0，所以 x + 2 ≠ 0，得到 x ≠ -2。先看分母，再看零点，是这类题的核心步骤。',
      },
    ],
    variant: [
      {
        type: '单选题',
        source: '函数图像变换',
        stem: '1. 将函数 <span class=”math”>y = f(x)</span> 向右平移 2 个单位，再向上平移 3 个单位，得到 ( )',
        options: [
          { key: 'A', text: 'y = f(x + 2) + 3' },
          { key: 'B', text: 'y = f(x - 2) + 3' },
          { key: 'C', text: 'y = f(x + 2) - 3' },
          { key: 'D', text: 'y = f(x - 2) - 3' },
        ],
        correctAnswer: 'B',
        analysis: '向右平移对应 x - 2，向上平移对应 +3，组合后为 y = f(x - 2) + 3。',
      },
      {
        type: '单选题',
        source: '函数图像变换',
        stem: '2. 函数 <span class=”math”>y = f(x)</span> 关于 y 轴对称后，表达式变为 ( )',
        options: [
          { key: 'A', text: 'y = f(x) + 1' },
          { key: 'B', text: 'y = f(-x)' },
          { key: 'C', text: 'y = -f(x)' },
          { key: 'D', text: 'y = f(x - 1)' },
        ],
        correctAnswer: 'B',
        analysis: '关于 y 轴对称就是把自变量 x 替换成 -x。',
      },
    ],
    challenge: [
      {
        type: '单选题',
        source: '函数性质综合',
        stem: '1. 若函数 <span class=”math”>f(x)</span> 在区间上单调递增，则下列说法正确的是 ( )',
        options: [
          { key: 'A', text: 'x1 < x2 时，f(x1) > f(x2)' },
          { key: 'B', text: 'x1 < x2 时，f(x1) = f(x2)' },
          { key: 'C', text: 'x1 < x2 时，f(x1) < f(x2)' },
          { key: 'D', text: 'f(x) 与 x 无关' },
        ],
        correctAnswer: 'C',
        analysis: '单调递增表示自变量增大时，函数值也增大，因此 x1 < x2 时有 f(x1) < f(x2)。',
      },
    ],
  },
  physics: {
    basic: [
      {
        type: '单选题',
        source: '牛顿第二定律',
        stem: '1. 一物体质量 m=2kg，在光滑水平面上受到 F=10N 的恒力，加速度大小为 ( )',
        options: [
          { key: 'A', text: '2 m/s²' },
          { key: 'B', text: '5 m/s²' },
          { key: 'C', text: '10 m/s²' },
          { key: 'D', text: '20 m/s²' },
        ],
        correctAnswer: 'B',
        analysis: '根据牛顿第二定律 F=ma，a=F/m=10/2=5 m/s²。牢记公式是解题第一步。',
      },
      {
        type: '单选题',
        source: '自由落体',
        stem: '2. 物体从静止自由下落，2s 末的速度为 ( )（g=10m/s²）',
        options: [
          { key: 'A', text: '10 m/s' },
          { key: 'B', text: '15 m/s' },
          { key: 'C', text: '20 m/s' },
          { key: 'D', text: '30 m/s' },
        ],
        correctAnswer: 'C',
        analysis: '自由落体 v=gt=10×2=20 m/s。初速度为 0 时直接用 v=gt。',
      },
    ],
    variant: [
      {
        type: '单选题',
        source: '运动学公式',
        stem: '1. 一质点位移 s=2t²+3t（SI），t=2s 时瞬时速度为 ( )',
        options: [
          { key: 'A', text: '7 m/s' },
          { key: 'B', text: '11 m/s' },
          { key: 'C', text: '14 m/s' },
          { key: 'D', text: '17 m/s' },
        ],
        correctAnswer: 'B',
        analysis: 'v=ds/dt=4t+3，代入 t=2 得 v=11 m/s。导数即瞬时变化率。',
      },
      {
        type: '单选题',
        source: '牛顿第三定律',
        stem: '2. 作用力与反作用力总是 ( )',
        options: [
          { key: 'A', text: '大小相等，方向相同' },
          { key: 'B', text: '大小相等，方向相反' },
          { key: 'C', text: '大小不等，方向相反' },
          { key: 'D', text: '作用在同一物体上' },
        ],
        correctAnswer: 'B',
        analysis: '牛顿第三定律：作用力与反作用力大小相等、方向相反、作用在不同物体上。',
      },
    ],
    challenge: [
      {
        type: '单选题',
        source: '受力分析综合',
        stem: '1. 物体沿粗糙斜面匀速下滑，则斜面给物体的摩擦力方向是 ( )',
        options: [
          { key: 'A', text: '沿斜面向下' },
          { key: 'B', text: '沿斜面向上' },
          { key: 'C', text: '垂直于斜面' },
          { key: 'D', text: '竖直向上' },
        ],
        correctAnswer: 'B',
        analysis: '匀速下滑合力为 0，重力沿斜面分力向下，摩擦力必沿斜面向上与之平衡。',
      },
    ],
  },
  english: {
    basic: [
      {
        type: '单选题',
        source: '一般现在时',
        stem: '1. She ___ to school by bus every day.',
        options: [
          { key: 'A', text: 'go' },
          { key: 'B', text: 'goes' },
          { key: 'C', text: 'going' },
          { key: 'D', text: 'gone' },
        ],
        correctAnswer: 'B',
        analysis: '一般现在时第三人称单数，动词加 -s/-es。every day 提示是习惯性动作。',
      },
      {
        type: '单选题',
        source: '名词复数',
        stem: '2. The Great Wall is one of the greatest ___ (wonder) in the world.',
        options: [
          { key: 'A', text: 'wonder' },
          { key: 'B', text: 'wonders' },
          { key: 'C', text: 'wondering' },
          { key: 'D', text: 'wondered' },
        ],
        correctAnswer: 'B',
        analysis: 'one of + 复数名词，wonder 的复数为 wonders。固定搭配要记牢。',
      },
    ],
    variant: [
      {
        type: '单选题',
        source: '非谓语动词',
        stem: '1. The book ___ on the desk is mine.',
        options: [
          { key: 'A', text: 'lay' },
          { key: 'B', text: 'lying' },
          { key: 'C', text: 'lied' },
          { key: 'D', text: 'lies' },
        ],
        correctAnswer: 'B',
        analysis: '现在分词短语作后置定语，lying on the desk 修饰 the book，表主动进行。',
      },
      {
        type: '单选题',
        source: '虚拟语气',
        stem: '2. If I ___ you, I would study harder.',
        options: [
          { key: 'A', text: 'am' },
          { key: 'B', text: 'was' },
          { key: 'C', text: 'were' },
          { key: 'D', text: 'be' },
        ],
        correctAnswer: 'C',
        analysis: '与现在事实相反的虚拟语气，if 从句用 were（所有人称均用 were）。',
      },
    ],
    challenge: [
      {
        type: '单选题',
        source: '定语从句',
        stem: '1. This is the school ___ I studied three years ago.',
        options: [
          { key: 'A', text: 'which' },
          { key: 'B', text: 'that' },
          { key: 'C', text: 'where' },
          { key: 'D', text: 'when' },
        ],
        correctAnswer: 'C',
        analysis: '先行词 school 在从句中作地点状语（I studied at the school），用关系副词 where。',
      },
    ],
  },
}

const reportData = {
  current: {
    overallScore: 85,
    summary: '击败了班级 76% 的同学，保持进步！避免盲目刷题策略已见成效。',
    radarLabels: [
      { text: '思维', left: '50%', top: '3px' },
      { text: '运算', left: '95%', top: '50%' },
      { text: '应用', left: '50%', top: '92%' },
      { text: '表达', left: '4%', top: '50%' },
    ],
    radar: { mine: [82, 68, 74, 79], class: [65, 58, 60, 62] },
    chapters: [
      { name: '集合与逻辑用语', value: 92, color: '#00B894' },
      { name: '一元二次函数与方程', value: 75, color: '#6C5CE7' },
      { name: '基本初等函数', value: 45, color: '#FDCB6E' },
    ],
  },
  prev: {
    overallScore: 76,
    summary: '相比上月提升 8 分，章节薄弱点已开始收敛。',
    radarLabels: [
      { text: '思维', left: '50%', top: '3px' },
      { text: '运算', left: '95%', top: '50%' },
      { text: '应用', left: '50%', top: '92%' },
      { text: '表达', left: '4%', top: '50%' },
    ],
    radar: { mine: [72, 60, 66, 70], class: [64, 57, 59, 61] },
    chapters: [
      { name: '集合与逻辑用语', value: 84, color: '#00B894' },
      { name: '一元二次函数与方程', value: 68, color: '#6C5CE7' },
      { name: '基本初等函数', value: 39, color: '#FDCB6E' },
    ],
  },
  term: {
    overallScore: 88,
    summary: '本学期累计提升明显，建议继续保持“错题回炉 + 靶向练习”的节奏。',
    radarLabels: [
      { text: '思维', left: '50%', top: '3px' },
      { text: '运算', left: '95%', top: '50%' },
      { text: '应用', left: '50%', top: '92%' },
      { text: '表达', left: '4%', top: '50%' },
    ],
    radar: { mine: [86, 74, 78, 82], class: [68, 61, 63, 65] },
    chapters: [
      { name: '集合与逻辑用语', value: 94, color: '#00B894' },
      { name: '一元二次函数与方程', value: 78, color: '#6C5CE7' },
      { name: '基本初等函数', value: 52, color: '#FDCB6E' },
    ],
  },
}

const assistantSeedMessages = [
  {
    id: 'student-1',
    role: 'student',
    avatar: '👦',
    lines: ['老师，我还是不明白这道题为什么选D，函数的定义域该怎么找？'],
  },
  {
    id: 'assistant-1',
    role: 'assistant',
    avatar: 'AI',
    lines: [
      '别着急，我们一起看。题目是 f(x) = 1/(x-1)。',
      '对于分式函数，你还记得它的分母需要满足什么条件吗？',
      '回忆一下我们课上讲的分数性质。',
    ],
    quickActions: [
      { key: 'course', label: '查看课件' },
      { key: 'knowledge', label: '复习知识点' },
    ],
    showHint: true,
    hintTitle: '提示',
    hintText: '先想一想分母什么时候会失去意义。',
  },
  { id: 'student-2', role: 'student', avatar: '👦', lines: ['分母有什么限制条件吗？'] },
  { id: 'assistant-2', role: 'assistant', avatar: 'AI', lines: ['哦！分母不能等于0！'] },
  {
    id: 'assistant-3',
    role: 'assistant',
    avatar: 'AI',
    lines: [
      '既然分母 (x-1) 不能等于 0，那么 x 应该满足什么条件呢？试着自己解一下这个不等式吧。',
      '完全正确！👏',
    ],
    quickActions: [
      { key: 'course', label: '查看课件' },
      { key: 'knowledge', label: '复习知识点' },
    ],
    showHint: true,
    hintTitle: '继续思考',
    hintText: '把“不能为 0”转化成一个具体的不等式。',
  },
]

export const useStudentStore = defineStore('student', {
  state: () => ({
    profile: {
      name: '张同学',
      className: '高一 (1) 班',
      studentId: '20230910',
      avatar: 'https://placehold.co/52x52',
      studyDays: 128,
      points: 3450,
      badges: 42,
    },
    dashboard: {
      studyMinutes: 35,
      practiceTask: {
        subject: '高一数学 - 集合与函数',
        completed: 3,
        total: 10,
      },
      metrics: [
        { label: '错题数', value: 32, unit: '道' },
        { label: '未完成练习', value: 8, unit: '道' },
        { label: '掌握知识点', value: 68, unit: '个' },
      ],
      recentMistakes: [
        { title: '函数的定义域求解不完整', meta: '数学-函数 | 随堂测验' },
        { title: '牛顿第二定律公式误用', meta: '物理-动力学 | 课后作业' },
      ],
      shortcuts: [
        { label: '错题本', shortLabel: '错', path: '/wrong-book', color: 'linear-gradient(135deg, #c7d2fe, #ddd6fe)' },
        { label: '薄弱点图谱', shortLabel: '薄', path: '/weakness-map', color: 'linear-gradient(135deg, #dbeafe, #c4b5fd)' },
        { label: '练习推送', shortLabel: '练', path: '/target-practice', color: 'linear-gradient(135deg, #d1fae5, #bfdbfe)' },
        { label: '学情报告', shortLabel: '报', path: '/analysis-report', color: 'linear-gradient(135deg, #fde68a, #fca5a5)' },
      ],
    },
    practice: {
      activeMode: 'basic',
      currentQuestionIndex: 0,
      selectedAnswer: 'B',
      showResult: true,
      progress: { current: 5, total: 10, estimatedMinutes: 15 },
      modes: [
        { key: 'basic', label: '基础巩固' },
        { key: 'variant', label: '变式提升' },
        { key: 'challenge', label: '拔高压轴' },
      ],
      tips: [
        { key: 'basic', label: '基础巩固' },
        { key: 'variant', label: '变式提升' },
        { key: 'challenge', label: '拔高压轴' },
      ],
      questionBanks: practiceQuestionBanks,
    },
    wrongBook: {
      selectedSubject: 'math',
      selectedType: 'all',
      selectedView: 'all',
      subjectOptions: [
        { key: 'math', label: '数学' },
        { key: 'physics', label: '物理' },
        { key: 'english', label: '英语' },
      ],
      typeOptions: [
        { key: 'all', label: '全部题型' },
        { key: 'single', label: '单选题' },
        { key: 'fill', label: '填空题' },
      ],
      mistakes: [],
      openedAnalysisId: '',
    },
    favorites: [
      {
        id: 'fav-1',
        subject: 'math',
        type: 'single',
        stem: '已知集合 A = {x | x² - 3x + 2 = 0}，则集合 A 的真子集个数是 ( )',
        correctAnswer: 'C',
        analysis: '集合 A = {1, 2}，共 2 个元素，真子集个数为 2² - 1 = 3',
        date: '06-09 14:30',
      },
      {
        id: 'fav-2',
        subject: 'physics',
        type: 'single',
        stem: '一物体做匀加速直线运动，初速度为 2m/s，加速度为 1m/s²，则 3s 末的速度为 ( )',
        correctAnswer: 'D',
        analysis: 'v = v₀ + at = 2 + 1 × 3 = 5 m/s',
        date: '06-08 10:15',
      },
    ],
    analysis: {
      selectedMonthKey: 'current',
      monthOptions: [
        { key: 'current', label: '本月' },
        { key: 'prev', label: '上月' },
        { key: 'term', label: '本学期' },
      ],
      reportData,
      showMonthMenu: false,
    },
    assistant: {
      messages: assistantSeedMessages,
      inputPlaceholder: '输入你的思考过程...',
      draftMessage: '',
    },
    weaknessMap: {
      selectedSubjectKey: 'math',
      activeTab: 'mastery',
      selectedNodeId: 'tri',
      subjects: [
        { key: 'math', label: '数学' },
        { key: 'physics', label: '物理' },
        { key: 'english', label: '英语' },
      ],
    },
  }),
  getters: {
    taskProgress(state) {
      return Math.round((state.dashboard.practiceTask.completed / state.dashboard.practiceTask.total) * 100)
    },
    currentPracticeQuestion(state) {
      const subject = state.weaknessMap.selectedSubjectKey || 'math'
      const bank = state.practice.questionBanks[subject]
      const list = (bank && bank[state.practice.activeMode]) || []
      return list[state.practice.currentQuestionIndex] || list[0]
    },
    currentPracticeQuestions(state) {
      const subject = state.weaknessMap.selectedSubjectKey || 'math'
      const bank = state.practice.questionBanks[subject]
      return (bank && bank[state.practice.activeMode]) || []
    },
    filteredMistakes(state) {
      return state.wrongBook.mistakes.filter((item) => {
        const subjectMatch = item.subject === state.wrongBook.selectedSubject
        const typeMatch = state.wrongBook.selectedType === 'all' || item.type === state.wrongBook.selectedType
        return subjectMatch && typeMatch
      })
    },
    currentReport(state) {
      return state.analysis.reportData[state.analysis.selectedMonthKey]
    },
  },
  actions: {
    continuePractice() {
      if (this.dashboard.practiceTask.completed < this.dashboard.practiceTask.total) {
        this.dashboard.practiceTask.completed += 1
        this.dashboard.studyMinutes += 2
      }
    },
    resetPractice() {
      this.dashboard.practiceTask.completed = 3
      this.dashboard.studyMinutes = 35
    },
    selectPracticeMode(mode) {
      this.practice.activeMode = mode
      this.practice.currentQuestionIndex = 0
      this.practice.selectedAnswer = 'B'
      this.practice.showResult = true
    },
    pickPracticeAnswer(answer) {
      this.practice.selectedAnswer = answer
      this.practice.showResult = true

      // 调用后端提交答案
      const q = this.currentPracticeQuestion
      if (q && q.id) {
        this.submitPracticeAnswer(q.id, answer).then(result => {
          if (result) {
            this.practice.lastSubmitResult = result
          }
        })
      }
    },
    nextPracticeQuestion() {
      const questions = this.currentPracticeQuestions
      if (!questions.length) {
        return
      }

      if (this.practice.currentQuestionIndex < questions.length - 1) {
        this.practice.currentQuestionIndex += 1
      } else {
        this.practice.currentQuestionIndex = 0
      }

      const nextQuestion = questions[this.practice.currentQuestionIndex]
      this.practice.selectedAnswer = nextQuestion.correctAnswer === 'D' ? 'B' : 'A'
      this.practice.showResult = true
      this.practice.progress.current = Math.min(this.practice.progress.total, this.practice.progress.current + 1)
    },
    setWrongBookSubject(subject) {
      this.wrongBook.selectedSubject = subject
    },
    /** 学习界面切换学科 */
    selectStudySubject(subjectKey) {
      this.weaknessMap.selectedSubjectKey = subjectKey
      const labels = { math: '高一数学 - 集合与函数', physics: '高一物理 - 力学基础', english: '高一英语 - 语法与阅读' }
      this.dashboard.practiceTask.subject = labels[subjectKey] || labels.math
      // 切换学科时重置练习进度
      this.practice.currentQuestionIndex = 0
      this.practice.activeMode = 'basic'
      this.practice.selectedAnswer = 'B'
    },
    setWrongBookType(type) {
      this.wrongBook.selectedType = type
    },
    toggleWrongBookView(view) {
      this.wrongBook.selectedView = this.wrongBook.selectedView === view ? 'all' : view
    },
    setOpenedAnalysisId(id) {
      this.wrongBook.openedAnalysisId = id
    },
    setReportMonth(key) {
      this.analysis.selectedMonthKey = key
      this.analysis.showMonthMenu = false
    },
    toggleReportMonthMenu() {
      this.analysis.showMonthMenu = !this.analysis.showMonthMenu
    },
    clearAssistantChat() {
      this.assistant.messages = assistantSeedMessages.slice(0, 1)
      this.assistant.draftMessage = ''
      this.assistant.inputPlaceholder = '输入你的思考过程...'
    },
    appendAssistantDraft(draftMessage) {
      this.assistant.draftMessage = draftMessage
    },
    setAssistantPlaceholder(placeholder) {
      this.assistant.inputPlaceholder = placeholder
    },
    sendAssistantMessage(message) {
      const content = message || this.assistant.draftMessage || '我想先确认一下：是不是要先找分母为 0 的情况？'

      this.assistant.messages = [
        ...this.assistant.messages,
        {
          id: `student-${Date.now()}`,
          role: 'student',
          avatar: '👦',
          lines: [content],
        },
        {
          id: `assistant-${Date.now()}`,
          role: 'assistant',
          avatar: 'AI',
          lines: ['很好，你已经找到关键点了。接下来把 x-1 ≠ 0 解出来，再回到定义域的表达方式。'],
          quickActions: [
            { key: 'course', label: '查看课件' },
            { key: 'knowledge', label: '复习知识点' },
          ],
          showHint: true,
          hintTitle: '下一步',
          hintText: '把不等式解出来后，再写成集合或区间的形式。',
        },
      ]

      this.assistant.draftMessage = ''
      this.assistant.inputPlaceholder = '继续说出你的思考过程...'
    },

    // ========== 真实 API 对接 — 错题本 ==========

    /** 从后端加载错题列表 */
    async loadMistakes() {
      try {
        const res = await fetchMistakes()
        // 后端返回 { code: 200, message: "success", data: [...] }
        const list = (res && res.data) || []
        this.wrongBook.mistakes = list.map(item => this._mapMistakeToCard(item))
      } catch (e) {
        console.error('加载错题失败:', e)
      }
    },

    /** 手动添加错题 */
    async addMistakeEntry(formData) {
      try {
        await createMistakeApi(formData)
        // 添加成功后重新加载错题列表
        await this.loadMistakes()
        return true
      } catch (e) {
        console.error('添加错题失败:', e)
        return false
      }
    },

    /** 删除错题 */
    async removeMistake(questionId) {
      try {
        await deleteMistake(questionId)
        this.wrongBook.mistakes = this.wrongBook.mistakes.filter(m => m.questionId !== questionId)
      } catch (e) {
        console.error('删除错题失败:', e)
      }
    },

    /** 取消收藏 */
    removeFavorite(id) {
      this.favorites = this.favorites.filter(f => f.id !== id)
    },

    /** 提交练习答案 */
    async submitPracticeAnswer(questionId, userAnswer) {
      try {
        const res = await submitAnswer({ questionId, userAnswer })
        return (res && res.data) || null
      } catch (e) {
        console.error('提交答案失败:', e)
        return null
      }
    },

    /** 将后端返回的 MistakeVO 映射为前端卡片格式 */
    _mapMistakeToCard(item) {
      const levelMap = { easy: 'weak', medium: 'medium', hard: 'weak' }
      const labelMap = { easy: '未掌握', medium: '基本掌握', hard: '未掌握' }

      // 格式化日期
      let dateStr = ''
      if (item.addedAt) {
        const d = new Date(item.addedAt)
        dateStr = `${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')} 收录`
      }

      // type 映射: single_choice → single
      const typeMap = { single_choice: 'single', multi_choice: 'multi', fill: 'fill' }

      return {
        id: `mistake-${item.id}`,
        questionId: item.questionId,
        subject: item.subject || 'math',
        type: typeMap[item.type] || 'single',
        level: levelMap[item.difficulty] || 'medium',
        levelLabel: labelMap[item.difficulty] || '基本掌握',
        date: dateStr,
        stem: item.stem || '',
        root: item.knowledgePoint || '',
        wrongCount: item.wrongCount || 1,
        myAnswer: item.myAnswer || '',
        correctAnswer: item.correctAnswer || '',
        analysis: item.analysis || '暂未提供解析',
        options: item.options || '[]',
      }
    },
  },
})