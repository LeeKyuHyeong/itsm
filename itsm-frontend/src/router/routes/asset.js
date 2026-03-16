export default [
  {
    path: '/assets',
    name: 'AssetList',
    component: () => import('@/views/asset/AssetListView.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/assets/hw',
    name: 'AssetHwList',
    component: () => import('@/views/asset/AssetHwListView.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/assets/hw/:id',
    name: 'AssetHwDetail',
    component: () => import('@/views/asset/AssetHwDetailView.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/assets/sw',
    name: 'AssetSwList',
    component: () => import('@/views/asset/AssetSwListView.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/assets/sw/:id',
    name: 'AssetSwDetail',
    component: () => import('@/views/asset/AssetSwDetailView.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/assets/oa',
    name: 'AssetOaList',
    component: () => import('@/views/asset/AssetOaListView.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/assets/oa/:id',
    name: 'AssetOaDetail',
    component: () => import('@/views/asset/AssetOaDetailView.vue'),
    meta: { requiresAuth: true }
  }
]
