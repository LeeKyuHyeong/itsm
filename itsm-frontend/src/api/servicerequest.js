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
  getAssignees(id) {
    return api.get(`/service-requests/${id}/assignees`)
  },
  assignUser(id, data) {
    return api.post(`/service-requests/${id}/assignees`, data)
  },
  removeAssignee(id, userId) {
    return api.delete(`/service-requests/${id}/assignees/${userId}`)
  },
  getProcesses(id) {
    return api.get(`/service-requests/${id}/processes`)
  },
  addProcess(id, data) {
    return api.post(`/service-requests/${id}/processes`, data)
  },
  completeProcess(id, processId) {
    return api.patch(`/service-requests/${id}/processes/${processId}/complete`)
  },
  submitSatisfaction(id, data) {
    return api.post(`/service-requests/${id}/satisfaction`, data)
  },
  getHistory(id) {
    return api.get(`/service-requests/${id}/history`)
  }
}
