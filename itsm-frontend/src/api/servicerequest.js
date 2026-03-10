import api from './index.js'

export const serviceRequestApi = {
  getList(params) {
    return api.get('/service-requests', { params })
  },
  create(data) {
    return api.post('/service-requests', data)
  },
  getDetail(id) {
    return api.get(`/service-requests/${id}`)
  },
  update(id, data) {
    return api.patch(`/service-requests/${id}`, data)
  },
  changeStatus(id, data) {
    return api.patch(`/service-requests/${id}/status`, data)
  },
  assignUser(id, data) {
    return api.post(`/service-requests/${id}/assignees`, data)
  },
  removeAssignee(id, assigneeId) {
    return api.delete(`/service-requests/${id}/assignees/${assigneeId}`)
  },
  submitSatisfaction(id, data) {
    return api.post(`/service-requests/${id}/satisfaction`, data)
  },
  getHistory(id) {
    return api.get(`/service-requests/${id}/history`)
  }
}
