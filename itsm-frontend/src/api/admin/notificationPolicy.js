import api from '../index.js'

export const notificationPolicyApi = {
  getList(params) {
    return api.get('/admin/notification-policies', { params })
  },
  create(data) {
    return api.post('/admin/notification-policies', data)
  },
  update(id, data) {
    return api.patch(`/admin/notification-policies/${id}`, data)
  },
  changeStatus(id, data) {
    return api.patch(`/admin/notification-policies/${id}/status`, data)
  }
}
