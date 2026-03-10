export default [
  {
    path: '/incidents',
    name: 'IncidentList',
    component: () => import('@/views/incident/IncidentListView.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/incidents/new',
    name: 'IncidentCreate',
    component: () => import('@/views/incident/IncidentFormView.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/incidents/:id',
    name: 'IncidentDetail',
    component: () => import('@/views/incident/IncidentDetailView.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/incidents/:id/edit',
    name: 'IncidentEdit',
    component: () => import('@/views/incident/IncidentFormView.vue'),
    meta: { requiresAuth: true }
  }
]
