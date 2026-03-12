import api from './index.js'

export const incidentApi = {
  getList(params) {
    return api.get('/incidents', { params })
  },
  getDetail(id) {
    return api.get(`/incidents/${id}`)
  },
  create(data) {
    return api.post('/incidents', data)
  },
  update(id, data) {
    return api.patch(`/incidents/${id}`, data)
  },
  changeStatus(id, data) {
    return api.patch(`/incidents/${id}/status`, data)
  },
  assignMainManager(id, data) {
    return api.patch(`/incidents/${id}/main-manager`, data)
  },
  getAssignees(id) {
    return api.get(`/incidents/${id}/assignees`)
  },
  assignUser(id, data) {
    return api.post(`/incidents/${id}/assignees`, data)
  },
  removeAssignee(id, assigneeId) {
    return api.delete(`/incidents/${id}/assignees/${assigneeId}`)
  },
  getAssets(id) {
    return api.get(`/incidents/${id}/assets`)
  },
  addAsset(id, data) {
    return api.post(`/incidents/${id}/assets`, data)
  },
  removeAsset(id, assetType, assetId) {
    return api.delete(`/incidents/${id}/assets/${assetType}/${assetId}`)
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
