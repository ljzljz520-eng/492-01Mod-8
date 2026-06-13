import request from '@/utils/request'

export const positionApi = {
  save(data) {
    return request({ url: '/position', method: 'post', data })
  },
  update(id, data) {
    return request({ url: `/position/${id}`, method: 'put', data })
  },
  delete(id) {
    return request({ url: `/position/${id}`, method: 'delete' })
  },
  getById(id) {
    return request({ url: `/position/${id}`, method: 'get' })
  },
  list() {
    return request({ url: '/position/list', method: 'get' })
  },
  page(params) {
    return request({ url: '/position/page', method: 'get', params })
  }
}
