import { request } from '../utils/request'

/** 获取收藏列表 */
export function fetchFavorites() {
  return request('/favorites')
}

/** 添加收藏 */
export function addFavorite(questionId) {
  return request('/favorites', {
    method: 'POST',
    body: { questionId },
  })
}

/** 取消收藏 */
export function removeFavoriteApi(questionId) {
  return request(`/favorites/${questionId}`, { method: 'DELETE' })
}
