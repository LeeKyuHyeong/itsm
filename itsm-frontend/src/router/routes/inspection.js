export default [
  {
    path: '/inspections',
    name: 'InspectionList',
    component: () => import('@/views/inspection/InspectionListView.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/inspections/new',
    name: 'InspectionCreate',
    component: () => import('@/views/inspection/InspectionFormView.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/inspections/:id',
    name: 'InspectionDetail',
    component: () => import('@/views/inspection/InspectionDetailView.vue'),
    meta: { requiresAuth: true }
  }
]
