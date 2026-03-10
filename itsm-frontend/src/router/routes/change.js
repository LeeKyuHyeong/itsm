export default [
  {
    path: '/changes',
    name: 'ChangeList',
    component: () => import('@/views/change/ChangeListView.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/changes/new',
    name: 'ChangeCreate',
    component: () => import('@/views/change/ChangeFormView.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/changes/:id',
    name: 'ChangeDetail',
    component: () => import('@/views/change/ChangeDetailView.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/changes/:id/edit',
    name: 'ChangeEdit',
    component: () => import('@/views/change/ChangeFormView.vue'),
    meta: { requiresAuth: true }
  }
]
