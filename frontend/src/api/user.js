import request from '@/utils/request'

export const userApi = {
  login(data) {
    return request({ url: '/user/login', method: 'post', data })
  },
  page(params) {
    return request({ url: '/user/page', method: 'get', params })
  },
  list(params) {
    return request({ url: '/user/list', method: 'get', params })
  },
  save(data) {
    return request({ url: '/user', method: 'post', data })
  },
  update(id, data) {
    return request({ url: `/user/${id}`, method: 'put', data })
  },
  delete(id) {
    return request({ url: `/user/${id}`, method: 'delete' })
  },
  resetPassword(id, newPassword) {
    return request({ url: `/user/${id}/reset-password`, method: 'put', data: { newPassword } })
  }
}
