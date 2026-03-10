<template>
  <div>
    <el-card>
      <template #header>
        <div class="card-header">车主视图</div>
      </template>

      <el-space wrap>
        <el-input v-model="plate" placeholder="plate number" style="width: 180px" />
        <el-button type="primary" :loading="loading" @click="loadMap">加载地图</el-button>
        <el-button type="success" :loading="loading" @click="doEntry">模拟入场</el-button>
      </el-space>

      <el-alert v-if="message" :title="message" type="warning" show-icon :closable="false" style="margin-top: 12px" />

      <div style="margin-top: 12px" v-if="map.recommendation">
        推荐车位：<b>{{ map.recommendation.spotCode }}</b>
        <span style="margin-left: 12px">score={{ map.recommendation.score }}</span>
      </div>
      <div style="margin-top: 6px" v-if="map.routePath?.length">
        引导路径节点数：{{ map.routePath.length }}
      </div>

      <div class="grid" :style="gridStyle">
        <template v-for="y in map.height" :key="'r'+y">
          <div
            v-for="x in map.width"
            :key="x+'-'+y"
            class="cell"
            :class="cellType(x-1,y-1)"
          />
        </template>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { computed, inject, ref } from 'vue'
import { request } from '../api/http'

const tokenRef = inject('tokenRef')
const plate = ref('TEST-001')
const loading = ref(false)
const message = ref('')
const map = ref({ width: 40, height: 25, spots: [], elements: [], recommendation: null, routePath: [] })

const gridStyle = computed(() => ({
  gridTemplateColumns: `repeat(${map.value.width}, 16px)`,
  gridTemplateRows: `repeat(${map.value.height}, 16px)`
}))

const isRouteCell = (x, y) => (map.value.routePath || []).some(p => p.x === x && p.y === y)

function cellType(x, y) {
  const spot = map.value.spots.find(s => s.x === x && s.y === y)
  if (spot) {
    if (map.value.recommendation?.spotCode === spot.code) return 'spot-recommended'
    return spot.occupied ? 'spot-occupied' : 'spot-free'
  }
  if (isRouteCell(x, y)) return 'route-path'
  const element = map.value.elements.find(e => x >= e.x && x < (e.x + (e.width || 1)) && y >= e.y && y < (e.y + (e.height || 1)))
  if (!element) return 'road'
  return `element-${element.type.toLowerCase()}`
}

async function loadMap() {
  loading.value = true
  message.value = ''
  try {
    map.value = await request({ method: 'get', url: '/api/driver/map-overview', token: tokenRef.value })
  } catch (e) {
    message.value = e?.response?.data?.message || e?.message || '加载地图失败'
  } finally {
    loading.value = false
  }
}

async function doEntry() {
  loading.value = true
  message.value = ''
  try {
    await request({ method: 'post', url: '/api/driver/entry', token: tokenRef.value, data: { plateNumber: plate.value } })
    await loadMap()
    message.value = '入场成功'
  } catch (e) {
    message.value = e?.response?.data?.message || e?.message || '入场失败'
  } finally {
    loading.value = false
  }
}
</script>
