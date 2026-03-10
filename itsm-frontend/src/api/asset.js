import api from './index.js'

export const assetHwApi = {
  getList(params) {
    return api.get('/assets/hw', { params })
  },
  create(data) {
    return api.post('/assets/hw', data)
  },
  getDetail(id) {
    return api.get(`/assets/hw/${id}`)
  },
  update(id, data) {
    return api.patch(`/assets/hw/${id}`, data)
  },
  getHistory(id) {
    return api.get(`/assets/hw/${id}/history`)
  }
}

export const assetSwApi = {
  getList(params) {
    return api.get('/assets/sw', { params })
  },
  create(data) {
    return api.post('/assets/sw', data)
  },
  getDetail(id) {
    return api.get(`/assets/sw/${id}`)
  },
  update(id, data) {
    return api.patch(`/assets/sw/${id}`, data)
  },
  getHistory(id) {
    return api.get(`/assets/sw/${id}/history`)
  }
}
