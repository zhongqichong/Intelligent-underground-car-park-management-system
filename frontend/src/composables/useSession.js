import { computed, reactive } from 'vue'

const SESSION_KEY = 'garage_session'

const state = reactive({
  token: '',
  username: '',
  role: ''
})

const saved = localStorage.getItem(SESSION_KEY)
if (saved) {
  try {
    Object.assign(state, JSON.parse(saved))
  } catch (_) {
    localStorage.removeItem(SESSION_KEY)
  }
}

function decodeJwtPayload(token) {
  try {
    const payload = token.split('.')[1]
    return JSON.parse(atob(payload.replace(/-/g, '+').replace(/_/g, '/')))
  } catch (_) {
    return {}
  }
}

function persist() {
  localStorage.setItem(SESSION_KEY, JSON.stringify(state))
}

export function useSession() {
  const isAuthenticated = computed(() => !!state.token)
  const isAdmin = computed(() => state.role === 'ADMIN')

  function setToken(token) {
    const payload = decodeJwtPayload(token)
    state.token = token
    state.username = payload.sub || ''
    state.role = payload.role || ''
    persist()
  }

  function clearSession() {
    state.token = ''
    state.username = ''
    state.role = ''
    localStorage.removeItem(SESSION_KEY)
  }

  return {
    state,
    isAuthenticated,
    isAdmin,
    setToken,
    clearSession
  }
}
