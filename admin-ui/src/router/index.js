import { createRouter, createWebHistory } from 'vue-router';
import Layout from '../layout/Layout.vue';
import Login from '../views/Login.vue';
import Dashboard from '../views/Dashboard.vue';
import UserManagement from '../views/UserManagement.vue';
import AppManagement from '../views/AppManagement.vue';
import StreamManagement from '../views/StreamManagement.vue';
import NetworkTest from '../views/NetworkTest.vue';
import SystemSettings from '../views/SystemSettings.vue';

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: Login,
    meta: { hideInMenu: true }
  },
  {
    path: '/',
    component: Layout,
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: Dashboard,
        meta: { title: '仪表盘', icon: 'dashboard' }
      },
      {
          path: 'users',
          name: 'UserManagement',
          component: UserManagement,
          meta: { title: '用户管理', icon: 'user' }
        },
        {
          path: 'apps',
          name: 'AppManagement',
          component: AppManagement,
          meta: { title: '应用管理', icon: 'app' }
        },
        {
          path: 'streams',
          name: 'StreamManagement',
          component: StreamManagement,
          meta: { title: '流媒体管理', icon: 'video' }
        },
        {
          path: 'network',
          name: 'NetworkTest',
          component: NetworkTest,
          meta: { title: '网络测试', icon: 'connection' }
        },
        {
          path: 'settings',
          name: 'SystemSettings',
          component: SystemSettings,
          meta: { title: '系统设置', icon: 'setting' }
        }
    ]
  }
];

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes,
});

// 路由守卫
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token');
  
  if (to.path === '/login') {
    if (token) {
      next('/dashboard');
    } else {
      next();
    }
  } else {
    if (token) {
      next();
    } else {
      next('/login');
    }
  }
});

export default router;