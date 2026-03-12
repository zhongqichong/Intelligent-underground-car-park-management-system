<template>
  <div class="login-page">
    <el-card class="login-card" shadow="hover">
      <template #header>
        <div class="login-title">智能地下车库管理系统</div>
      </template>

      <el-tabs v-model="activeTab">
        <el-tab-pane label="登录" name="login">
          <el-form :model="loginForm" @submit.prevent>
            <el-form-item label="用户名">
              <el-input v-model="loginForm.username" placeholder="请输入用户名" />
            </el-form-item>
            <el-form-item label="密码">
              <el-input v-model="loginForm.password" type="password" show-password placeholder="请输入密码" />
            </el-form-item>
            <el-button type="primary" :loading="loading" class="full" @click="login">登录</el-button>
          </el-form>
        </el-tab-pane>
        <el-tab-pane label="车主注册" name="register">
          <el-form :model="registerForm" @submit.prevent>
            <el-form-item label="用户名">
              <el-input v-model="registerForm.username" placeholder="至少 4 位" />
            </el-form-item>
            <el-form-item label="密码">
              <el-input v-model="registerForm.password" type="password" show-password placeholder="至少 6 位" />
            </el-form-item>
            <el-button type="success" :loading="loading" class="full" @click="registerOwner">注册车主</el-button>
          </el-form>
        </el-tab-pane>
      </el-tabs>

      <el-alert
        v-if="hint"
        :title="hint"
        type="info"
        show-icon
        :closable="false"
        class="hint"
      />
    </el-card>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { request } from '../api/http'
import { useSession } from '../composables/useSession'

const router = useRouter()
const { setToken } = useSession()

const activeTab = ref('login')
const loading = ref(false)
const hint = ref('默认管理员账号：admin / Admin@123')

const loginForm = reactive({ username: 'admin', password: 'Admin@123' })
const registerForm = reactive({ username: '', password: '' })

async function login() {
  loading.value = true
  try {
    const res = await request({ method: 'post', url: '/api/auth/login', data: loginForm })
    setToken(res.token)
    ElMessage.success('登录成功')
    router.push('/owner')
  } finally {
    loading.value = false
  }
}

async function registerOwner() {
  loading.value = true
  try {
    await request({ method: 'post', url: '/api/auth/register-owner', data: registerForm })
    ElMessage.success('注册成功，请登录')
    activeTab.value = 'login'
    loginForm.username = registerForm.username
    loginForm.password = registerForm.password
  } finally {
    loading.value = false
  }
}
</script>
