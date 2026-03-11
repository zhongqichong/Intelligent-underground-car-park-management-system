import axios from 'axios'

const apiBaseUrl = import.meta.env.VITE_API_BASE_URL || '/api'

const http = axios.create({
  baseURL: apiBaseUrl
})

export async function request({ method, url, data, token }) {
  const headers = token ? { Authorization: `Bearer ${token}` } : {}
  const res = await http({ method, url, data, headers })
  const payload = res.data
  if (payload && payload.success === false) {
    throw new Error(payload.message || 'Request failed')
  }
  return payload?.data
}
