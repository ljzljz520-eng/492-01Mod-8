import request from '@/utils/request'

export const heatConfigApi = {
  getAll() {
    return request({ url: '/heat-config/all', method: 'get' })
  },
  list() {
    return request({ url: '/heat-config/list', method: 'get' })
  },
  getByKey(key) {
    return request({ url: `/heat-config/${key}`, method: 'get' })
  },
  update(id, data) {
    return request({ url: `/heat-config/${id}`, method: 'put', data })
  }
}
