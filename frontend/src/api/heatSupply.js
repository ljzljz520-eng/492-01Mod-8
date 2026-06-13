import request from '@/utils/request'

export const heatSupplyApi = {
  save(data) {
    return request({ url: '/heat-supply', method: 'post', data })
  },
  update(id, data) {
    return request({ url: `/heat-supply/${id}`, method: 'put', data })
  },
  delete(id) {
    return request({ url: `/heat-supply/${id}`, method: 'delete' })
  },
  getById(id) {
    return request({ url: `/heat-supply/${id}`, method: 'get' })
  },
  list() {
    return request({ url: '/heat-supply/list', method: 'get' })
  },
  page(params) {
    return request({ url: '/heat-supply/page', method: 'get', params })
  }
}
