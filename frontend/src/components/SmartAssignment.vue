<template>
  <section class="content-card">
    <div class="card-title">
      <h2>智能宿舍分配</h2>
      <span>基于学生特征的自动分配 & 调宿推荐</span>
    </div>

    <div class="action-bar">
      <div class="stats-row">
        <span>待分配学生: <b>{{ summary.unassigned || 0 }}</b> 人</span>
        <span>已分配宿舍: <b>{{ summary.roomCount || 0 }}</b> 间</span>
        <span>空余床位: <b>{{ summary.emptyBeds || 0 }}</b> 床</span>
      </div>
      <button class="blue-btn" @click="runAssignment" :disabled="running">
        {{ running ? '分配中...' : '开始新生分配' }}
      </button>
      <p v-if="resultMessage" class="result-msg">{{ resultMessage }}</p>
    </div>

    <div v-if="rooms.length > 0" class="assignment-result">
      <h3>分配结果</h3>
      <div v-for="room in rooms" :key="room.roomNo" class="asgn-room">
        <div class="asgn-room-header">
          <strong>{{ room.roomNo }}</strong>
          <span class="compat-badge">兼容度均分: {{ room.avgCompatibility }}</span>
        </div>
        <div class="bed-grid" :style="{ gridTemplateColumns: 'repeat(' + (room.capacity || 4) + ', 1fr)' }">
          <div v-for="(m, idx) in room.members" :key="m.id" class="bed-cell occupied" :title="tooltip(m)">
            <span class="bed-num">{{ idx + 1 }}号床</span>
            <span class="bed-name">{{ m.realName }}</span>
            <span class="bed-class">{{ m.majorClass }}</span>
          </div>
          <div v-for="n in ((room.capacity || 4) - room.members.length)" :key="'e'+n" class="bed-cell empty">
            <span class="bed-num">{{ room.members.length + n }}号床</span>
            <span class="bed-name">空床位</span>
          </div>
        </div>
      </div>

      <button v-if="rooms.length > 0" class="green-btn block" @click="confirmAll">确认以上分配</button>
    </div>

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
import { fetchUsers, runAssignment, recommendTransfer, confirmAssignment } from '../api.js'

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
      recLoaded: false
    }
  },
  mounted() {
    this.loadSummary()
  },
  methods: {
    async loadSummary() {
      try {
        const data = await fetchUsers('', 1, 200)
        const all = data.rows || []
        const students = all.filter(u => u.role === 'student')
        const assigned = students.filter(s => s.roomNo)
        const unassigned = students.filter(s => !s.roomNo)
        const roomSet = new Set()
        assigned.forEach(s => {
          const prefix = (s.roomNo || '').replace(/ · \d+号床.*$/, '')
          if (prefix) roomSet.add(prefix)
        })
        this.summary.unassigned = unassigned.length
        this.summary.roomCount = roomSet.size
        this.summary.emptyBeds = Math.max(0, (roomSet.size * 4) - assigned.length)
        this.studentList = students
      } catch (e) {
        // ignore
      }
    },
    async runAssignment() {
      this.running = true
      this.resultMessage = ''
      try {
        const data = await runAssignment()
        this.rooms = data.rooms || []
        this.resultMessage = data.message || `分配完成！共 ${this.rooms.length} 间宿舍，${data.unassignedCount || 0} 人未分配`
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
          assignments.push({
            studentId: m.id,
            roomNo: room.roomNo,
            bedNo: (idx + 1) + '号床'
          })
        })
      })
      try {
        await confirmAssignment(assignments)
        alert('分配已确认！')
        this.loadSummary()
      } catch (e) {
        alert('确认失败: ' + e.message)
      }
    }
  }
}
</script>
