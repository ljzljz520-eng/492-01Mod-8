import request from '@/utils/request'

export const supplyRecordApi = {
  confirm(data) {
    return request({ url: '/supply-record/confirm', method: 'post', data })
  },
  delete(id) {
    return request({ url: `/supply-record/${id}`, method: 'delete' })
  },
  getById(id) {
    return request({ url: `/supply-record/${id}`, method: 'get' })
  },
  page(params) {
    return request({ url: '/supply-record/page', method: 'get', params })
  }
}
