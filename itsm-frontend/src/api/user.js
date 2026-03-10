import api from './index.js'

export const userApi = {
  getList(params) {
    return api.get('/users', { params })
  },
  create(data) {
    return api.post('/users', data)
  },
  getDetail(userId) {
    return api.get(`/users/${userId}`)
  },
  update(userId, data) {
    return api.patch(`/users/${userId}`, data)
  },
  changeStatus(userId, data) {
    return api.patch(`/users/${userId}/status`, data)
  },
  getHistory(userId) {
    return api.get(`/users/${userId}/history`)
  },
  assignRole(userId, roleData) {
    return api.post(`/users/${userId}/roles`, roleData)
  },
  removeRole(userId, roleId) {
    return api.delete(`/users/${userId}/roles/${roleId}`)
  }
}
