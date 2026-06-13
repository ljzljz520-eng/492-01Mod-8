import request from '@/utils/request'

export const workHourApi = {
  settleMonth(settleMonth) {
    return request({ url: '/work-hour/settle-month', method: 'post', params: { settleMonth } })
  },
  settleWorker(settleMonth, workerId) {
    return request({ url: '/work-hour/settle-worker', method: 'post', params: { settleMonth, workerId } })
  },
  update(id, data) {
    return request({ url: `/work-hour/${id}`, method: 'put', data })
  },
  delete(id) {
    return request({ url: `/work-hour/${id}`, method: 'delete' })
  },
  getById(id) {
    return request({ url: `/work-hour/${id}`, method: 'get' })
  },
  page(params) {
    return request({ url: '/work-hour/page', method: 'get', params })
  }
}
