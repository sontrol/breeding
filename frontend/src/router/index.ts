import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'
import { useUserStore } from '@/store/user'
import { jwtDecode } from 'jwt-decode'
import { hasPermission } from '@/composables/usePermission'

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
        meta: { title: '数据看板', icon: 'Odometer', permission: 'dashboard:view' }
      }
    ]
  }
]

export const dynamicRoutes: RouteRecordRaw[] = [
  {
    path: '/animal',
    name: 'Animal',
    component: () => import('../layout/Layout.vue'),
    meta: { title: '动物管理', permission: 'animal:view' },
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
    meta: { title: '饲养管理', permission: 'feeding:view' },
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
    meta: { title: '库存管理', permission: 'inventory:view' },
    children: [
      {
        path: 'list',
        name: 'InventoryList',
        component: () => import('../views/inventory/InventoryList.vue'),
        meta: { title: '库存列表', icon: 'Box' }
      },
      {
        path: 'log',
        name: 'InventoryLog',
        component: () => import('../views/inventory/InventoryLog.vue'),
        meta: { title: '出入库明细', icon: 'Document' }
      }
    ]
  },
  {
    path: '/disease',
    name: 'Disease',
    component: () => import('../layout/Layout.vue'),
    meta: { title: '疾病管理', permission: 'disease:view' },
    children: [
      {
        path: 'symptom',
        name: 'SymptomList',
        component: () => import('../views/disease/SymptomList.vue'),
        meta: { title: '症状上报', icon: 'Warning' }
      },
      {
        path: 'diagnosis',
        name: 'DiagnosisList',
        component: () => import('../views/disease/DiagnosisList.vue'),
        meta: { title: '诊断记录', icon: 'FirstAidKit' }
      },
      {
        path: 'treatment',
        name: 'TreatmentList',
        component: () => import('../views/disease/TreatmentList.vue'),
        meta: { title: '治疗记录', icon: 'MagicStick' }
      },
      {
        path: 'vaccine',
        name: 'VaccineList',
        component: () => import('../views/disease/VaccineList.vue'),
        meta: { title: '疫苗管理', icon: 'Coin' }
      }
    ]
  },
  {
    path: '/ai/chat',
    name: 'AI',
    component: () => import('../layout/Layout.vue'),
    meta: { title: 'AI 助手', permission: 'ai:view' },
    children: [
      {
        path: '',
        name: 'AIChat',
        component: () => import('../views/ai/AIChat.vue'),
        meta: { title: 'AI 助手', icon: 'Monitor' }
      }
    ]
  },
  {
    path: '/event',
    name: 'Event',
    component: () => import('../layout/Layout.vue'),
    meta: { title: '事件中心', permission: 'event:view' },
    children: [
      {
        path: 'list',
        name: 'EventList',
        component: () => import('../views/event/EventList.vue'),
        meta: { title: '事件记录', icon: 'List' }
      }
    ]
  },
  {
    path: '/alert',
    name: 'Alert',
    component: () => import('../layout/Layout.vue'),
    meta: { title: '预警系统', permission: 'alert:view' },
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
    meta: { title: '系统管理', permission: 'system:view' },
    children: [
      {
        path: 'user',
        name: 'UserList',
        component: () => import('../views/system/UserList.vue'),
        meta: { title: '用户管理', icon: 'User' }
      },
      {
        path: 'invalid',
        name: 'InvalidDataList',
        component: () => import('../views/system/InvalidDataList.vue'),
        meta: { title: '作废数据', icon: 'DeleteFilled', permission: 'system:invalid:view' }
      }
    ]
  },
  {
    path: '/register-audit',
    name: 'RegisterAudit',
    component: () => import('../layout/Layout.vue'),
    meta: { title: '注册审核', permission: 'system:register:view' },
    children: [
      {
        path: 'list',
        name: 'RegisterAuditList',
        component: () => import('../views/system/RegisterAuditList.vue'),
        meta: { title: '注册审核', icon: 'Finished' }
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
  const hasValidAuthState = () => {
    if (!token) return false
    try {
      const decoded = jwtDecode<{ exp: number }>(token)
      if (decoded.exp * 1000 < Date.now()) {
        return false
      }
      return true
    } catch (e) {
      return false
    }
  }
  const resetInvalidAuth = (redirect = true) => {
    userStore.logout()
    return redirect ? next('/login') : next()
  }
  const firstAllowedPath = () => {
    if (hasPermission('dashboard:view')) {
      return '/dashboard'
    }
    const firstRoute = dynamicRoutes.find(route => hasPermission(route.meta?.permission as string | undefined))
    if (!firstRoute) {
      return '/login'
    }
    const firstChild = firstRoute.children?.[0]
    return firstChild ? `${firstRoute.path}/${firstChild.path}` : firstRoute.path
  }

  if (to.path === '/login') {
    if (token && !hasValidAuthState()) {
      return resetInvalidAuth(false)
    }
    if (token) {
      const targetPath = firstAllowedPath()
      if (targetPath !== '/login') {
        return next(targetPath)
      }
    }
    return next()
  }

  if (!token) {
    return next('/login')
  }

  if (!hasValidAuthState()) {
    return resetInvalidAuth()
  }

  // 简单的动态路由注入逻辑
  if (!userStore.hasAddedRoutes) {
    const permissions = userStore.permissions || []
    dynamicRoutes.forEach(route => {
      // 基于 permission 码判断权限
      if (route.meta?.permission) {
        const reqPerm = route.meta.permission as string
        if (permissions.includes(reqPerm) || permissions.includes('system:*') || userStore.roles.includes('admin')) {
          router.addRoute(route)
        }
      } else {
        router.addRoute(route)
      }
    })
    userStore.setHasAddedRoutes(true)
    return next({ ...to, replace: true })
  }

  if (!hasPermission(to.meta?.permission as string | undefined)) {
    const targetPath = firstAllowedPath()
    if (targetPath === '/login') {
      return resetInvalidAuth()
    }
    return next(targetPath)
  }

  next()
})

export default router
