<template>
  <section class="content-card">
    <div class="card-title inline">
      <h2>调宿申请</h2>
      <span>申请更换宿舍或床位</span>
      <button class="blue-btn" @click="showForm = !showForm">{{ showForm ? '取消' : '+ 提交申请' }}</button>
    </div>

    <div v-if="showForm" class="edit-form">
      <div class="form-grid">
        <div class="field"><label>当前宿舍</label><input :value="currentRoom" readonly></div>
        <div class="field"><label>期望宿舍</label><input v-model="form.location" placeholder="例如：芙蓉楼3 · 620室"></div>
        <div class="field wide"><label>申请原因</label><textarea v-model="form.content" placeholder="请说明申请调宿的原因"></textarea></div>
        <div class="field wide actions">
          <button class="blue-btn" @click="submit">提交申请</button>
          <button class="reg-btn outline" style="width:auto;padding:8px 20px;font-size:13px" @click="loadRecommendations">🔍 查看推荐宿舍</button>
          <button @click="showForm = false">取消</button>
        </div>
      </div>
    </div>

    <!-- Recommendation panel -->
    <div v-if="recommendations.length > 0" class="recommend-panel">
      <h4>为你推荐的宿舍（匹配度 Top 3）</h4>
      <div v-for="(rec, idx) in recommendations" :key="rec.roomNo" class="recommend-card" @click="form.location = rec.roomNo">
        <div class="rec-rank">#{{ idx + 1 }}</div>
        <div class="rec-info">
          <strong>{{ rec.roomNo }}</strong>
          <span>现有 {{ rec.currentCount }}/4 人</span>
          <span class="rec-score">匹配度 {{ rec.compatibility }} 分</span>
          <span class="rec-reason">{{ rec.reason }}</span>
          <span class="rec-hint">点击填入期望宿舍</span>
        </div>
      </div>
    </div>
    <p v-if="recMsg" class="reg-error" style="text-align:center">{{ recMsg }}</p>

    <div class="table-wrap" style="margin-top: 18px">
      <table>
        <thead>
          <tr><th>期望宿舍</th><th>申请原因</th><th>状态</th><th>操作</th></tr>
        </thead>
        <tbody>
          <tr v-for="row in records" :key="row.id">
            <td>{{ row.location }}</td>
            <td>{{ row.content }}</td>
            <td><span class="state" :class="{ warn: isPending(row.status) }">{{ row.status }}</span></td>
            <td>
              <button class="link-btn danger" @click="removeRecord(row.id)">删除</button>
            </td>
          </tr>
          <tr v-if="records.length === 0">
            <td colspan="4" class="empty">暂无申请记录</td>
          </tr>
        </tbody>
      </table>
    </div>
  </section>
</template>

<script>
import { fetchRecords, createRecord, deleteRecord } from '../api.js'
import { recommendTransfer } from '../api.js'

export default {
  name: 'TransferPage',
  props: {
    displayName: { type: String, default: '' },
    currentRoom: { type: String, default: '' }
  },
  data() {
    return {
      showForm: false,
      form: { location: '', content: '' },
      records: [],
      recommendations: [],
      recMsg: ''
    }
  },
  mounted() {
    this.loadRecords()
  },
  methods: {
    async loadRecords() {
      try {
        const data = await fetchRecords('transfer', '')
        this.records = data.rows || data
      } catch (e) { /* ignore */ }
    },
    async submit() {
      if (!this.form.location.trim()) return
      try {
        await createRecord({
          category: 'transfer',
          title: '调宿申请：' + this.form.location,
          owner: this.displayName,
          location: this.form.location,
          amount: 0,
          status: '审核中',
          content: this.form.content
        })
        this.form = { location: '', content: '' }
        this.showForm = false
        this.loadRecords()
      } catch (e) { /* ignore */ }
    },
    async removeRecord(id) {
      if (!window.confirm('确认删除？')) return
      try {
        await deleteRecord(id)
        this.loadRecords()
      } catch (e) { /* ignore */ }
    },
    async loadRecommendations() {
      this.recommendations = []
      this.recMsg = ''
      try {
        const user = JSON.parse(sessionStorage.getItem('dorm-user') || '{}')
        if (!user.id) { this.recMsg = '无法获取用户信息'; return }
        const data = await recommendTransfer(user.id)
        this.recommendations = data || []
        if (this.recommendations.length === 0) this.recMsg = '暂无可推荐的宿舍'
      } catch (e) { this.recMsg = '推荐失败: ' + e.message }
    },
    isPending(status) {
      return ['待审核', '审核中'].includes(status)
    }
  }
}
</script>
