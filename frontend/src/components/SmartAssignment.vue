<template>
  <section class="content-card">
    <div class="card-title">
      <h2>智能宿舍分配</h2>
      <span>基于学生特征的自动分配 & 调宿推荐</span>
    </div>

    <!-- Stats -->
    <div class="action-bar">
      <div class="stats-row">
        <span>待分配学生: <b>{{ summary.unassigned || 0 }}</b> 人</span>
        <span>已分配宿舍: <b>{{ summary.roomCount || 0 }}</b> 间</span>
        <span>空余床位: <b>{{ summary.emptyBeds || 0 }}</b> 床</span>
      </div>
      <button class="blue-btn" @click="runAssignment" :disabled="running">
        {{ running ? '分配中...' : '⚡ 开始新生分配' }}
      </button>
      <p v-if="resultMessage" class="result-msg">{{ resultMessage }}</p>
    </div>

    <!-- Heatmap -->
    <div v-if="heatmap.labels && heatmap.labels.length > 1" class="heatmap-section">
      <h3>📊 学生兼容性热力图 <small>— 颜色越深越匹配，帮助预判分配效果</small></h3>
      <div class="heatmap-scroll">
        <table class="heatmap-table">
          <thead>
            <tr>
              <th></th>
              <th v-for="s in heatmap.labels" :key="s.id" class="heatmap-col-label">{{ s.name }}</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="(row, i) in heatmap.matrix" :key="i">
              <td class="heatmap-row-label">{{ heatmap.labels[i].name }}</td>
              <td v-for="(score, j) in row" :key="j"
                  :class="['heatmap-cell', { self: i === j }]"
                  :style="{ background: heatColor(score) }"
                  :title="heatmap.labels[i].name + ' ↔ ' + heatmap.labels[j].name + ': ' + score + '分'">
                <span v-if="i !== j">{{ score }}</span>
                <span v-else>—</span>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
      <div class="heatmap-legend">
        <span class="legend-dot" style="background:#e74c3c"></span> 低兼容
        <span class="legend-dot" style="background:#f39c12"></span>
        <span class="legend-dot" style="background:#2ecc71"></span> 高兼容
      </div>
    </div>

    <!-- Assignment result with score rings -->
    <div v-if="rooms.length > 0" class="assignment-result">
      <h3>分配结果 · 兼容度仪表盘</h3>
      <div class="room-dash-grid">
        <div v-for="room in rooms" :key="room.roomNo" class="asgn-room">
          <div class="asgn-room-header">
            <strong>{{ room.roomNo }}</strong>
            <span class="room-type-badge">{{ room.members.length === 6 ? '6人间' : '4人间' }}</span>
          </div>

          <!-- Score ring -->
          <div class="score-ring-wrap">
            <div class="score-ring" :style="{ background: ringGradient(room.avgCompatibility) }">
              <div class="score-ring-inner">
                <strong>{{ room.avgCompatibility }}</strong>
                <span>分</span>
              </div>
            </div>
            <p class="score-label">{{ scoreLabel(room.avgCompatibility) }}</p>
          </div>

          <!-- Bed grid -->
          <div class="bed-grid" :style="{ gridTemplateColumns: 'repeat(' + (room.members.length > 4 ? 6 : 4) + ', 1fr)' }">
            <div v-for="(m, idx) in room.members" :key="m.id" class="bed-cell occupied" :title="tooltip(m)">
              <span class="bed-num">{{ idx + 1 }}号床</span>
              <span class="bed-name">{{ m.realName }}</span>
              <span class="bed-class">{{ m.majorClass }}</span>
            </div>
            <div v-for="n in ((room.members.length > 4 ? 6 : 4) - room.members.length)" :key="'e'+n" class="bed-cell empty">
              <span class="bed-num">{{ room.members.length + n }}号床</span>
              <span class="bed-name">空床位</span>
            </div>
          </div>
        </div>
      </div>

      <!-- Before/After comparison -->
      <div v-if="heatmap.labels && heatmap.labels.length > 1" class="comparison-bar">
        <div class="comp-item">
          <span class="comp-label">随机分配预估均分</span>
          <span class="comp-val low">≈ {{ randomAvg }}</span>
        </div>
        <div class="comp-arrow">→</div>
        <div class="comp-item">
          <span class="comp-label">贪心分配实际均分</span>
          <span class="comp-val high">{{ greedyAvg }} 分</span>
        </div>
        <div class="comp-item">
          <span class="comp-label">提升幅度</span>
          <span class="comp-val boost">+{{ (greedyAvg - randomAvg).toFixed(1) }} 分</span>
        </div>
      </div>

      <button v-if="rooms.length > 0" class="green-btn block" @click="confirmAll">确认以上分配</button>
    </div>

    <!-- Transfer recommendation -->
    <div class="transfer-section">
      <h3>调宿匹配推荐</h3>
      <div class="transfer-row">
        <select v-model="selectedStudentId">
          <option :value="null" disabled>-- 选择待调宿学生 --</option>
          <option v-for="s in studentList" :key="s.id" :value="s.id">{{ s.realName }} ({{ s.majorClass || '未知班级' }})</option>
        </select>
        <button class="blue-btn" @click="getRecommendations" :disabled="!selectedStudentId">推荐宿舍</button>
      </div>

      <div v-if="recommendations.length > 0" class="recommend-list">
        <div v-for="(rec, idx) in recommendations" :key="rec.roomNo" class="recommend-card">
          <div class="rec-rank">#{{ idx + 1 }}</div>
          <div class="rec-info">
            <strong>{{ rec.roomNo }}</strong>
            <span>现有 {{ rec.currentCount }}/4 人</span>
            <span class="rec-score">匹配度 {{ rec.compatibility }} 分</span>
            <span class="rec-reason">{{ rec.reason }}</span>
          </div>
        </div>
      </div>
      <p v-if="recommendations.length === 0 && selectedStudentId && recLoaded" class="dim">暂无推荐结果</p>
    </div>
  </section>
