<template>
  <section class="content-card">
    <div class="card-title">
      <h2>账户设置</h2>
      <span>个人信息管理</span>
    </div>
    <div class="profile-form">
      <div><label>姓名</label><input :value="displayName" readonly></div>
      <div><label>角色</label><input :value="roleLabel" readonly></div>
      <div><label>宿舍/岗位</label><input :value="currentUser.roomNo" readonly></div>
      <div><label>电话</label><input :value="currentUser.phone" readonly></div>
      <div><label>用户名</label><input :value="currentUser.username" readonly></div>
      <div><label>注册时间</label><input :value="currentUser.createdAt || '-'" readonly></div>
    </div>
  </section>
</template>

<script>
export default {
  name: 'AccountSettings',
  props: {
    currentUser: { type: Object, required: true },
    displayName: { type: String, default: '' }
  },
  computed: {
    roleLabel() {
      const map = { admin: '管理员', student: '学生端', dormkeeper: '宿管员' }
      return map[this.currentUser.role] || this.currentUser.role
    }
  }
}
</script>
