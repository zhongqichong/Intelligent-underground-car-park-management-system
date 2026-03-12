<template>
  <el-row :gutter="16">
    <el-col :span="16">
      <el-card shadow="never">
        <template #header>
          <div class="panel-header">车位地图与引导</div>
        </template>
        <div class="legend">
          <el-tag type="success">空闲车位</el-tag>
          <el-tag type="danger">占用车位</el-tag>
          <el-tag type="warning">推荐车位</el-tag>
          <el-tag type="info">障碍/边界</el-tag>
          <el-tag color="#d93025" effect="dark">A*引导路径</el-tag>
        </div>
        <div class="grid-wrapper">
          <div class="grid" :style="gridStyle">
            <template v-for="y in map.height" :key="`r-${y}`">
              <div
                v-for="x in map.width"
                :key="`${x}-${y}`"
                class="cell"
                :class="cellType(x - 1, y - 1)"
              />
            </template>
          </div>
        </div>
      </el-card>
    </el-col>

    <el-col :span="8">
      <el-card shadow="never" class="mb16">
        <template #header>
          <div class="panel-header">车辆操作</div>
        </template>
        <el-form label-position="top">
          <el-form-item label="车牌号">
            <el-input v-model="plate" placeholder="例如：粤A12345" clearable />
          </el-form-item>
          <el-space wrap>
            <el-button type="primary" :loading="loading" @click="loadMap">刷新地图</el-button>
            <el-button type="success" :loading="loading" @click="doEntry">模拟入场</el-button>
            <el-button type="warning" :loading="loading" @click="doExit">模拟出场</el-button>
          </el-space>
        </el-form>
      </el-card>

      <el-card shadow="never" class="mb16">
        <template #header><div class="panel-header">推荐结果</div></template>
        <el-descriptions :column="1" border>
          <el-descriptions-item label="推荐车位">{{ map.recommendation?.spotCode || '-' }}</el-descriptions-item>
          <el-descriptions-item label="评分">{{ map.recommendation?.score ?? '-' }}</el-descriptions-item>
          <el-descriptions-item label="路径节点数">{{ map.routePath?.length || 0 }}</el-descriptions-item>
          <el-descriptions-item label="本次费用">{{ exitInfo?.fee ?? '-' }}</el-descriptions-item>
        </el-descriptions>
      </el-card>

      <el-alert v-if="message" :title="message" type="warning" :closable="false" show-icon />
    </el-col>
  </el-row>
</template>

<script setup>
import { computed, ref } from 'vue'
import { request } from '../api/http'
import { useSession } from '../composables/useSession'

const { state } = useSession()
const plate = ref('TEST-001')
const loading = ref(false)
const message = ref('')
const exitInfo = ref(null)
const map = ref({ width: 60, height: 35, spots: [], elements: [], recommendation: null, routePath: [] })

const gridStyle = computed(() => ({
  gridTemplateColumns: `repeat(${map.value.width}, 26px)`,
  gridTemplateRows: `repeat(${map.value.height}, 26px)`
}))

function inRect(x, y, element) {
  return x >= element.x && x < element.x + (element.width || 1) && y >= element.y && y < element.y + (element.height || 1)
}

function isRouteCell(x, y) {
  return (map.value.routePath || []).some((p) => p.x === x && p.y === y)
}

function cellType(x, y) {
  const spot = map.value.spots.find((s) => s.x === x && s.y === y)
  if (spot) {
    if (map.value.recommendation?.spotCode === spot.code) return 'spot-recommended'
    return spot.occupied ? 'spot-occupied' : 'spot-free'
  }
  if (isRouteCell(x, y)) return 'route-path'
  const element = map.value.elements.find((e) => inRect(x, y, e))
  if (!element) return 'road'
  return `element-${String(element.type).toLowerCase()}`
}

function demoMap() {
  const spots = []
  let idx = 1
  const rowGroupStarts = [3, 11, 19]
  const colGroupStarts = [4, 16, 28, 40]
  for (const rg of rowGroupStarts) {
    for (const ro of [0, 2]) {
      const y = rg + ro
      for (const cg of colGroupStarts) {
        for (let i = 0; i < 5; i++) {
          const x = cg + i * 2
          spots.push({ code: `A${idx}`, x, y, occupied: idx % 7 === 0 })
          idx++
        }
      }
    }
  }

  const elements = [
    { type: 'BOUNDARY', x: 0, y: 0, width: 60, height: 1 },
    { type: 'BOUNDARY', x: 0, y: 34, width: 60, height: 1 },
    { type: 'BOUNDARY', x: 0, y: 0, width: 1, height: 35 },
    { type: 'BOUNDARY', x: 59, y: 0, width: 1, height: 35 },
    { type: 'ENTRANCE', x: 1, y: 17, width: 2, height: 2 },
    { type: 'ELEVATOR', x: 30, y: 2, width: 2, height: 2 }
  ]

  return {
    width: 60,
    height: 35,
    spots,
    elements,
    recommendation: { spotCode: 'A1', score: 1 },
    routePath: [{ x: 1, y: 17 }, { x: 2, y: 17 }, { x: 3, y: 17 }, { x: 4, y: 17 }, { x: 4, y: 5 }, { x: 4, y: 3 }]
  }
}

async function loadMap() {
  loading.value = true
  try {
    map.value = await request({ method: 'get', url: '/api/driver/map-overview', token: state.token })
    message.value = ''
  } catch (_) {
    map.value = demoMap()
    message.value = '后端未连接，已显示演示车位布局（不是空地图）'
  } finally {
    loading.value = false
  }
}

async function doEntry() {
  loading.value = true
  message.value = ''
  try {
    await request({ method: 'post', url: '/api/driver/entry', token: state.token, data: { plateNumber: plate.value } })
    message.value = '车辆入场成功'
    await loadMap()
  } finally {
    loading.value = false
  }
}

async function doExit() {
  loading.value = true
  message.value = ''
  try {
    exitInfo.value = await request({ method: 'post', url: `/api/driver/exit/${plate.value}`, token: state.token })
    message.value = '车辆出场成功'
    await loadMap()
  } finally {
    loading.value = false
  }
}

loadMap()
</script>
