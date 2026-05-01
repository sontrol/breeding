import { useUserStore } from '@/store/user'

export function useCurrentUserId(): number | undefined {
  return useUserStore().userInfo.userId
}
