import api from '../index.js'

export const commonCodeApi = {
  getGroups(params) {
    return api.get('/admin/common-codes', { params })
  },
  createGroup(data) {
    return api.post('/admin/common-codes', data)
  },
  updateGroup(codeId, data) {
    return api.patch(`/admin/common-codes/${codeId}`, data)
  },
  getDetails(codeId) {
    return api.get(`/admin/common-codes/${codeId}/details`)
  },
  createDetail(codeId, data) {
    return api.post(`/admin/common-codes/${codeId}/details`, data)
  },
  updateDetail(codeId, detailId, data) {
    return api.patch(`/admin/common-codes/${codeId}/details/${detailId}`, data)
  }
}
