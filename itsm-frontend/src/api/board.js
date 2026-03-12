import api from './index.js'

export const boardApi = {
  // Admin - Board Config
  getConfigs() {
    return api.get('/admin/boards')
  },
  createConfig(data) {
    return api.post('/admin/boards', data)
  },
  updateConfig(boardId, data) {
    return api.put(`/admin/boards/${boardId}`, data)
  },
  deleteConfig(boardId) {
    return api.delete(`/admin/boards/${boardId}`)
  },

  // Posts
  getPosts(boardId, params) {
    return api.get(`/boards/${boardId}/posts`, { params })
  },
  createPost(boardId, data) {
    return api.post(`/boards/${boardId}/posts`, data)
  },
  getPost(boardId, postId) {
    return api.get(`/boards/${boardId}/posts/${postId}`)
  },
  updatePost(boardId, postId, data) {
    return api.put(`/boards/${boardId}/posts/${postId}`, data)
  },
  deletePost(boardId, postId) {
    return api.delete(`/boards/${boardId}/posts/${postId}`)
  },

  // Comments
  getComments(boardId, postId) {
    return api.get(`/boards/${boardId}/posts/${postId}/comments`)
  },
  addComment(boardId, postId, data) {
    return api.post(`/boards/${boardId}/posts/${postId}/comments`, data)
  },
  updateComment(boardId, postId, commentId, data) {
    return api.put(`/boards/${boardId}/posts/${postId}/comments/${commentId}`, data)
  },
  deleteComment(boardId, postId, commentId) {
    return api.delete(`/boards/${boardId}/posts/${postId}/comments/${commentId}`)
  }
}
