<template>
  <div class="manage-space">
    <section class="content-card">
      <div class="card-title inline">
        <h2>入住管理</h2>
        <span>在住学生信息（来自系统用户）</span>
      </div>

      <div class="metric-grid manage">
        <div class="metric-card"><span>人</span><b>已分配宿舍</b><strong>{{ assignedCount }} 人</strong></div>
        <div class="metric-card"><span>待</span><b>待分配</b><strong>{{ unassignedCount }} 人</strong></div>
        <div class="metric-card"><span>总</span><b>学生总数</b><strong>{{ students.length }} 人</strong></div>
      </div>
    </section>

    <section class="content-card">
      <div class="manage-grid">
        <div v-for="s in students" :key="s.id" class="resident">
          <span class="avatar-dot" :style="{ background: residentColor(s.realName) }">{{ (s.realName || '?').slice(0, 1) }}</span>
          <div>
            <b>{{ s.realName }}</b>
            <p>{{ s.roomNo || '未分配' }}</p>
          </div>
          <em :class="{ away: !s.roomNo }">{{ s.roomNo ? '在住' : '待分配' }}</em>
          <span style="font-size:12px;color:#8895a0">{{ s.majorClass || '' }}</span>
        </div>
        <div v-if="students.length === 0" class="empty" style="grid-column: 1 / -1">暂无学生数据</div>
      </div>
    </section>
  </div>
</template>

<script>
import { fetchUsers } from '../api.js'

const COLORS = ['#4b8fe8', '#5aac6e', '#d98c45', '#c47d8b', '#6c8ebf', '#8c7db5', '#b58d7f', '#8798a7']

export default {
  name: 'CheckinManage',
  data() {
    return { students: [] }
  },
  computed: {
    assignedCount() { return this.students.filter(s => s.roomNo).length },
    unassignedCount() { return this.students.filter(s => !s.roomNo).length }
  },
  mounted() { this.load() },
  methods: {
    async load() {
      try {
        const data = await fetchUsers('', 1, 200)
        this.students = (data.rows || []).filter(u => u.role === 'student')
      } catch (e) { /* ignore */ }
    },
    residentColor(name) {
      let hash = 0
      if (!name) return COLORS[0]
      for (let i = 0; i < name.length; i++) hash = name.charCodeAt(i) + ((hash << 5) - hash)
      return COLORS[Math.abs(hash) % COLORS.length]
    }
  }
}
</script>
