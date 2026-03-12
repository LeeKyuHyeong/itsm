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
  getApprovers(id) {
    return api.get(`/changes/${id}/approvers`)
  },
  addApprover(id, data) {
    return api.post(`/changes/${id}/approvers`, data)
  },
  removeApprover(id, userId) {
    return api.delete(`/changes/${id}/approvers/${userId}`)
  },
  approve(id, userId, data) {
    return api.patch(`/changes/${id}/approvers/${userId}`, data)
  },
  getComments(id) {
    return api.get(`/changes/${id}/comments`)
  },
  addComment(id, data) {
    return api.post(`/changes/${id}/comments`, data)
  },
  updateComment(id, commentId, data) {
    return api.patch(`/changes/${id}/comments/${commentId}`, data)
  },
  deleteComment(id, commentId) {
    return api.delete(`/changes/${id}/comments/${commentId}`)
  },
  getHistory(id) {
    return api.get(`/changes/${id}/history`)
  }
}
