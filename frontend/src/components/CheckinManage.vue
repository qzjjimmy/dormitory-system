<template>
  <div class="manage-space">
    <section class="content-card">
      <div class="card-title inline">
        <h2>入住管理</h2>
        <span>学生入住/退宿办理与在住人员统计</span>
        <button class="blue-btn" @click="showForm = true">+ 办理入住</button>
      </div>

      <div class="metric-grid manage">
        <div class="metric-card"><span>人</span><b>在住人员</b><strong>{{ records.filter(r => r.status === '在住').length }} 人</strong></div>
        <div class="metric-card"><span>房</span><b>登记记录</b><strong>{{ records.length }} 条</strong></div>
      </div>

      <div v-if="showForm" class="edit-form">
        <div class="form-grid">
          <div class="field"><label>姓名</label><input v-model="form.title" placeholder="学生姓名"></div>
          <div class="field"><label>宿舍/床位</label><input v-model="form.location" placeholder="例如：芙蓉楼3 · 519 · 2床"></div>
          <div class="field"><label>状态</label>
            <select v-model="form.status">
              <option value="在住">在住</option>
              <option value="已退宿">已退宿</option>
              <option value="请假">请假</option>
            </select>
          </div>
          <div class="field wide"><label>备注</label><textarea v-model="form.content" placeholder="学号、年级等信息"></textarea></div>
          <div class="field wide actions">
            <button class="blue-btn" @click="saveForm">保存</button>
            <button @click="showForm = false; editingId = null">取消</button>
          </div>
        </div>
      </div>
    </section>

    <section class="content-card">
      <div class="manage-grid">
        <div v-for="row in records" :key="row.id" class="resident">
          <span class="avatar-dot" :style="{ background: residentColor(row.owner) }">{{ row.owner ? row.owner.slice(0, 1) : '?' }}</span>
          <div>
            <b>{{ row.owner || row.title }}</b>
            <p>{{ row.location }}</p>
          </div>
          <em :class="{ away: row.status !== '在住' }">{{ row.status }}</em>
          <div class="resident-actions">
            <button class="link-btn" @click="editRow(row)">详情</button>
            <button class="link-btn danger" @click="removeRow(row.id)">删除</button>
          </div>
        </div>
        <div v-if="records.length === 0" class="empty" style="grid-column: 1 / -1">暂无入住记录</div>
      </div>
    </section>
  </div>
</template>

<script>
import { fetchRecords, createRecord, updateRecord, deleteRecord } from '../api.js'

const COLORS = ['#4b8fe8', '#5aac6e', '#d98c45', '#c47d8b', '#6c8ebf', '#8c7db5', '#b58d7f', '#8798a7']

export default {
  name: 'CheckinManage',
  data() {
    return {
      records: [],
      showForm: false,
      editingId: null,
      form: { title: '', location: '', status: '在住', content: '' }
    }
  },
  mounted() {
    this.loadRecords()
  },
  methods: {
    async loadRecords() {
      try {
        const data = await fetchRecords('student', '')
        this.records = data.rows || data
      } catch (e) { /* ignore */ }
    },
    async saveForm() {
      if (!this.form.title.trim()) return
      try {
        if (this.editingId) {
          await updateRecord(this.editingId, {
            ...this.form,
            category: 'student',
            owner: this.form.title
          })
        } else {
          await createRecord({
            category: 'student',
            title: this.form.title + '入住登记',
            owner: this.form.title,
            location: this.form.location,
            amount: 0,
            status: this.form.status,
            content: this.form.content
          })
        }
        this.showForm = false
        this.editingId = null
        this.form = { title: '', location: '', status: '在住', content: '' }
        this.loadRecords()
      } catch (e) { /* ignore */ }
    },
    editRow(row) {
      this.editingId = row.id
      this.form = {
        title: row.owner || row.title,
        location: row.location,
        status: row.status,
        content: row.content
      }
      this.showForm = true
    },
    async removeRow(id) {
      if (!window.confirm('确认删除？')) return
      try {
        await deleteRecord(id)
        this.loadRecords()
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
