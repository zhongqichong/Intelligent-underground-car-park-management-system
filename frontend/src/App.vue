<template>
  <el-container class="layout">
    <el-header class="top-header">
      <div class="brand">Underground Garage Intelligent Management</div>
      <div class="top-right">
        <el-input v-model="token" placeholder="Paste JWT token" style="width: 320px" clearable />
      </div>
    </el-header>

    <el-container>
      <el-aside width="220px" class="side-menu">
        <el-menu :default-active="route.path" router>
          <el-menu-item index="/owner">车主视图</el-menu-item>
          <el-menu-item index="/admin">管理员看板</el-menu-item>
        </el-menu>
      </el-aside>

      <el-main>
        <router-view :token="token" />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { ref, provide } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()
const token = ref('')
provide('tokenRef', token)
</script>
