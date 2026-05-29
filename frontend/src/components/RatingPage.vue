<template>
  <div class="rating-page">
    <div class="split-layout">
      <section class="content-card">
        <div class="card-title"><h2>服务评价</h2><span>评价宿舍服务与维修处理</span></div>
        <div class="score-hero">
          <div class="score-circle">
            <strong>{{ avgScore }}</strong>
            <span>分</span>
          </div>
          <div class="score-meta">
            <p><span>全楼排名</span><b>第 12 名</b></p>
            <p><span>本月提交</span><b>{{ records.length }} 条</b></p>
          </div>
        </div>
        <small>宿舍卫生与服务综合评分</small>
      </section>

      <section class="content-card">
        <div class="card-title"><h3>评价记录</h3></div>
        <div v-if="records.length === 0" class="empty">暂无评价记录</div>
        <div v-for="row in records" :key="row.id" class="rating-record">
          <div class="rating-top">
            <b>{{ row.title }}</b>
            <span class="state" :class="{ success: row.status === '优秀', warn: row.status !== '优秀' }">{{ row.status }}</span>
          </div>
          <p>{{ row.content }}</p>
          <small>{{ row.location || '' }}</small>
        </div>
      </section>
    </div>

    <section class="content-card">
      <div class="card-title"><h3>提交评价</h3></div>
      <div class="form-grid">
        <div class="field">
          <label>评价类型</label>
          <input v-model="form.title" placeholder="例如：报修服务评价">
        </div>
        <div class="field">
          <label>评分</label>
          <select v-model="form.status">
            <option value="优秀">优秀（五星）</option>
            <option value="良好">良好（四星）</option>
            <option value="合格">合格（三星）</option>
            <option value="待改进">待改进</option>
          </select>
        </div>
        <div class="field wide">
          <label>评价内容</label>
          <textarea v-model="form.content" placeholder="请描述你的评价和建议"></textarea>
        </div>
        <div class="field wide actions">
          <button class="blue-btn" @click="submit">提交评价</button>
        </div>
      </div>
    </section>
  </div>
</template>

<script>
import { fetchRecords, createRecord } from '../api.js'

export default {
  name: 'RatingPage',
  props: {
    displayName: { type: String, default: '' }
  },
  data() {
    return {
      form: { title: '', status: '优秀', content: '' },
      records: []
    }
  },
  computed: {
    avgScore() {
      const map = { '优秀': 95, '良好': 85, '合格': 70, '待改进': 50 }
      if (this.records.length === 0) return 98
      const total = this.records.reduce((sum, r) => sum + (map[r.status] || 70), 0)
      return Math.round(total / this.records.length)
    }
  },
  mounted() {
    this.loadRecords()
  },
  methods: {
    async loadRecords() {
      try {
        const data = await fetchRecords('hygiene', '')
        this.records = data.rows || data
      } catch (e) { /* ignore */ }
    },
    async submit() {
      if (!this.form.title.trim()) return
      try {
        await createRecord({
          category: 'hygiene',
          title: this.form.title,
          owner: this.displayName,
          location: '',
          amount: 0,
          status: this.form.status,
          content: this.form.content
        })
        this.form = { title: '', status: '优秀', content: '' }
        this.loadRecords()
      } catch (e) { /* ignore */ }
    }
  }
}
</script>
