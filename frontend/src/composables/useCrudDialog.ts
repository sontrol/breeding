import { ElMessage } from 'element-plus'
import request from '@/api/request'

export function useCrudDialog(
  apiUrl: string,
  onRefresh: () => void,
  options?: {
    addOnly?: boolean
    addSuccessMessage?: string
    editSuccessMessage?: string
  }
) {
  const reset = (form: Record<string, any>, formRef: any, initialValues: Record<string, any>) => {
    Object.assign(form, initialValues)
    formRef?.resetFields()
  }

  const submitForm = (formRef: any, form: Record<string, any>) => {
    formRef?.validate(async (valid: boolean) => {
      if (valid) {
        if (!options?.addOnly && form.id) {
          await request.put(apiUrl, form)
          ElMessage.success(options?.editSuccessMessage || '修改成功')
        } else {
          await request.post(apiUrl, form)
          ElMessage.success(options?.addSuccessMessage || '新增成功')
        }
        onRefresh()
      }
    })
  }

  return { reset, submitForm }
}
