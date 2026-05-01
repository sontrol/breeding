import { useUserStore } from '@/store/user'

export function hasPermission(perm?: string): boolean {
  if (!perm) return true
  const userStore = useUserStore()
  return userStore.permissions.includes(perm) || userStore.permissions.includes('system:*') || userStore.roles.includes('admin')
}

export function usePermission() {
  return { hasPerm: hasPermission }
}
