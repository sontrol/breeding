import axios from 'axios'
import { useUserStore } from '@/store/user'
import { ElMessage } from 'element-plus'
import router from '@/router'

const service = axios.create({
  baseURL: '/api',
  timeout: 5000
})

service.interceptors.request.use(
  config => {
    const userStore = useUserStore()
    if (userStore.token) {
      config.headers['Authorization'] = 'Bearer ' + userStore.token
    }
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

service.interceptors.response.use(
  response => {
    const res = response.data
    if (res.code !== 200) {
      ElMessage.error(res.msg || '错误')
      if (res.code === 401 || res.code === 403) {
        useUserStore().logout()
        router.push('/login')
      }
      return Promise.reject(new 错误(res.msg || '错误'))
    } else {
      return res
    }
  },
  error => {
    ElMessage.error(error.message)
    return Promise.reject(error)
  }
)

export default service
