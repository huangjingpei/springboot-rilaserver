import { createRouter, createWebHistory } from 'vue-router'
import Login from '../views/Login.vue'
import ControlPanel from '../views/ControlPanel.vue'
import SessionHistory from '../views/SessionHistory.vue'
import ActivityLog from '../views/ActivityLog.vue'

const routes = [
  { path: '/', redirect: '/login' },
  { path: '/login', component: Login },
  { path: '/control', component: ControlPanel },
  { path: '/sessions', component: SessionHistory },
  { path: '/logs', component: ActivityLog }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router
