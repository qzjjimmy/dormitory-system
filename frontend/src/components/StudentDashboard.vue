<template>
  <div class="student-dashboard">
    <section class="welcome-row">
      <div>
        <h1>你好，{{ displayName }}</h1>
        <p>{{ beijingDateText }}，开启高效的一天吧！</p>
      </div>
      <div class="weather">☔ <b>{{ weatherText }}</b></div>
    </section>

    <div class="dashboard-grid">
      <section class="hero-banner">
        <div>
          <h2>智慧生活 · 安全第一</h2>
          <p>严禁在寝室使用大功率违章电器</p>
        </div>
      </section>

      <section class="notice-card">
        <div class="card-title inline"><h2>校园公告</h2><a>更多</a></div>
        <ul class="notice-list">
          <li v-for="(item, idx) in announcements" :key="item.id">
            <i :class="{ red: idx === 0 }"></i>
            <div><b>{{ item.title }}</b><span>{{ item.createdAt ? item.createdAt.slice(0, 10) : '' }}</span></div>
          </li>
          <li v-if="announcements.length === 0">
            <i></i><div><b>暂无公告</b><span></span></div>
          </li>
        </ul>
      </section>

      <button class="quick-card" @click="$emit('goDorm')">
        <span class="quick-icon blue">⌂</span>
        <b>我的宿舍</b>
        <strong>{{ shortDormRoom }}</strong>
        <em>查看详情 ›</em>
      </button>
      <button class="quick-card">
        <span class="quick-icon gold">▣</span>
        <b>账户余额</b>
        <strong>{{ feeInfo }}</strong>
        <em>立即充值 ›</em>
      </button>
      <button class="quick-card" @click="$emit('goRepair')">
        <span class="quick-icon blue">⚙</span>
        <b>报修申请</b>
        <strong>{{ repairCount }} 件</strong>
        <em>申请维修 ›</em>
      </button>

      <section class="score-banner">
        <div class="sb-circle">
          <strong>{{ hygieneScore }}</strong>
          <span>分</span>
        </div>
        <div class="sb-rows">
          <p><span>全楼排名</span><b>{{ rankText }}</b></p>
          <p><span>上次检查</span><b>{{ lastCheckResult }}</b></p>
        </div>
        <small>本月卫生综合评分</small>
      </section>

      <section class="roommate-card">
        <div class="card-title"><h2>我的室友</h2></div>
        <div class="compact-roommates">
          <div v-for="mate in filteredRoommates" :key="mate.name">
            <span class="avatar-img" :style="{ background: mate.color }">{{ mate.avatar }}</span>
            <b>{{ mate.name }}</b>
            <span>{{ mate.bed }}</span>
            <i>☻</i>
          </div>
        </div>
      </section>
    </div>
  </div>
</template>

<script>
export default {
  name: 'StudentDashboard',
  props: {
    displayName: { type: String, required: true },
    roommates: { type: Array, default: () => [] },
    beijingDateText: { type: String, default: '' },
    weatherText: { type: String, default: '' },
    announcements: { type: Array, default: () => [] },
    repairCount: { type: Number, default: 0 },
    dormRoom: { type: String, default: '' },
    feeInfo: { type: String, default: '¥ 0.00' },
    hygieneScore: { type: Number, default: 0 },
    rankText: { type: String, default: '-' },
    lastCheckResult: { type: String, default: '-' }
  },
  emits: ['goDorm', 'goRepair'],
  computed: {
    filteredRoommates() {
      return this.roommates.filter(item => !item.tag)
    },
    shortDormRoom() {
      // Truncate "芙蓉楼3 · 519室 · 2号床" → "芙蓉楼3 · 519室" to prevent overflow
      return this.dormRoom ? this.dormRoom.split(' · ').slice(0, 2).join(' · ') : ''
    }
  }
}
</script>
