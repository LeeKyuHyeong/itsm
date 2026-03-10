export default [
  {
    path: '/service-requests',
    name: 'ServiceRequestList',
    component: () => import('@/views/servicerequest/ServiceRequestListView.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/service-requests/new',
    name: 'ServiceRequestCreate',
    component: () => import('@/views/servicerequest/ServiceRequestFormView.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/service-requests/:id',
    name: 'ServiceRequestDetail',
    component: () => import('@/views/servicerequest/ServiceRequestDetailView.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/service-requests/:id/edit',
    name: 'ServiceRequestEdit',
    component: () => import('@/views/servicerequest/ServiceRequestFormView.vue'),
    meta: { requiresAuth: true }
  }
]
