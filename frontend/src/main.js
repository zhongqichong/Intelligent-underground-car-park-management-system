import { createApp, ref, computed } from 'vue'
import axios from 'axios'

createApp({
  setup() {
    const token = ref('')
    const plate = ref('TEST-001')
    const tab = ref('owner')
    const map = ref({ width: 40, height: 25, spots: [], elements: [], recommendation: null, routePath: [] })
    const report = ref({
      totalSpots: 0, occupiedSpots: 0, freeSpots: 0, activeSessions: 0,
      todayRevenue: 0, todayEntries: 0, unresolvedAlerts: 0, recentDailyRevenue: []
    })
    const loading = ref(false)
    const message = ref('')

    const gridStyle = computed(() => ({
      gridTemplateColumns: `repeat(${map.value.width}, 18px)`,
      gridTemplateRows: `repeat(${map.value.height}, 18px)`
    }))

    const occupancyRate = computed(() => {
      if (!report.value.totalSpots) return 0
      return Math.round((report.value.occupiedSpots / report.value.totalSpots) * 100)
    })

    const call = async (method, url, body) => {
      const headers = token.value ? { Authorization: `Bearer ${token.value}` } : {}
      const res = await axios({ method, url: `http://localhost:8080${url}`, data: body, headers })
      const payload = res.data
      if (payload && payload.success === false) throw new Error(payload.message || 'Request failed')
      return payload?.data
    }

    const isRouteCell = (x, y) => (map.value.routePath || []).some(p => p.x === x && p.y === y)

    const cellType = (x, y) => {
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

    const loadMap = async () => {
      loading.value = true
      message.value = ''
      try {
        map.value = await call('get', '/api/driver/map-overview')
      } catch (e) {
        message.value = e?.response?.data?.message || '加载地图失败，请确认后端已启动并完成登录。'
      } finally {
        loading.value = false
      }
    }

    const doEntry = async () => {
      try {
        await call('post', '/api/driver/entry', { plateNumber: plate.value })
        await loadMap()
        message.value = '入场成功'
      } catch (e) {
        message.value = e?.response?.data?.message || '入场失败'
      }
    }

    const loadReport = async () => {
      loading.value = true
      message.value = ''
      try {
        report.value = await call('get', '/api/admin/reports/overview')
      } catch (e) {
        message.value = e?.response?.data?.message || '加载报表失败，请使用管理员token。'
      } finally {
        loading.value = false
      }
    }

    return { token, plate, tab, map, report, occupancyRate, loading, message, gridStyle, cellType, loadMap, doEntry, loadReport }
  },
  template: `
  <div style="font-family: Arial; padding: 16px; max-width: 1200px; margin: auto;">
    <h2>Garage Web Demo</h2>
    <div style="display:flex; gap:8px; margin-bottom:10px; align-items:center;">
      <input v-model="token" placeholder="JWT Token" style="width:360px;" />
      <button @click="tab='owner'">车主视图</button>
      <button @click="tab='admin'">管理员看板</button>
      <span>{{ loading ? 'loading...' : '' }}</span>
    </div>
    <p style="color:#d9534f;">{{ message }}</p>

    <div v-if="tab==='owner'">
      <div style="display:flex; gap:8px; margin-bottom:10px; align-items:center;">
        <input v-model="plate" placeholder="plate number" />
        <button @click="loadMap">加载地图</button>
        <button @click="doEntry">模拟入场</button>
      </div>
      <p v-if="map.recommendation">推荐车位: <b>{{ map.recommendation.spotCode }}</b>, score={{ map.recommendation.score }}</p>
      <p v-if="map.routePath && map.routePath.length">引导路径节点数: {{ map.routePath.length }}</p>
      <div :style="gridStyle" style="display:grid; gap:1px; background:#ddd; padding:8px; width: fit-content;">
        <div v-for="y in map.height" :key="'r'+y" style="display:contents;">
          <div v-for="x in map.width" :key="x+'-'+y" :class="cellType(x-1,y-1)" style="width:18px; height:18px;"></div>
        </div>
      </div>
    </div>

    <div v-else>
      <div style="display:flex; gap:8px; margin-bottom:12px;">
        <button @click="loadReport">刷新看板</button>
      </div>
      <div style="display:grid; grid-template-columns: repeat(4, minmax(140px,1fr)); gap:10px;">
        <div class="card"><h4>总车位</h4><p>{{ report.totalSpots }}</p></div>
        <div class="card"><h4>占用车位</h4><p>{{ report.occupiedSpots }}</p></div>
        <div class="card"><h4>空闲车位</h4><p>{{ report.freeSpots }}</p></div>
        <div class="card"><h4>占用率</h4><p>{{ occupancyRate }}%</p></div>
        <div class="card"><h4>今日进场</h4><p>{{ report.todayEntries }}</p></div>
        <div class="card"><h4>在场会话</h4><p>{{ report.activeSessions }}</p></div>
        <div class="card"><h4>今日收入</h4><p>¥ {{ report.todayRevenue }}</p></div>
        <div class="card"><h4>未处理告警</h4><p>{{ report.unresolvedAlerts }}</p></div>
      </div>
      <h4 style="margin-top:14px;">近7日收入</h4>
      <table border="1" cellspacing="0" cellpadding="6" style="border-collapse: collapse; width: 100%;">
        <thead><tr><th>日期</th><th>收入(¥)</th></tr></thead>
        <tbody>
          <tr v-for="p in report.recentDailyRevenue" :key="p.day"><td>{{ p.day }}</td><td>{{ p.revenue }}</td></tr>
        </tbody>
      </table>
    </div>
  </div>
  `
}).mount('#app')

const style = document.createElement('style')
style.textContent = `
.road{background:#f7f7f7}
.route-path{background:#8e44ad}
.spot-free{background:#67c23a}
.spot-occupied{background:#f56c6c}
.spot-recommended{background:#e6a23c}
.element-pillar{background:#606266}
.element-elevator{background:#409eff}
.element-entrance{background:#909399}
.element-boundary{background:#303133}
.element-no_parking{background:#c0c4cc}
.card{border:1px solid #ddd;padding:8px;border-radius:6px;background:#fafafa}
.card h4{margin:0 0 4px 0;font-size:13px;color:#666}
.card p{margin:0;font-size:20px;font-weight:700}
`
document.head.appendChild(style)
