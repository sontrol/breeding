import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const userInfo = ref(JSON.parse(localStorage.getItem('userInfo') || '{}'))
  const roles = ref<string[]>(JSON.parse(localStorage.getItem('roles') || '[]'))
  const permissions = ref<string[]>(JSON.parse(localStorage.getItem('permissions') || '[]'))
  const hasAddedRoutes = ref(false)

  const setAuthData = (data: any) => {
    token.value = data.token
    userInfo.value = { userId: data.userId, username: data.username, realName: data.realName }
    roles.value = data.roles
    permissions.value = data.permissions
    
    localStorage.setItem('token', data.token)
    localStorage.setItem('userInfo', JSON.stringify(userInfo.value))
    localStorage.setItem('roles', JSON.stringify(roles.value))
    localStorage.setItem('permissions', JSON.stringify(permissions.value))
  }

  const setHasAddedRoutes = (status: boolean) => {
    hasAddedRoutes.value = status
  }

  const logout = () => {
    token.value = ''
    userInfo.value = {}
    roles.value = []
    permissions.value = []
    hasAddedRoutes.value = false
    localStorage.clear()
  }

  return { token, userInfo, roles, permissions, hasAddedRoutes, setAuthData, setHasAddedRoutes, logout }
})
