import api from '../index.js'

export const slaApi = {
  getList(params) {
    return api.get('/admin/sla-policies', { params })
  },
  create(data) {
    return api.post('/admin/sla-policies', data)
  },
  update(id, data) {
    return api.patch(`/admin/sla-policies/${id}`, data)
  }
}
