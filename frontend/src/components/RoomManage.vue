<template>
  <div class="manage-space">
    <section class="content-card">
      <div class="card-title inline">
        <h2>房间管理</h2>
        <span>宿舍房间、床位与入住状态</span>
        <button class="blue-btn" @click="showForm = true">+ 新增房间</button>
      </div>

      <div class="metric-grid manage">
        <div class="metric-card"><span>房</span><b>房间总数</b><strong>{{ records.length }} 间</strong></div>
        <div class="metric-card"><span>床</span><b>已住满</b><strong>{{ records.filter(r => r.status === '已住满').length }} 间</strong></div>
        <div class="metric-card"><span>空</span><b>可入住</b><strong>{{ records.filter(r => r.status !== '已住满').length }} 间</strong></div>
      </div>

      <div v-if="showForm" class="edit-form">
        <div class="form-grid">
          <div class="field"><label>房间号</label><input v-model="form.title" placeholder="例如：519宿舍"></div>
          <div class="field"><label>楼栋/楼层</label><input v-model="form.location" placeholder="例如：芙蓉楼3 · 5层"></div>
          <div class="field"><label>状态</label>
            <select v-model="form.status">
              <option value="可入住">可入住</option>
              <option value="已住满">已住满</option>
              <option value="维护中">维护中</option>
            </select>
          </div>
          <div class="field wide"><label>房间配置</label><textarea v-model="form.content" placeholder="例如：4人间，空调，独立卫浴，朝南"></textarea></div>
          <div class="field wide actions">
            <button class="blue-btn" @click="saveForm">保存</button>
            <button @click="showForm = false; editingId = null">取消</button>
          </div>
        </div>
      </div>
    </section>

    <section class="content-card">
      <div class="room-grid">
        <div v-for="row in records" :key="row.id" class="room-card" :class="{ full: row.status === '已住满', maint: row.status === '维护中' }">
          <div class="room-card-top">
            <span class="room-icon">⌂</span>
            <b>{{ row.title }}</b>
            <span class="state" :class="{ warn: row.status === '已住满', success: row.status === '可入住' }">{{ row.status }}</span>
          </div>
          <p>{{ row.location }}</p>
          <small>{{ row.content }}</small>
          <div class="room-card-actions">
            <button class="link-btn" @click="editRow(row)">编辑</button>
            <button class="link-btn danger" @click="removeRow(row.id)">删除</button>
          </div>
        </div>
        <div v-if="records.length === 0" class="empty" style="grid-column: 1 / -1">暂无房间数据</div>
      </div>
    </section>
  </div>
</template>

<script>
import { fetchRecords, createRecord, updateRecord, deleteRecord } from '../api.js'

export default {
  name: 'RoomManage',
  data() {
    return {
      records: [],
      showForm: false,
      editingId: null,
      form: { title: '', location: '', status: '可入住', content: '' }
    }
  },
  mounted() {
    this.loadRecords()
  },
  methods: {
    async loadRecords() {
      try {
        const data = await fetchRecords('room', '')
        this.records = data.rows || data
      } catch (e) { /* ignore */ }
    },
    async saveForm() {
      if (!this.form.title.trim()) return
      try {
        if (this.editingId) {
          await updateRecord(this.editingId, {
            ...this.form,
            category: 'room',
            owner: this.form.location
          })
        } else {
          await createRecord({
            category: 'room',
            title: this.form.title,
            owner: this.form.location,
            location: this.form.location,
            amount: 0,
            status: this.form.status,
            content: this.form.content
          })
        }
        this.showForm = false
        this.editingId = null
        this.form = { title: '', location: '', status: '可入住', content: '' }
        this.loadRecords()
      } catch (e) { /* ignore */ }
    },
    editRow(row) {
      this.editingId = row.id
      this.form = {
        title: row.title,
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
    }
  }
}
</script>
