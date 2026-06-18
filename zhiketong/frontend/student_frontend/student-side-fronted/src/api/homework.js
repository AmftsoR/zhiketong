import { request } from '../utils/request'

export function fetchMyHomework(className) {
  return request(`/homeworks/my?className=${encodeURIComponent(className)}`)
}
