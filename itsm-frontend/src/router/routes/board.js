export default [
  {
    path: '/boards/:boardId',
    name: 'BoardList',
    component: () => import('@/views/board/BoardListView.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/boards/:boardId/posts/new',
    name: 'BoardPostCreate',
    component: () => import('@/views/board/BoardPostFormView.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/boards/:boardId/posts/:postId',
    name: 'BoardPostDetail',
    component: () => import('@/views/board/BoardPostDetailView.vue'),
    meta: { requiresAuth: true }
  }
]
