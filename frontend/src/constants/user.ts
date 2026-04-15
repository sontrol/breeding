export interface RoleOption {
  label: string
  value: string
}

export const userRoleOptions: RoleOption[] = [
  { label: '管理员', value: 'admin' },
  { label: '牧场主', value: 'owner' },
  { label: '兽医', value: 'vet' },
  { label: '饲养员', value: 'feeder' }
]

export const assignableUserRoleOptions = userRoleOptions.filter(item => item.value !== 'admin')

export const registerRoleOptions = userRoleOptions.filter(item => item.value === 'vet' || item.value === 'feeder')

export const getUserRoleLabel = (roleCode?: string) => {
  return userRoleOptions.find(item => item.value === roleCode)?.label || '未设置'
}
