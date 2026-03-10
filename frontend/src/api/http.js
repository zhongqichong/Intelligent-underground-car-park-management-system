import axios from 'axios'

const http = axios.create({
  baseURL: 'http://localhost:8080'
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
