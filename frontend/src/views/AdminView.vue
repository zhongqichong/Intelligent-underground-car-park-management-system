<template>
  <el-space direction="vertical" fill size="16">
    <el-row :gutter="12">
      <el-col :span="6" v-for="item in cards" :key="item.label">
        <el-card shadow="hover" class="metric-card">
          <div class="metric-label">{{ item.label }}</div>
          <div class="metric-value">{{ item.value }}</div>
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="never">
      <el-tabs v-model="activeTab">
        <el-tab-pane label="运营报表" name="report">
          <el-button type="primary" @click="loadReport">刷新报表</el-button>
          <el-table :data="report.recentDailyRevenue || []" style="margin-top: 12px">
            <el-table-column prop="day" label="日期" width="180" />
            <el-table-column prop="revenue" label="收入（¥）" />
          </el-table>
        </el-tab-pane>

        <el-tab-pane label="车位管理" name="spots">
          <el-space>
            <el-button type="primary" @click="loadSpots">刷新列表</el-button>
            <el-button type="success" @click="saveSpot">新增/更新车位</el-button>
          </el-space>
          <el-form :inline="true" :model="spotForm" style="margin-top: 12px">
            <el-form-item label="编号"><el-input v-model="spotForm.code" /></el-form-item>
            <el-form-item label="X"><el-input-number v-model="spotForm.x" :min="0" /></el-form-item>
            <el-form-item label="Y"><el-input-number v-model="spotForm.y" :min="0" /></el-form-item>
            <el-form-item label="占用"><el-switch v-model="spotForm.occupied" /></el-form-item>
          </el-form>
          <el-table :data="spots" max-height="280">
            <el-table-column prop="id" label="ID" width="80" />
            <el-table-column prop="code" label="车位编号" />
            <el-table-column prop="x" label="X" width="70" />
            <el-table-column prop="y" label="Y" width="70" />
            <el-table-column prop="occupied" label="占用" width="90" />
          </el-table>
        </el-tab-pane>

        <el-tab-pane label="地图元素" name="elements">
          <el-space>
            <el-button type="primary" @click="loadElements">刷新元素</el-button>
            <el-button type="success" @click="saveElement">新增/更新元素</el-button>
          </el-space>
          <el-form :inline="true" :model="elementForm" style="margin-top: 12px">
            <el-form-item label="类型">
              <el-select v-model="elementForm.type" style="width: 130px">
                <el-option label="PILLAR" value="PILLAR" />
                <el-option label="ENTRANCE" value="ENTRANCE" />
                <el-option label="ELEVATOR" value="ELEVATOR" />
                <el-option label="BOUNDARY" value="BOUNDARY" />
                <el-option label="NO_PARKING" value="NO_PARKING" />
              </el-select>
            </el-form-item>
            <el-form-item label="X"><el-input-number v-model="elementForm.x" :min="0" /></el-form-item>
            <el-form-item label="Y"><el-input-number v-model="elementForm.y" :min="0" /></el-form-item>
            <el-form-item label="宽"><el-input-number v-model="elementForm.width" :min="1" /></el-form-item>
            <el-form-item label="高"><el-input-number v-model="elementForm.height" :min="1" /></el-form-item>
          </el-form>
          <el-table :data="elements" max-height="280">
            <el-table-column prop="id" label="ID" width="80" />
            <el-table-column prop="type" label="类型" width="130" />
            <el-table-column prop="x" label="X" width="70" />
            <el-table-column prop="y" label="Y" width="70" />
            <el-table-column prop="width" label="宽" width="70" />
            <el-table-column prop="height" label="高" width="70" />
          </el-table>
        </el-tab-pane>

        <el-tab-pane label="告警中心" name="alerts">
          <el-space>
            <el-button @click="loadAlerts(true)">未处理告警</el-button>
            <el-button type="primary" @click="loadAlerts(false)">全部告警</el-button>
            <el-button type="success" @click="scanOvertime">扫描超时停放</el-button>
          </el-space>
          <el-table :data="alerts" style="margin-top: 12px">
            <el-table-column prop="id" label="ID" width="80" />
            <el-table-column prop="type" label="类型" />
            <el-table-column prop="message" label="内容" />
            <el-table-column prop="resolved" label="状态" width="100" />
            <el-table-column label="操作" width="120">
              <template #default="scope">
                <el-button size="small" type="success" :disabled="scope.row.resolved" @click="resolve(scope.row.id)">处理</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <el-tab-pane label="操作日志" name="logs">
          <el-button type="primary" @click="loadLogs">刷新日志</el-button>
          <el-table :data="logs" style="margin-top: 12px" max-height="320">
            <el-table-column prop="id" label="ID" width="80" />
            <el-table-column prop="username" label="用户名" width="120" />
            <el-table-column prop="action" label="动作" />
            <el-table-column prop="createdAt" label="时间" width="220" />
          </el-table>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </el-space>
