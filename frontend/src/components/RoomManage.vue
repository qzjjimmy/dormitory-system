<template>
  <div class="manage-space">
    <section class="content-card">
      <div class="card-title inline">
        <h2>房间管理</h2>
        <span>已分配宿舍（根据学生数据自动聚合）</span>
      </div>

      <div class="metric-grid manage">
        <div class="metric-card"><span>房</span><b>宿舍数量</b><strong>{{ rooms.length }} 间</strong></div>
        <div class="metric-card"><span>人</span><b>已入住</b><strong>{{ totalOccupied }} 人</strong></div>
        <div class="metric-card"><span>床</span><b>空余床位</b><strong>{{ totalEmpty }} 床</strong></div>
      </div>
    </section>

    <section class="content-card">
      <div class="room-grid">
        <div v-for="room in rooms" :key="room.roomNo" class="room-card" :class="{ full: room.occupied === room.capacity }">
          <div class="room-card-top">
            <span class="room-icon">⌂</span>
            <b>{{ room.roomNo }}</b>
            <span class="state" :class="{ warn: room.occupied === room.capacity, success: room.occupied < room.capacity }">{{ room.occupied === room.capacity ? '已住满' : '可入住' }}</span>
          </div>
          <p>{{ room.capacity }}人间 · {{ room.building }}</p>
          <small>
            <span v-for="(m, idx) in room.members" :key="m.id">{{ m.realName }}（{{ m.bed }}）{{ idx < room.members.length - 1 ? '、' : '' }}</span>
          </small>
          <div class="room-card-actions">
            <span style="font-size:12px;color:#8895a0">入住 {{ room.occupied }}/{{ room.capacity }} · 空 {{ room.capacity - room.occupied }} 床</span>
          </div>
        </div>
        <div v-if="rooms.length === 0" class="empty" style="grid-column: 1 / -1">暂无已分配宿舍</div>
      </div>
    </section>
  </div>
</template>

<script>
import { fetchUsers } from '../api.js'

export default {
  name: 'RoomManage',
  data() {
    return { rooms: [] }
  },
  computed: {
    totalOccupied() { return this.rooms.reduce((s, r) => s + r.occupied, 0) },
    totalEmpty() { return this.rooms.reduce((s, r) => s + (r.capacity - r.occupied), 0) }
  },
  mounted() { this.load() },
  methods: {
    async load() {
      try {
        const data = await fetchUsers('', 1, 200)
        const students = (data.rows || []).filter(u => u.role === 'student' && u.roomNo)
        const groups = {}
        students.forEach(s => {
          const roomNo = s.roomNo || ''
          const prefix = roomNo.replace(/ · \d+号床.*$/, '')
          if (!prefix) return
          if (!groups[prefix]) groups[prefix] = []
          groups[prefix].push({
            id: s.id,
            realName: s.realName,
            bed: (roomNo.match(/(\d+号床)/) || [])[0] || '-'
          })
        })
        this.rooms = Object.entries(groups).map(([roomNo, members]) => ({
          roomNo,
          building: roomNo.split(' · ')[0] || '',
          members,
          occupied: members.length,
          capacity: members.length > 4 ? 6 : 4
        }))
      } catch (e) { /* ignore */ }
    }
  }
}
</script>
