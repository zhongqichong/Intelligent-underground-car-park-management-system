<template>
  <el-config-provider>
    <el-container class="app-shell">
      <el-aside width="220px" class="sidebar" v-if="isAuthenticated">
        <div class="logo">Smart Garage</div>
        <el-menu :default-active="route.path" router>
          <el-menu-item index="/owner">车主工作台</el-menu-item>
          <el-menu-item index="/admin" :disabled="!isAdmin">管理员中心</el-menu-item>
        </el-menu>
      </el-aside>

      <el-container>
        <el-header class="header" v-if="isAuthenticated">
          <el-breadcrumb separator="/">
            <el-breadcrumb-item>智能地下车库</el-breadcrumb-item>
            <el-breadcrumb-item>{{ route.path === '/admin' ? '管理员中心' : '车主工作台' }}</el-breadcrumb-item>
          </el-breadcrumb>
          <div class="header-right">
            <el-tag type="success">{{ session.state.role || 'OWNER' }}</el-tag>
            <span class="username">{{ session.state.username }}</span>
            <el-button link type="danger" @click="logout">退出登录</el-button>
          </div>
        </el-header>

        <el-main class="main-content">
          <router-view />
        </el-main>
      </el-container>
    </el-container>
  </el-config-provider>
</template>

<script setup>
import { useRoute, useRouter } from 'vue-router'
import { useSession } from './composables/useSession'

const session = useSession()
const route = useRoute()
const router = useRouter()

const isAuthenticated = session.isAuthenticated
const isAdmin = session.isAdmin

function logout() {
  session.clearSession()
  router.push('/login')
}
</script>
