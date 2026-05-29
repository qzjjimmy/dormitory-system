<template>
  <section class="content-card">
    <div class="card-title">
      <h2>账户设置</h2>
      <span>个人信息管理</span>
    </div>

    <!-- Basic info -->
    <div class="acct-section">
      <h3 class="acct-section-title">基本信息</h3>
      <div class="acct-grid">
        <div class="acct-field">
          <label>姓名</label>
          <div class="acct-value">{{ displayName }}</div>
        </div>
        <div class="acct-field">
          <label>角色</label>
          <div class="acct-value">{{ roleLabel }}</div>
        </div>
        <div class="acct-field">
          <label>用户名</label>
          <div class="acct-value">{{ currentUser.username }}</div>
        </div>
        <div class="acct-field">
          <label>宿舍/岗位</label>
          <div class="acct-value">{{ currentUser.roomNo || '未分配' }}</div>
        </div>
        <div class="acct-field">
          <label>注册时间</label>
          <div class="acct-value">{{ currentUser.createdAt || '-' }}</div>
        </div>
        <div class="acct-field acct-phone">
          <label>电话</label>
          <div class="acct-edit-row">
            <input v-model="phone" placeholder="请输入手机号" class="acct-input">
            <button class="reg-btn primary" style="width:auto;padding:8px 20px;font-size:13px" @click="savePhone">保存</button>
          </div>
          <p v-if="phoneMsg" class="acct-msg" :class="{ ok: phoneOk }">{{ phoneMsg }}</p>
        </div>
      </div>
    </div>

    <!-- Features (read-only) -->
    <div v-if="currentUser.role === 'student'" class="acct-section">
      <h3 class="acct-section-title">个人特征 <small>（注册时填写，用于智能宿舍分配）</small></h3>

      <div class="feat-card-grid">
        <!-- Gender -->
        <div class="feat-card">
          <div class="feat-card-icon">{{ form.gender === '男' ? '♂' : '♀' }}</div>
          <div class="feat-card-label">性别</div>
          <div class="feat-card-val">{{ form.gender || '-' }}</div>
        </div>

        <!-- Room preference -->
        <div class="feat-card">
          <div class="feat-card-icon">🏠</div>
          <div class="feat-card-label">房间偏好</div>
          <div class="feat-card-val">{{ form.preferredRoomType || '-' }}</div>
        </div>

        <!-- Bed preference -->
        <div class="feat-card">
          <div class="feat-card-icon">{{ form.preferredBed === '靠窗' ? '🪟' : form.preferredBed === '靠门' ? '🚪' : '🛏' }}</div>
          <div class="feat-card-label">床位偏好</div>
          <div class="feat-card-val">{{ form.preferredBed || '无所谓' }}</div>
        </div>

        <!-- Sleep habit -->
        <div class="feat-card">
          <div class="feat-card-icon">{{ form.sleepHabit === '早睡' ? '🌅' : '🌙' }}</div>
          <div class="feat-card-label">作息习惯</div>
          <div class="feat-card-val">{{ form.sleepHabit || '-' }}</div>
        </div>

        <!-- Return time -->
        <div class="feat-card">
          <div class="feat-card-icon">🕐</div>
          <div class="feat-card-label">返回时间</div>
          <div class="feat-card-val">{{ form.returnTime || '-' }}</div>
        </div>

        <!-- Smoking -->
        <div class="feat-card">
          <div class="feat-card-icon">{{ form.smoking === '否' ? '🚭' : '🚬' }}</div>
          <div class="feat-card-label">抽烟</div>
          <div class="feat-card-val">{{ form.smoking || '-' }}</div>
        </div>

        <!-- Cleanliness -->
        <div class="feat-card">
          <div class="feat-card-icon">🧹</div>
          <div class="feat-card-label">卫生习惯</div>
          <div class="feat-card-val">{{ form.cleanliness || '-' }}</div>
        </div>

        <!-- Gaming -->
        <div class="feat-card">
          <div class="feat-card-icon">{{ form.gaming === '是' ? '🎮' : '📚' }}</div>
          <div class="feat-card-label">打游戏</div>
          <div class="feat-card-val">{{ form.gaming || '-' }}</div>
        </div>

        <!-- Snoring -->
        <div class="feat-card">
          <div class="feat-card-icon">{{ form.snoring === '否' ? '😴' : '💤' }}</div>
          <div class="feat-card-label">打鼾</div>
          <div class="feat-card-val">{{ form.snoring || '-' }}</div>
        </div>

        <!-- Noise tolerance -->
        <div class="feat-card">
          <div class="feat-card-icon">🔊</div>
          <div class="feat-card-label">噪音容忍</div>
          <div class="feat-card-val">{{ form.noiseTolerance || '-' }}</div>
        </div>

        <!-- Major/class - wider -->
        <div class="feat-card wide">
          <div class="feat-card-icon">🎓</div>
          <div class="feat-card-label">专业班级</div>
          <div class="feat-card-val">{{ form.majorClass || '-' }}</div>
        </div>
      </div>

      <!-- Hobbies section -->
      <div class="feat-hobbies">
        <div class="feat-hobbies-header">
          <span class="feat-hobbies-icon">🎯</span>
          <span class="feat-hobbies-title">兴趣爱好</span>
        </div>
        <div class="feat-hobbies-tags">
          <span v-for="h in form.hobbies" :key="h" class="feat-hobby-tag">{{ h }}</span>
          <span v-if="!form.hobbies || form.hobbies.length === 0" class="feat-empty">暂无</span>
        </div>
      </div>
    </div>
  </section>
