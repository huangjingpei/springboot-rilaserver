import { createRouter, createWebHistory } from 'vue-router'
import Login from '../views/Login.vue'
import LoginRegister from '../views/LoginRegister.vue'
import LoginAuth from '../views/LoginAuth.vue'
import LoginEnterprise from '../views/LoginEnterprise.vue'
import ControlPanel from '../views/ControlPanel.vue'
import SessionHistory from '../views/SessionHistory.vue'
import ActivityLog from '../views/ActivityLog.vue'

const routes = [
  { path: '/', redirect: '/login' },
  { path: '/login', component: Login },
  { path: '/login/register', component: LoginRegister },
  { path: '/login/auth', component: LoginAuth },
  { path: '/login/enterprise', component: LoginEnterprise },
  { path: '/control', component: ControlPanel },
  { path: '/sessions', component: SessionHistory },
  { path: '/logs', component: ActivityLog }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router
