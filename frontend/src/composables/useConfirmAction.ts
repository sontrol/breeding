import { ElMessage, ElMessageBox } from 'element-plus'

const isCancelOrClose = (action: string) =>
  action === 'cancel' || action === 'close'

export function useConfirmAction(
  options: {
    message: string | ((row: any) => string)
    title?: string
    confirmText?: string
    cancelText?: string
    action: (row: any) => Promise<any>
    successMessage?: string
    onSuccess?: () => void
  }
) {
  const execute = async (row: any) => {
    const msg = typeof options.message === 'function' ? options.message(row) : options.message
    try {
      await ElMessageBox.confirm(msg, options.title || '确认', {
        confirmButtonText: options.confirmText || '确定',
        cancelButtonText: options.cancelText || '取消',
        type: 'warning'
      })
    } catch (action: unknown) {
      if (isCancelOrClose(action as string)) {
        return
      }
      throw action
    }

    try {
      await options.action(row)
      ElMessage.success(options.successMessage || '操作成功')
      options.onSuccess?.()
    } catch (e: any) {
      ElMessage.error(e?.response?.data?.message || e?.message || '操作失败')
    }
  }

  return { execute }
}