</template>

<script>
import { fetchUsers } from '../api.js'
import { updateProfile } from '../api.js'

export default {
  name: 'AccountSettings',
  props: {
    currentUser: { type: Object, required: true },
    displayName: { type: String, default: '' }
  },
  data() {
    return {
      phone: this.currentUser.phone || '',
      phoneMsg: '',
      phoneOk: false,
      form: { gender:'', majorClass:'', sleepHabit:'', smoking:'', hobbies:[], cleanliness:'', gaming:'', snoring:'', returnTime:'', noiseTolerance:'', preferredRoomType:'', preferredBed:'' }
    }
  },
  computed: {
    roleLabel() {
      const map = { admin: '管理员', student: '学生端', dormkeeper: '宿管员' }
      return map[this.currentUser.role] || this.currentUser.role
    }
  },
  mounted() {
    if (this.currentUser.role === 'student') {
      this.loadProfile()
    }
  },
  methods: {
    async loadProfile() {
      try {
        const data = await fetchUsers('', 1, 200)
        const users = data.rows || []
        const me = users.find(u => u.id === this.currentUser.id)
        if (me) {
          this.form.gender = me.gender || ''
          this.form.majorClass = me.majorClass || ''
          this.form.sleepHabit = me.sleepHabit || ''
          this.form.smoking = me.smoking || ''
          this.form.hobbies = me.hobbies ? me.hobbies.split(',') : []
          this.form.cleanliness = me.cleanliness || ''
          this.form.gaming = me.gaming || ''
          this.form.snoring = me.snoring || ''
          this.form.returnTime = me.returnTime || ''
          this.form.noiseTolerance = me.noiseTolerance || ''
          this.form.preferredRoomType = me.preferredRoomType || ''
          this.form.preferredBed = me.preferredBed || ''
          this.phone = me.phone || this.currentUser.phone || ''
        }
      } catch (e) {
        // use defaults
      }
    },
    async savePhone() {
      this.phoneMsg = ''
      this.phoneOk = false
      if (!this.phone || !this.phone.trim()) {
        this.phoneMsg = '电话不能为空'
        return
      }
      try {
        await updateProfile({
          gender: this.form.gender,
          majorClass: this.form.majorClass,
          sleepHabit: this.form.sleepHabit,
          smoking: this.form.smoking,
          hobbies: (this.form.hobbies || []).join(','),
          cleanliness: this.form.cleanliness,
          gaming: this.form.gaming,
          snoring: this.form.snoring,
          returnTime: this.form.returnTime,
          noiseTolerance: this.form.noiseTolerance,
          preferredRoomType: this.form.preferredRoomType,
          preferredBed: this.form.preferredBed,
          phone: this.phone
        })
        this.phoneMsg = '已保存'
        this.phoneOk = true
        // Update session too
        const user = JSON.parse(sessionStorage.getItem('dorm-user') || '{}')
        user.phone = this.phone
        sessionStorage.setItem('dorm-user', JSON.stringify(user))
      } catch (e) {
        this.phoneMsg = e.message
      }
    }
  }
}
</script>
