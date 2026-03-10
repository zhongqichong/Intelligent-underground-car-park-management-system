<template>
  <el-card>
    <template #header>
      <div class="card-header">
        管理员看板
        <el-button type="primary" size="small" style="float:right" :loading="loading" @click="loadReport">刷新</el-button>
      </div>
    </template>

    <el-alert v-if="message" :title="message" type="warning" show-icon :closable="false" style="margin-bottom: 12px" />

    <el-row :gutter="12">
      <el-col :span="6" v-for="item in cards" :key="item.label">
        <el-card shadow="hover" class="metric-card">
          <div class="metric-label">{{ item.label }}</div>
          <div class="metric-value">{{ item.value }}</div>
        </el-card>
      </el-col>
    </el-row>

    <el-table :data="report.recentDailyRevenue || []" style="width: 100%; margin-top: 16px">
      <el-table-column prop="day" label="日期" width="180" />
      <el-table-column prop="revenue" label="收入(¥)" />
    </el-table>
  </el-card>
</template>

<script setup>
import { computed, inject, ref } from 'vue'
import { request } from '../api/http'

const tokenRef = inject('tokenRef')
const loading = ref(false)
const message = ref('')
const report = ref({
  totalSpots: 0,
  occupiedSpots: 0,
  freeSpots: 0,
  activeSessions: 0,
  todayRevenue: 0,
  todayEntries: 0,
  unresolvedAlerts: 0,
  recentDailyRevenue: []
})

const occupancyRate = computed(() => {
  if (!report.value.totalSpots) return 0
  return Math.round((report.value.occupiedSpots / report.value.totalSpots) * 100) + '%'
})

const cards = computed(() => [
  { label: '总车位', value: report.value.totalSpots },
  { label: '占用车位', value: report.value.occupiedSpots },
  { label: '空闲车位', value: report.value.freeSpots },
  { label: '占用率', value: occupancyRate.value },
  { label: '今日进场', value: report.value.todayEntries },
  { label: '在场会话', value: report.value.activeSessions },
  { label: '今日收入', value: `¥ ${report.value.todayRevenue}` },
  { label: '未处理告警', value: report.value.unresolvedAlerts }
])

async function loadReport() {
  loading.value = true
  message.value = ''
  try {
    report.value = await request({ method: 'get', url: '/api/admin/reports/overview', token: tokenRef.value })
  } catch (e) {
    message.value = e?.response?.data?.message || e?.message || '加载报表失败，请使用管理员token'
  } finally {
    loading.value = false
  }
}
</script>
