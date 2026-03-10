import { ROLES } from '@/constants/roles.js'

const adminRoles = [ROLES.SUPER_ADMIN, ROLES.ITSM_ADMIN]

export default [
  {
    path: '/admin/accounts',
    name: 'AccountManage',
    component: () => import('@/views/admin/AccountManageView.vue'),
    meta: { requiresAuth: true, roles: adminRoles }
  },
  {
    path: '/admin/organizations',
    name: 'OrgManage',
    component: () => import('@/views/admin/OrgManageView.vue'),
    meta: { requiresAuth: true, roles: adminRoles }
  },
  {
    path: '/admin/menus',
    name: 'MenuManage',
    component: () => import('@/views/admin/MenuManageView.vue'),
    meta: { requiresAuth: true, roles: adminRoles }
  },
  {
    path: '/admin/common-codes',
    name: 'CommonCodeManage',
    component: () => import('@/views/admin/CommonCodeView.vue'),
    meta: { requiresAuth: true, roles: adminRoles }
  },
  {
    path: '/admin/boards',
    name: 'BoardManage',
    component: () => import('@/views/admin/BoardManageView.vue'),
    meta: { requiresAuth: true, roles: adminRoles }
  },
  {
    path: '/admin/sla',
    name: 'SlaManage',
    component: () => import('@/views/admin/SlaManageView.vue'),
    meta: { requiresAuth: true, roles: adminRoles }
  },
  {
    path: '/admin/notification-policy',
    name: 'NotificationPolicyManage',
    component: () => import('@/views/admin/NotificationPolicyView.vue'),
    meta: { requiresAuth: true, roles: adminRoles }
  }
]
