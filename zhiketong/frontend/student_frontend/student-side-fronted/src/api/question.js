import { request } from '../utils/request'

// ====== 临时 mock 开关 ======
// 等后端 GET /api/questions/random 实现后，将 USE_MOCK 改为 false
const USE_MOCK = true

// ====== Mock 数据 ======
const mockQuestions = {
  basic: [
    {
      id: 101,
      type: '单选题',
      source: '函数定义域',
      stem: '函数 <span class="math">f(x) = 1 / (x - 1)</span> 的定义域是 ( )',
      options: [
        { key: 'A', text: 'x > 2' },
        { key: 'B', text: 'x < 2' },
        { key: 'C', text: 'x ≠ 2' },
        { key: 'D', text: 'x ∈ R，且 x ≠ 1' },
      ],
    },
    {
      id: 102,
      type: '单选题',
      source: '函数定义域',
      stem: '函数 <span class="math">g(x) = 1 / (x + 2)</span> 的定义域是 ( )',
      options: [
        { key: 'A', text: 'x ≠ -2' },
        { key: 'B', text: 'x > -2' },
        { key: 'C', text: 'x < -2' },
        { key: 'D', text: 'x ∈ R' },
      ],
    },
  ],
  variant: [
    {
      id: 201,
      type: '单选题',
      source: '函数图像变换',
      stem: '将函数 <span class="math">y = f(x)</span> 向右平移 2 个单位，再向上平移 3 个单位，得到 ( )',
      options: [
        { key: 'A', text: 'y = f(x + 2) + 3' },
        { key: 'B', text: 'y = f(x - 2) + 3' },
        { key: 'C', text: 'y = f(x + 2) - 3' },
        { key: 'D', text: 'y = f(x - 2) - 3' },
      ],
    },
  ],
  challenge: [
    {
      id: 301,
      type: '单选题',
      source: '函数性质综合',
      stem: '若函数 <span class="math">f(x)</span> 在区间上单调递增，则下列说法正确的是 ( )',
      options: [
        { key: 'A', text: 'x1 < x2 时，f(x1) > f(x2)' },
        { key: 'B', text: 'x1 < x2 时，f(x1) = f(x2)' },
        { key: 'C', text: 'x1 < x2 时，f(x1) < f(x2)' },
        { key: 'D', text: 'f(x) 与 x 无关' },
      ],
    },
  ],
}

/**
 * 获取随机题目
 * @param {string} mode - basic | variant | challenge
 * @returns {Promise<Object>} 一道随机题目
 */
export function fetchRandomQuestion(mode = 'basic') {
  if (USE_MOCK) {
    const list = mockQuestions[mode] || mockQuestions.basic
    const idx = Math.floor(Math.random() * list.length)
    return Promise.resolve(list[idx])
  }
  return request(`/questions/random?mode=${mode}`)
}
