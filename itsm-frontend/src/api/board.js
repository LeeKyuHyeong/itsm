import api from './index.js'

export const boardApi = {
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
    return api.patch(`/boards/${boardId}/posts/${postId}`, data)
  },
  deletePost(boardId, postId) {
    return api.delete(`/boards/${boardId}/posts/${postId}`)
  },
  getComments(boardId, postId) {
    return api.get(`/boards/${boardId}/posts/${postId}/comments`)
  },
  addComment(boardId, postId, data) {
    return api.post(`/boards/${boardId}/posts/${postId}/comments`, data)
  }
}
