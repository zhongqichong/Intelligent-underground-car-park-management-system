import { createRouter, createWebHistory } from 'vue-router'
import LoginView from '../views/LoginView.vue'
import OwnerView from '../views/OwnerView.vue'
import AdminView from '../views/AdminView.vue'
import { useSession } from '../composables/useSession'

const routes = [
  { path: '/', redirect: '/owner' },
  { path: '/login', component: LoginView, meta: { public: true } },
  { path: '/owner', component: OwnerView },
  { path: '/admin', component: AdminView, meta: { adminOnly: true } }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to) => {
  const { isAuthenticated, isAdmin } = useSession()

  if (to.meta.public) return true
  if (!isAuthenticated.value) return '/login'
  if (to.meta.adminOnly && !isAdmin.value) return '/owner'
  return true
})

export default router
