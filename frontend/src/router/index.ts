import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'
import { useUserStore } from '@/store/user'

const staticRoutes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/Login.vue')
  },
  {
    path: '/',
    component: () => import('../layout/Layout.vue'),
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('../views/Dashboard.vue'),
        meta: { title: '数据看板', icon: 'Odometer' }
      }
    ]
  }
]

export const dynamicRoutes: RouteRecordRaw[] = [
  {
    path: '/animal',
    name: 'Animal',
    component: () => import('../layout/Layout.vue'),
    meta: { title: '动物管理', roles: ['ROLE_ADMIN', 'ROLE_RANCHER', 'ROLE_VET'] },
    children: [
      {
        path: 'list',
        name: 'AnimalList',
        component: () => import('../views/animal/AnimalList.vue'),
        meta: { title: '动物档案', icon: 'List' }
      }
    ]
  },
  {
    path: '/feeding',
    name: 'Feeding',
    component: () => import('../layout/Layout.vue'),
    meta: { title: '饲养管理', roles: ['ROLE_ADMIN', 'ROLE_BREEDER'] },
    children: [
      {
        path: 'plan',
        name: 'FeedingPlan',
        component: () => import('../views/feeding/FeedingPlan.vue'),
        meta: { title: '饲养计划', icon: 'Clock' }
      }
    ]
  },
  {
    path: '/inventory',
    name: 'Inventory',
    component: () => import('../layout/Layout.vue'),
    meta: { title: '库存管理', roles: ['ROLE_ADMIN', 'ROLE_VET', 'ROLE_BREEDER'] },
    children: [
      {
        path: 'list',
        name: 'InventoryList',
        component: () => import('../views/inventory/InventoryList.vue'),
        meta: { title: '库存列表', icon: 'Box' }
      }
    ]
  },
  {
    path: '/disease',
    name: 'Disease',
    component: () => import('../layout/Layout.vue'),
    meta: { title: '疾病管理', roles: ['ROLE_ADMIN', 'ROLE_VET', 'ROLE_BREEDER'] },
    children: [
      {
        path: 'symptom',
        name: 'SymptomList',
        component: () => import('../views/disease/SymptomList.vue'),
        meta: { title: '症状上报', icon: 'Warning' }
      }
    ]
  },
  {
    path: '/ai',
    name: 'AI',
    component: () => import('../layout/Layout.vue'),
    meta: { title: 'AI 助手', roles: ['ROLE_ADMIN', 'ROLE_RANCHER', 'ROLE_VET', 'ROLE_BREEDER'] },
    children: [
      {
        path: 'chat',
        name: 'AIChat',
        component: () => import('../views/ai/AIChat.vue'),
        meta: { title: '智能分析', icon: 'Monitor' }
      }
    ]
  },
  {
    path: '/alert',
    name: 'Alert',
    component: () => import('../layout/Layout.vue'),
    meta: { title: '预警系统', roles: ['ROLE_ADMIN', 'ROLE_RANCHER'] },
    children: [
      {
        path: 'list',
        name: 'AlertList',
        component: () => import('../views/alert/AlertList.vue'),
        meta: { title: '预警消息', icon: 'Bell' }
      }
    ]
  },
  {
    path: '/system',
    name: 'System',
    component: () => import('../layout/Layout.vue'),
    meta: { title: '系统管理', roles: ['ROLE_ADMIN'] },
    children: [
      {
        path: 'user',
        name: 'UserList',
        component: () => import('../views/system/UserList.vue'),
        meta: { title: '用户管理', icon: 'User' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: staticRoutes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const userStore = useUserStore()
  const token = localStorage.getItem('token')

  if (to.path === '/login') {
    if (token) return next('/')
    return next()
  }

  if (!token) {
    return next('/login')
  }

  // 简单的动态路由注入逻辑
  if (!userStore.hasAddedRoutes) {
    const roles = userStore.roles || []
    dynamicRoutes.forEach(route => {
      // 简单判断角色权限
      if (route.meta?.roles) {
        const routeRoles = route.meta.roles as string[]
        if (roles.some(r => routeRoles.includes(r))) {
          router.addRoute(route)
        }
      } else {
        router.addRoute(route)
      }
    })
    userStore.setHasAddedRoutes(true)
    return next({ ...to, replace: true })
  }

  next()
})

export default router
