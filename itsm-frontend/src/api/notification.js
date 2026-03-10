import api from './index.js'

export const notificationApi = {
  getList(params) {
    return api.get('/notifications', { params })
  },
  markAsRead(id) {
    return api.patch(`/notifications/${id}/read`)
  },
  markAllAsRead() {
    return api.patch('/notifications/read-all')
  }
}
