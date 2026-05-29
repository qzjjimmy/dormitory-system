<template>
  <div class="admin-page">
    <div class="admin-stats">
      <div><span>系统用户</span><b>{{ stats.users || 0 }}</b></div>
      <div><span>宿舍房间</span><b>{{ stats.rooms || 0 }}</b></div>
      <div><span>报修工单</span><b>{{ stats.repairs || 0 }}</b></div>
      <div><span>访客预约</span><b>{{ stats.visitors || 0 }}</b></div>
      <div><span>费用总额</span><b>{{ stats.fees || 0 }}</b></div>
    </div>
    <section class="content-card">
      <div class="card-title"><h2>数据概览</h2></div>
      <div class="bar-list">
        <div v-for="row in safeCategories" :key="row.category">
          <label>{{ categoryName(row.category) }}</label>
          <p><i :style="{ width: barWidth(row.value) }"></i></p>
          <b>{{ row.value }}</b>
        </div>
        <div v-if="safeCategories.length === 0" class="empty">暂无数据</div>
      </div>
    </section>
  </div>
</template>

<script>
export default {
  name: 'AdminDashboard',
  props: {
    stats: { type: Object, default: () => ({}) },
    categoryName: { type: Function, default: (cat) => cat },
    barWidth: { type: Function, default: () => '8%' }
  },
  computed: {
    safeCategories() {
      return Array.isArray(this.stats.categories) ? this.stats.categories : []
    }
  }
}
</script>
