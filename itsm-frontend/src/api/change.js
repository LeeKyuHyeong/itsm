import api from './index.js'

export const changeApi = {
  getList(params) {
    return api.get('/changes', { params })
  },
  create(data) {
    return api.post('/changes', data)
  },
  getDetail(id) {
    return api.get(`/changes/${id}`)
  },
  update(id, data) {
    return api.patch(`/changes/${id}`, data)
  },
  changeStatus(id, data) {
    return api.patch(`/changes/${id}/status`, data)
  },
  addApprover(id, data) {
    return api.post(`/changes/${id}/approvers`, data)
  },
  approve(id, approverId, data) {
    return api.patch(`/changes/${id}/approvers/${approverId}`, data)
  },
  getComments(id) {
    return api.get(`/changes/${id}/comments`)
  },
  addComment(id, data) {
    return api.post(`/changes/${id}/comments`, data)
  },
  getHistory(id) {
    return api.get(`/changes/${id}/history`)
  }
}
