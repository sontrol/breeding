import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/api/request'

const isCancelOrClose = (action: string) =>
  action === 'cancel' || action === 'close'

export function useInvalidate(apiUrl: string, resourceLabel: string, onSuccess: () => void) {
  const handleInvalidate = async (row: any) => {
    try {
      await ElMessageBox.confirm(
        `是否确认作废${resourceLabel}记录 #${row.id}？作废后仅可由管理员恢复。`,
        '作废确认',
        { confirmButtonText: '确定作废', cancelButtonText: '取消', type: 'warning' }
      )
    } catch (action: unknown) {
      if (isCancelOrClose(action as string)) {
        return
      }
      throw action
    }

    try {
      await request.put(`${apiUrl}/invalidate/${row.id}`)
      ElMessage.success('作废成功')
      onSuccess()
    } catch (e: any) {
      ElMessage.error(e?.response?.data?.message || e?.message || '作废失败')
    }
  }

  return { handleInvalidate }
}