</template>

<script>
import { fetchUsers, runAssignment, recommendTransfer, confirmAssignment, fetchHeatmap, fetchRooms } from '../api.js'

export default {
  name: 'SmartAssignment',
  data() {
    return {
      running: false,
      resultMessage: '',
      rooms: [],
      summary: { unassigned: 0, roomCount: 0, emptyBeds: 0 },
      studentList: [],
      selectedStudentId: null,
      recommendations: [],
      recLoaded: false,
      heatmap: { labels: [], matrix: [] }
    }
  },
  computed: {
    greedyAvg() {
      if (!this.rooms.length) return 0
      const sum = this.rooms.reduce((s, r) => s + (r.avgCompatibility || 0), 0)
      return Math.round(sum / this.rooms.length)
    },
    randomAvg() {
      // Estimate: average of all pairwise scores in heatmap
      if (!this.heatmap.matrix || !this.heatmap.matrix.length) return 0
      let sum = 0, count = 0
      for (let i = 0; i < this.heatmap.matrix.length; i++) {
        for (let j = i + 1; j < this.heatmap.matrix[i].length; j++) {
          sum += this.heatmap.matrix[i][j]
          count++
        }
      }
      return count > 0 ? Math.round(sum / count) : 0
    }
  },
  mounted() {
    this.loadSummary()
    this.loadHeatmap()
    this.loadExistingRooms()
  },
  methods: {
    async loadExistingRooms() {
      try {
        const data = await fetchRooms()
        if (data.rooms) this.rooms = data.rooms
      } catch (e) { /* ignore */ }
    },
    async loadSummary() {
      try {
        const data = await fetchUsers('', 1, 200)
        const all = data.rows || []
        const students = all.filter(u => u.role === 'student')
        const assigned = students.filter(s => s.roomNo)
        const unassigned = students.filter(s => !s.roomNo)
        const roomSet = new Set()
        assigned.forEach(s => {
          const prefix = (s.roomNo || '').replace(/ \· \d+号床.*$/, '')
          if (prefix) roomSet.add(prefix)
        })
        this.summary.unassigned = unassigned.length
        this.summary.roomCount = roomSet.size
        this.summary.emptyBeds = Math.max(0, (roomSet.size * 4) - assigned.length)
        this.studentList = students
      } catch (e) { /* ignore */ }
    },
    async loadHeatmap() {
      try {
        const data = await fetchHeatmap()
        if (data.labels) this.heatmap = data
      } catch (e) { /* ignore */ }
    },
    heatColor(score) {
      // Red(0) → Orange(50) → Green(100)
      if (score <= 30) {
        const t = score / 30
        const r = 231, g = Math.round(76 + t * 83), b = Math.round(60 + t * 14)
        return `rgb(${r},${g},${b})`
      }
      if (score <= 60) {
        const t = (score - 30) / 30
        const r = 231, g = Math.round(159 + t * 36), b = Math.round(74 + t * 100)
        return `rgb(${r},${g},${b})`
      }
      const t = (score - 60) / 40
      const r = Math.round(231 - t * 185), g = Math.round(195 + t * 10), b = Math.round(174 - t * 128)
      return `rgb(${r},${g},${b})`
    },
    ringGradient(score) {
      const deg = Math.round(score * 3.6)
      const color = score >= 70 ? '#2ecc71' : score >= 45 ? '#f39c12' : '#e74c3c'
      return `conic-gradient(${color} 0 ${deg}deg, #edf2f7 ${deg}deg 360deg)`
    },
    scoreLabel(score) {
      if (score >= 75) return '高度和谐'
      if (score >= 55) return '良好兼容'
      if (score >= 35) return '一般匹配'
      return '存在冲突'
    },
    async runAssignment() {
      this.running = true
      this.resultMessage = ''
      try {
        const data = await runAssignment()
        const newRooms = data.rooms || []
        this.resultMessage = data.message || `分配完成！共 ${newRooms.length} 间新宿舍，${data.unassignedCount || 0} 人未分配`
        // Reload all rooms (including existing ones)
        await this.loadExistingRooms()
        await this.loadSummary()
      } catch (e) {
        this.resultMessage = '分配失败: ' + e.message
      } finally {
        this.running = false
      }
    },
    async getRecommendations() {
      if (!this.selectedStudentId) return
      this.recLoaded = false
      try {
        const data = await recommendTransfer(this.selectedStudentId)
        this.recommendations = data || []
        this.recLoaded = true
      } catch (e) {
        this.resultMessage = '推荐失败: ' + e.message
        this.recLoaded = true
      }
    },
    tooltip(m) {
      return `作息:${m.sleepHabit} 抽烟:${m.smoking} 卫生:${m.cleanliness} 游戏:${m.gaming}`
    },
    async confirmAll() {
      if (!confirm('确认执行此分配方案？将更新学生宿舍信息。')) return
      const assignments = []
      this.rooms.forEach(room => {
        room.members.forEach((m, idx) => {
          assignments.push({ studentId: m.id, roomNo: room.roomNo, bedNo: (idx + 1) + '号床' })
        })
      })
      try {
        await confirmAssignment(assignments)
        alert('分配已确认！')
        this.loadSummary()
      } catch (e) { alert('确认失败: ' + e.message) }
    }
  }
}
</script>
