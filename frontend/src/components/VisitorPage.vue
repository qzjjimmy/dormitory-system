<template>
  <section class="content-card">
    <div class="card-title inline">
      <h2>访客预约</h2>
      <span>来访人员登记与审核状态</span>
      <button class="blue-btn" @click="showForm = !showForm">{{ showForm ? '取消' : '+ 新增预约' }}</button>
    </div>

    <div v-if="showForm" class="edit-form">
      <div class="form-grid">
        <div class="field"><label>访客姓名</label><input v-model="form.owner" placeholder="请输入访客姓名"></div>
        <div class="field"><label>来访时间</label><input v-model="form.location" placeholder="例如：周六 14:00"></div>
        <div class="field wide"><label>来访事由</label><textarea v-model="form.content" placeholder="请简要描述来访事由"></textarea></div>
        <div class="field wide actions">
          <button class="blue-btn" @click="submit">提交预约</button>
          <button @click="showForm = false">取消</button>
        </div>
      </div>
    </div>

    <div class="table-wrap" style="margin-top: 18px">
      <table>
        <thead>
          <tr><th>访客姓名</th><th>来访时间</th><th>事由</th><th>状态</th><th>操作</th></tr>
        </thead>
        <tbody>
          <tr v-for="row in records" :key="row.id">
            <td>{{ row.owner }}</td>
            <td>{{ row.location }}</td>
            <td>{{ row.content }}</td>
            <td><span class="state" :class="{ warn: isPending(row.status) }">{{ row.status }}</span></td>
            <td>
              <button class="link-btn danger" @click="removeRecord(row.id)">删除</button>
            </td>
          </tr>
          <tr v-if="records.length === 0">
            <td colspan="5" class="empty">暂无预约记录</td>
          </tr>
        </tbody>
      </table>
    </div>
  </section>
</template>

<script>
import { fetchRecords, createRecord, deleteRecord } from '../api.js'

export default {
  name: 'VisitorPage',
  props: {
    displayName: { type: String, default: '' }
  },
  data() {
    return {
      showForm: false,
      form: { owner: '', location: '', content: '' },
      records: []
    }
  },
  mounted() {
    this.loadRecords()
  },
  methods: {
    async loadRecords() {
      try {
        const data = await fetchRecords('visitor', '')
        this.records = data.rows || data
      } catch (e) { /* ignore */ }
    },
    async submit() {
      if (!this.form.owner.trim()) return
      try {
        await createRecord({
          category: 'visitor',
          title: '访客预约：' + this.form.owner,
          owner: this.form.owner,
          location: this.form.location,
          amount: 0,
          status: '待审核',
          content: this.form.content
        })
        this.form = { owner: '', location: '', content: '' }
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
    isPending(status) {
      return ['待审核', '审核中'].includes(status)
    }
  }
}
</script>
