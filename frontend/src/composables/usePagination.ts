import { ref, type Ref, onMounted } from 'vue'
import request from '@/api/request'

interface PageParams {
  page: number
  size: number
  [key: string]: any
}

export function usePagination<T>(
  apiUrl: string,
  queryParams: PageParams,
  options?: {
    autoFetch?: boolean
  }
) {
  const loading = ref(true)
  const list = ref<T[]>([]) as Ref<T[]>
  const total = ref(0)

  const getList = async () => {
    loading.value = true
    try {
      const res: any = await request.get(apiUrl, { params: queryParams })
      if (res.code === 200) {
        list.value = res.data.records
        total.value = res.data.total
      }
    } finally {
      loading.value = false
    }
  }

  const resetQuery = (...fields: string[]) => {
    for (const field of fields) {
      queryParams[field] = undefined
    }
    handleQuery()
  }

  const handleQuery = () => {
    queryParams.page = 1
    getList()
  }

  const handleSizeChange = (val: number) => {
    queryParams.size = val
    getList()
  }

  const handleCurrentChange = (val: number) => {
    queryParams.page = val
    getList()
  }

  if (options?.autoFetch) {
    onMounted(() => {
      getList()
    })
  }

  return { loading, list, total, getList, resetQuery, handleQuery, handleSizeChange, handleCurrentChange }
}
