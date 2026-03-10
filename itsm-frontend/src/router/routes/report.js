export default [
  {
    path: '/reports',
    name: 'ReportList',
    component: () => import('@/views/report/ReportListView.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/reports/:id',
    name: 'ReportDetail',
    component: () => import('@/views/report/ReportDetailView.vue'),
    meta: { requiresAuth: true }
  }
]
