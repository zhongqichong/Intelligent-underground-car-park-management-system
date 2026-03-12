import axios from 'axios'
import { ElMessage } from 'element-plus'

const baseURL = import.meta.env.VITE_API_BASE_URL || ''

const http = axios.create({
  baseURL,
  timeout: 15000
})

http.interceptors.response.use(
  (response) => {
    const payload = response.data
    if (payload && typeof payload.success === 'boolean') {
      if (!payload.success) {
        const error = new Error(payload.message || 'Request failed')
        error.payload = payload
        throw error
      }
      return payload.data
    }
    return payload
  },
  (error) => {
    const message = error?.response?.data?.message || error.message || 'Network error'
    ElMessage.error(message)
    throw error
  }
)

export function request({ method = 'get', url, data, params, token }) {
  const headers = token ? { Authorization: `Bearer ${token}` } : {}
  return http({ method, url, data, params, headers })
}
