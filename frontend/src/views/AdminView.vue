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

        <el-tab-pane label="车位平面图管理" name="spots">
          <el-space>
            <el-button type="primary" @click="loadSpots">刷新列表</el-button>
            <el-button type="success" @click="saveSpot">{{ spotForm.id ? '更新车位' : '新增车位' }}</el-button>
            <el-button @click="resetSpotForm">清空</el-button>
          </el-space>

          <el-form :inline="true" :model="spotForm" style="margin-top: 12px">
            <el-form-item label="ID"><el-input v-model="spotForm.id" disabled style="width: 90px" /></el-form-item>
            <el-form-item label="编号"><el-input v-model="spotForm.code" /></el-form-item>
            <el-form-item label="X"><el-input-number v-model="spotForm.x" :min="0" /></el-form-item>
            <el-form-item label="Y"><el-input-number v-model="spotForm.y" :min="0" /></el-form-item>
            <el-form-item label="拥堵"><el-input-number v-model="spotForm.zoneLoad" :min="1" :max="10" /></el-form-item>
            <el-form-item label="临近电梯"><el-switch v-model="spotForm.nearElevator" /></el-form-item>
            <el-form-item label="临近柱子"><el-switch v-model="spotForm.nearPillar" /></el-form-item>
            <el-form-item label="占用"><el-switch v-model="spotForm.occupied" /></el-form-item>
          </el-form>

          <el-alert type="info" :closable="false" show-icon style="margin: 8px 0"
                    title="平面图操作：点击车位格子可编辑；点击空白道路可将坐标带入车位表单进行新增" />

          <div class="grid-wrapper map-xl">
            <div class="grid" :style="adminGridStyle">
              <template v-for="y in map.height" :key="`s-r-${y}`">
                <div
                  v-for="x in map.width"
                  :key="`s-${x}-${y}`"
                  class="cell"
                  :class="cellType(x - 1, y - 1)"
                  @click="handleSpotMapClick(x - 1, y - 1)"
                />
              </template>
            </div>
          </div>

          <el-table :data="spots" max-height="260" style="margin-top: 10px">
            <el-table-column prop="id" label="ID" width="80" />
            <el-table-column prop="code" label="车位编号" width="120" />
            <el-table-column prop="x" label="X" width="70" />
            <el-table-column prop="y" label="Y" width="70" />
            <el-table-column label="占用状态" width="120">
              <template #default="scope">
                <el-tag :type="scope.row.occupied ? 'danger' : 'success'">{{ scope.row.occupied ? '占用' : '空闲' }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="320">
              <template #default="scope">
                <el-space>
                  <el-button size="small" @click="editSpot(scope.row)">编辑</el-button>
                  <el-button size="small" type="warning" @click="toggleSpotStatus(scope.row)">{{ scope.row.occupied ? '设为空闲' : '设为占用' }}</el-button>
                  <el-popconfirm title="确认删除该车位？" @confirm="deleteSpot(scope.row.id)">
                    <template #reference><el-button size="small" type="danger">删除</el-button></template>
                  </el-popconfirm>
                </el-space>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <el-tab-pane label="边界/障碍管理" name="elements">
          <el-space>
            <el-button type="primary" @click="loadElements">刷新元素</el-button>
            <el-button type="success" @click="saveElement">{{ elementForm.id ? '更新元素' : '新增元素' }}</el-button>
            <el-button @click="resetElementForm">清空</el-button>
          </el-space>

          <el-form :inline="true" :model="elementForm" style="margin-top: 12px">
            <el-form-item label="ID"><el-input v-model="elementForm.id" disabled style="width: 90px" /></el-form-item>
            <el-form-item label="类型">
              <el-select v-model="elementForm.type" style="width: 140px">
                <el-option label="PILLAR" value="PILLAR" />
                <el-option label="ENTRANCE(单个)" value="ENTRANCE" />
                <el-option label="ELEVATOR" value="ELEVATOR" />
                <el-option label="BOUNDARY" value="BOUNDARY" />
                <el-option label="NO_PARKING" value="NO_PARKING" />
              </el-select>
            </el-form-item>
            <el-form-item label="标签"><el-input v-model="elementForm.label" style="width: 120px" /></el-form-item>
            <el-form-item label="X"><el-input-number v-model="elementForm.x" :min="0" /></el-form-item>
            <el-form-item label="Y"><el-input-number v-model="elementForm.y" :min="0" /></el-form-item>
            <el-form-item label="宽"><el-input-number v-model="elementForm.width" :min="1" /></el-form-item>
            <el-form-item label="高"><el-input-number v-model="elementForm.height" :min="1" /></el-form-item>
          </el-form>

          <el-alert type="info" :closable="false" show-icon style="margin: 8px 0"
                    title="点击平面图任意格子可快速设置边界/障碍起点坐标；边界支持增大宽高实现扩展" />

          <div class="grid-wrapper map-xl">
            <div class="grid" :style="adminGridStyle">
              <template v-for="y in map.height" :key="`e-r-${y}`">
                <div
                  v-for="x in map.width"
                  :key="`e-${x}-${y}`"
                  class="cell"
                  :class="cellType(x - 1, y - 1)"
                  @click="handleElementMapClick(x - 1, y - 1)"
                />
              </template>
            </div>
          </div>

          <el-table :data="elements" max-height="260" style="margin-top: 10px">
            <el-table-column prop="id" label="ID" width="80" />
            <el-table-column prop="type" label="类型" width="130" />
            <el-table-column prop="label" label="标签" width="130" />
            <el-table-column prop="x" label="X" width="70" />
            <el-table-column prop="y" label="Y" width="70" />
            <el-table-column prop="width" label="宽" width="70" />
            <el-table-column prop="height" label="高" width="70" />
            <el-table-column label="操作" width="200">
              <template #default="scope">
                <el-space>
                  <el-button size="small" @click="editElement(scope.row)">编辑</el-button>
                  <el-popconfirm title="确认删除该元素？" @confirm="deleteElement(scope.row.id)">
                    <template #reference><el-button size="small" type="danger">删除</el-button></template>
                  </el-popconfirm>
                </el-space>
              </template>
            </el-table-column>
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
const map = ref({ width: 60, height: 35, spots: [], elements: [], routePath: [], recommendation: null })
const spots = ref([])
const elements = ref([])
const alerts = ref([])
const logs = ref([])

const defaultSpotForm = () => ({ id: null, code: '', x: 0, y: 0, zoneLoad: 1, nearElevator: false, nearPillar: false, occupied: false })
const defaultElementForm = () => ({ id: null, type: 'PILLAR', label: '', x: 0, y: 0, width: 1, height: 1 })

const spotForm = ref(defaultSpotForm())
const elementForm = ref(defaultElementForm())

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

const adminGridStyle = computed(() => ({
  gridTemplateColumns: `repeat(${map.value.width}, 26px)`,
  gridTemplateRows: `repeat(${map.value.height}, 26px)`
}))

const auth = () => ({ token: state.token })


function demoData() {
  const demoSpots = []
  let idx = 1
  const rowGroupStarts = [3, 11, 19]
  const colGroupStarts = [4, 16, 28, 40]
  for (const rg of rowGroupStarts) {
    for (const ro of [0, 2]) {
      const y = rg + ro
      for (const cg of colGroupStarts) {
        for (let i = 0; i < 5; i++) {
          const x = cg + i * 2
          demoSpots.push({ id: idx, code: `A${idx}`, x, y, occupied: idx % 6 === 0, zoneLoad: (idx % 5) + 1, nearElevator: y <= 7, nearPillar: idx % 8 === 0 })
          idx++
        }
      }
    }
  }

  const demoElements = [
    { id: 1, type: 'BOUNDARY', x: 0, y: 0, width: 60, height: 1, label: 'North' },
    { id: 2, type: 'BOUNDARY', x: 0, y: 34, width: 60, height: 1, label: 'South' },
    { id: 3, type: 'BOUNDARY', x: 0, y: 0, width: 1, height: 35, label: 'West' },
    { id: 4, type: 'BOUNDARY', x: 59, y: 0, width: 1, height: 35, label: 'East' },
    { id: 5, type: 'ENTRANCE', x: 1, y: 17, width: 2, height: 2, label: 'Gate-1' },
    { id: 6, type: 'ELEVATOR', x: 30, y: 2, width: 2, height: 2, label: 'Main-Elevator' }
  ]

  return {
    map: {
      width: 60,
      height: 35,
      spots: demoSpots,
      elements: demoElements,
      routePath: [],
      recommendation: null
    },
    spots: demoSpots,
    elements: demoElements
  }
}

function inRect(x, y, element) {
  return x >= element.x && x < element.x + (element.width || 1) && y >= element.y && y < element.y + (element.height || 1)
}

function isRouteCell(x, y) {
  return (map.value.routePath || []).some((p) => p.x === x && p.y === y)
}

function cellType(x, y) {
  const spot = spots.value.find((s) => s.x === x && s.y === y)
  if (spot) {
    return spot.occupied ? 'spot-occupied' : 'spot-free'
  }
  if (isRouteCell(x, y)) return 'route-path'
  const element = elements.value.find((e) => inRect(x, y, e))
  if (!element) return 'road'
  return `element-${String(element.type).toLowerCase()}`
}

function resetSpotForm() {
  spotForm.value = defaultSpotForm()
}

function resetElementForm() {
  elementForm.value = defaultElementForm()
}

function editSpot(spot) {
  spotForm.value = { ...spot }
}

function editElement(element) {
  elementForm.value = { ...element }
}

function handleSpotMapClick(x, y) {
  const existing = spots.value.find((s) => s.x === x && s.y === y)
  if (existing) {
    editSpot(existing)
    return
  }
  spotForm.value = { ...spotForm.value, id: null, x, y, code: spotForm.value.code || `S-${x}-${y}` }
}

function handleElementMapClick(x, y) {
  const existing = elements.value.find((e) => inRect(x, y, e))
  if (existing) {
    editElement(existing)
    return
  }
  elementForm.value = { ...elementForm.value, id: null, x, y }
}

async function loadMapOverview() {
  try {
    map.value = await request({ method: 'get', url: '/api/driver/map-overview', ...auth() })
  } catch (_) {
    map.value = demoData().map
  }
}

async function loadReport() {
  report.value = await request({ method: 'get', url: '/api/admin/reports/overview', ...auth() })
}

async function loadSpots() {
  try {
    spots.value = await request({ method: 'get', url: '/api/admin/spots', ...auth() })
  } catch (_) {
    spots.value = demoData().spots
  }
}

async function saveSpot() {
  await request({ method: 'post', url: '/api/admin/spots', data: spotForm.value, ...auth() })
  ElMessage.success(spotForm.value.id ? '车位更新成功' : '车位新增成功')
  resetSpotForm()
  await loadSpots()
  await loadMapOverview()
  await loadReport()
}

async function toggleSpotStatus(spot) {
  await request({ method: 'patch', url: `/api/admin/spots/${spot.id}/status`, params: { occupied: !spot.occupied }, ...auth() })
  ElMessage.success('车位状态已更新')
  await loadSpots()
  await loadMapOverview()
  await loadReport()
}

async function deleteSpot(spotId) {
  await request({ method: 'delete', url: `/api/admin/spots/${spotId}`, ...auth() })
  ElMessage.success('车位删除成功')
  await loadSpots()
  await loadMapOverview()
  await loadReport()
}

async function loadElements() {
  try {
    elements.value = await request({ method: 'get', url: '/api/admin/map-elements', ...auth() })
  } catch (_) {
    elements.value = demoData().elements
  }
}

async function saveElement() {
  await request({ method: 'post', url: '/api/admin/map-elements', data: elementForm.value, ...auth() })
  ElMessage.success(elementForm.value.id ? '元素更新成功' : '元素新增成功')
  resetElementForm()
  await loadElements()
  await loadMapOverview()
}

async function deleteElement(elementId) {
  await request({ method: 'delete', url: `/api/admin/map-elements/${elementId}`, ...auth() })
  ElMessage.success('元素删除成功')
  await loadElements()
  await loadMapOverview()
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
loadMapOverview()
loadSpots()
loadElements()
loadAlerts(true)
</script>