</template>

<script setup>
import { computed, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { request } from '../api/http'
import { useSession } from '../composables/useSession'

const { state } = useSession()
const activeTab = ref('report')
const report = ref({ totalSpots: 0, occupiedSpots: 0, freeSpots: 0, activeSessions: 0, todayRevenue: 0, todayEntries: 0, unresolvedAlerts: 0, recentDailyRevenue: [] })
const spots = ref([])
const elements = ref([])
const alerts = ref([])
const logs = ref([])

const spotForm = ref({ code: '', x: 0, y: 0, occupied: false })
const elementForm = ref({ type: 'PILLAR', x: 0, y: 0, width: 1, height: 1 })

const cards = computed(() => [
  { label: '总车位', value: report.value.totalSpots },
  { label: '占用车位', value: report.value.occupiedSpots },
  { label: '空闲车位', value: report.value.freeSpots },
  { label: '今日进场', value: report.value.todayEntries },
  { label: '在场会话', value: report.value.activeSessions },
  { label: '今日收入', value: `¥ ${report.value.todayRevenue}` },
  { label: '未处理告警', value: report.value.unresolvedAlerts },
  { label: '占用率', value: report.value.totalSpots ? `${Math.round((report.value.occupiedSpots / report.value.totalSpots) * 100)}%` : '0%' }
])

const auth = () => ({ token: state.token })

async function loadReport() {
  report.value = await request({ method: 'get', url: '/api/admin/reports/overview', ...auth() })
}

async function loadSpots() {
  spots.value = await request({ method: 'get', url: '/api/admin/spots', ...auth() })
}

async function saveSpot() {
  await request({ method: 'post', url: '/api/admin/spots', data: spotForm.value, ...auth() })
  ElMessage.success('车位保存成功')
  await loadSpots()
}

async function loadElements() {
  elements.value = await request({ method: 'get', url: '/api/admin/map-elements', ...auth() })
}

async function saveElement() {
  await request({ method: 'post', url: '/api/admin/map-elements', data: elementForm.value, ...auth() })
  ElMessage.success('地图元素保存成功')
  await loadElements()
}

async function loadAlerts(unresolvedOnly) {
  alerts.value = await request({ method: 'get', url: '/api/admin/alerts', params: { unresolvedOnly }, ...auth() })
}

async function resolve(alertId) {
  await request({ method: 'post', url: `/api/admin/alerts/${alertId}/resolve`, ...auth() })
  ElMessage.success('告警已处理')
  await loadAlerts(true)
  await loadReport()
}

async function scanOvertime() {
  await request({ method: 'post', url: '/api/driver/alerts/scan-overtime', ...auth() })
  ElMessage.success('超时扫描完成')
  await loadAlerts(true)
}

async function loadLogs() {
  logs.value = await request({ method: 'get', url: '/api/admin/logs', ...auth() })
}

loadReport()
loadSpots()
loadElements()
loadAlerts(true)
</script>
