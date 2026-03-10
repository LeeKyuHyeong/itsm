import api from './index.js'

export const incidentApi = {
  getList(params) {
    return api.get('/incidents', { params })
  },
  create(data) {
    return api.post('/incidents', data)
  },
  getDetail(id) {
    return api.get(`/incidents/${id}`)
  },
  update(id, data) {
    return api.patch(`/incidents/${id}`, data)
  },
  changeStatus(id, data) {
    return api.patch(`/incidents/${id}/status`, data)
  },
  assignUser(id, data) {
    return api.post(`/incidents/${id}/assignees`, data)
  },
  removeAssignee(id, assigneeId) {
    return api.delete(`/incidents/${id}/assignees/${assigneeId}`)
  },
  getComments(id) {
    return api.get(`/incidents/${id}/comments`)
  },
  addComment(id, data) {
    return api.post(`/incidents/${id}/comments`, data)
  },
  updateComment(id, commentId, data) {
    return api.patch(`/incidents/${id}/comments/${commentId}`, data)
  },
  deleteComment(id, commentId) {
    return api.delete(`/incidents/${id}/comments/${commentId}`)
  },
  getHistory(id) {
    return api.get(`/incidents/${id}/history`)
  },
  getReport(id) {
    return api.get(`/incidents/${id}/report`)
  },
  saveReport(id, data) {
    return api.post(`/incidents/${id}/report`, data)
  },
  updateReport(id, data) {
    return api.patch(`/incidents/${id}/report`, data)
  }
}
