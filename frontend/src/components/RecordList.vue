<template>
  <section class="content-card">
    <div class="list-head">
      <div class="card-title">
        <h2>{{ title }}</h2>
        <span>{{ desc }}</span>
      </div>
      <div class="query-box">
        <input v-model="searchKeyword" placeholder="请输入关键字" @keyup.enter="$emit('search', searchKeyword)">
        <button @click="$emit('search', searchKeyword)">搜索</button>
        <button class="blue-btn" @click="$emit('create')">新增</button>
      </div>
    </div>

    <div v-if="editing" class="edit-form">
      <div><label>标题</label><input v-model="form.title"></div>
      <div><label>人员</label><input v-model="form.owner"></div>
      <div><label>地点</label><input v-model="form.location"></div>
      <div><label>金额</label><input v-model="form.amount" type="number" step="0.01"></div>
      <div><label>状态</label><input v-model="form.status"></div>
      <div class="wide"><label>内容</label><textarea v-model="form.content"></textarea></div>
      <div class="wide actions">
        <button class="blue-btn" @click="$emit('save', { ...form })">保存</button>
        <button @click="$emit('cancel')">取消</button>
      </div>
    </div>

    <div class="table-wrap">
      <table>
        <thead>
          <tr>
            <th>标题</th><th>人员</th><th>地点</th><th>金额</th><th>状态</th><th>说明</th><th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="row in records" :key="row.id">
            <td>{{ row.title }}</td>
            <td>{{ row.owner }}</td>
            <td>{{ row.location }}</td>
            <td>{{ row.amount }}</td>
            <td><span class="state" :class="{ warn: isTodo(row.status) }">{{ row.status }}</span></td>
            <td>{{ row.content }}</td>
            <td>
              <button v-if="isTodo(row.status)" class="link-btn" style="color:#27ae60" @click="$emit('approve', row)">通过</button>
              <button v-if="isTodo(row.status)" class="link-btn" style="color:#e67e22" @click="$emit('reject', row)">驳回</button>
              <button class="link-btn" @click="$emit('edit', row)">编辑</button>
              <button class="link-btn danger" @click="$emit('remove', row.id)">删除</button>
            </td>
          </tr>
          <tr v-if="records.length === 0">
            <td colspan="7" class="empty">暂无数据</td>
          </tr>
        </tbody>
      </table>
    </div>
  </section>
</template>

<script>
export default {
  name: 'RecordList',
  props: {
    title: { type: String, default: '' },
    desc: { type: String, default: '' },
    records: { type: Array, default: () => [] },
    editing: { type: Boolean, default: false },
    form: { type: Object, default: () => ({}) }
  },
  emits: ['search', 'create', 'edit', 'remove', 'save', 'cancel', 'approve', 'reject'],
  data() {
    return {
      searchKeyword: ''
    }
  },
  methods: {
    isTodo(status) {
      return ['待处理', '待审核', '审核中', '待缴费', '待回复'].includes(status)
    }
  }
}
</script>
