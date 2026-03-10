import { createRouter, createWebHistory } from 'vue-router'
import OwnerView from '../views/OwnerView.vue'
import AdminView from '../views/AdminView.vue'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', redirect: '/owner' },
    { path: '/owner', component: OwnerView },
    { path: '/admin', component: AdminView }
  ]
})

export default router
