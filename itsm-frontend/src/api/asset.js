import api from './index.js'

export const assetHwApi = {
  getList(params) {
    return api.get('/assets/hw', { params })
  },
  getDetail(id) {
    return api.get(`/assets/hw/${id}`)
  },
  create(data) {
    return api.post('/assets/hw', data)
  },
  update(id, data) {
    return api.patch(`/assets/hw/${id}`, data)
  },
  changeStatus(id, status) {
    return api.patch(`/assets/hw/${id}/status`, { status })
  },
  getHistory(id) {
    return api.get(`/assets/hw/${id}/history`)
  },
  getRelations(id) {
    return api.get(`/assets/hw/${id}/relations`)
  },
  addRelation(data) {
    return api.post('/assets/hw/relations', data)
  },
  removeRelation(assetHwId, assetSwId) {
    return api.delete(`/assets/hw/${assetHwId}/relations/${assetSwId}`)
  }
}

export const assetSwApi = {
  getList(params) {
    return api.get('/assets/sw', { params })
  },
  getDetail(id) {
    return api.get(`/assets/sw/${id}`)
  },
  create(data) {
    return api.post('/assets/sw', data)
  },
  update(id, data) {
    return api.patch(`/assets/sw/${id}`, data)
  },
  changeStatus(id, status) {
    return api.patch(`/assets/sw/${id}/status`, { status })
  },
  getHistory(id) {
    return api.get(`/assets/sw/${id}/history`)
  }
}

export const assetStatApi = {
  getStats() {
    return api.get('/assets/stats')
  }
}
