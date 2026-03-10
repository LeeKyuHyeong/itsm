import { createRouter, createWebHistory } from 'vue-router'
import { setupGuards } from './guards.js'
import authRoutes from './routes/auth.js'
import dashboardRoutes from './routes/dashboard.js'
import incidentRoutes from './routes/incident.js'
import serviceRequestRoutes from './routes/servicerequest.js'
import changeRoutes from './routes/change.js'
import assetRoutes from './routes/asset.js'
import inspectionRoutes from './routes/inspection.js'
import boardRoutes from './routes/board.js'
import reportRoutes from './routes/report.js'
import adminRoutes from './routes/admin.js'

const routes = [
  {
    path: '/',
    name: 'Home',
    redirect: '/dashboard'
  },
  ...authRoutes,
  ...dashboardRoutes,
  ...incidentRoutes,
  ...serviceRequestRoutes,
  ...changeRoutes,
  ...assetRoutes,
  ...inspectionRoutes,
  ...boardRoutes,
  ...reportRoutes,
  ...adminRoutes
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

setupGuards(router)

export default router
