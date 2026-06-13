import request from '@/utils/request'

export const scheduleApi = {
  save(data) {
    return request({ url: '/schedule', method: 'post', data })
  },
  update(id, data) {
    return request({ url: `/schedule/${id}`, method: 'put', data })
  },
  delete(id) {
    return request({ url: `/schedule/${id}`, method: 'delete' })
  },
  getById(id) {
    return request({ url: `/schedule/${id}`, method: 'get' })
  },
  page(params) {
    return request({ url: '/schedule/page', method: 'get', params })
  }
}
