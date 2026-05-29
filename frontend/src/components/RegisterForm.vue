<template>
  <div class="register-wrapper">
    <div class="register-card">
      <div class="reg-header">
        <div class="reg-icon">⌂</div>
        <h1>新生注册</h1>
        <p>创建账号并完善个人特征，享受智能宿舍分配</p>
      </div>

      <div class="reg-steps">
        <div :class="['step-dot', { done: step > 1, active: step === 1 }]">
          <span class="step-num">1</span>
          <span class="step-label">账号信息</span>
        </div>
        <div class="step-line" :class="{ filled: step > 1 }"></div>
        <div :class="['step-dot', { active: step === 2 }]">
          <span class="step-num">2</span>
          <span class="step-label">个人特征</span>
        </div>
      </div>

      <!-- Step 1: Account info -->
      <div v-if="step === 1" class="reg-body">
        <div class="reg-field">
          <label>用户名</label>
          <input v-model="form.username" placeholder="设置登录用户名" autocomplete="off">
        </div>
        <div class="reg-row">
          <div class="reg-field half">
            <label>密码</label>
            <input v-model="form.password" type="password" placeholder="至少6位">
          </div>
          <div class="reg-field half">
            <label>确认密码</label>
            <input v-model="form.confirmPassword" type="password" placeholder="再次输入">
          </div>
        </div>
        <div class="reg-row">
          <div class="reg-field half">
            <label>真实姓名</label>
            <input v-model="form.realName" placeholder="身份证姓名">
          </div>
          <div class="reg-field half">
            <label>手机号</label>
            <input v-model="form.phone" placeholder="选填">
          </div>
        </div>
        <div class="reg-field">
          <label>房间类型偏好</label>
          <div class="pill-group">
            <span :class="['pill', { on: form.preferredRoomType === '4人间' }]" @click="form.preferredRoomType = '4人间'">🏠 4人间</span>
            <span :class="['pill', { on: form.preferredRoomType === '6人间' }]" @click="form.preferredRoomType = '6人间'">🏢 6人间</span>
          </div>
        </div>
        <div class="reg-field">
          <label>床位偏好</label>
          <div class="pill-group">
            <span :class="['pill', { on: form.preferredBed === '靠窗' }]" @click="form.preferredBed = '靠窗'">🪟 靠窗</span>
            <span :class="['pill', { on: form.preferredBed === '靠门' }]" @click="form.preferredBed = '靠门'">🚪 靠门</span>
            <span :class="['pill', { on: form.preferredBed === '' }]" @click="form.preferredBed = ''">🎲 无所谓</span>
          </div>
        </div>
        <p class="reg-error" v-if="errorMessage">{{ errorMessage }}</p>
        <button class="reg-btn primary" @click="nextStep">下一步，填写特征 →</button>
      </div>

      <!-- Step 2: Features -->
      <div v-if="step === 2" class="reg-body">
        <div class="reg-field">
          <label>性别</label>
          <div class="pill-group">
            <span :class="['pill', { on: form.gender === '男' }]" @click="form.gender = '男'">♂ 男</span>
            <span :class="['pill', { on: form.gender === '女' }]" @click="form.gender = '女'">♀ 女</span>
          </div>
        </div>

        <div class="reg-field">
          <label>专业班级</label>
          <input v-model="form.majorClass" placeholder="如：智能科学与技术2025级">
        </div>

        <div class="reg-field">
          <label>作息习惯</label>
          <div class="pill-group">
            <span :class="['pill', { on: form.sleepHabit === '早睡' }]" @click="form.sleepHabit = '早睡'">🌅 早睡（23:00前）</span>
            <span :class="['pill', { on: form.sleepHabit === '晚睡' }]" @click="form.sleepHabit = '晚睡'">🌙 晚睡（23:00后）</span>
          </div>
        </div>

        <div class="reg-field">
          <label>是否抽烟</label>
          <div class="pill-group">
            <span :class="['pill', { on: form.smoking === '否' }]" @click="form.smoking = '否'">🚭 不抽烟</span>
            <span :class="['pill', { on: form.smoking === '是' }]" @click="form.smoking = '是'">🚬 抽烟</span>
          </div>
        </div>

        <div class="reg-field">
          <label>兴趣爱好 <small>（可多选）</small></label>
          <div class="hobby-grid">
            <span v-for="h in hobbyOptions" :key="h"
                  :class="['hobby-tag', { picked: form.hobbies.includes(h) }]"
                  @click="toggleHobby(h)">
              {{ hobbyIcon(h) }} {{ h }}
            </span>
          </div>
        </div>

        <div class="reg-field">
          <label>卫生习惯</label>
          <div class="pill-group triple">
            <span :class="['pill', { on: form.cleanliness === '整洁' }]" @click="form.cleanliness = '整洁'">🧹 整洁</span>
            <span :class="['pill', { on: form.cleanliness === '一般' }]" @click="form.cleanliness = '一般'">📦 一般</span>
            <span :class="['pill', { on: form.cleanliness === '随意' }]" @click="form.cleanliness = '随意'">🎲 随意</span>
          </div>
        </div>

        <div class="reg-row">
          <div class="reg-field half">
            <label>是否打游戏</label>
            <div class="pill-group">
              <span :class="['pill', { on: form.gaming === '是' }]" @click="form.gaming = '是'">🎮 打游戏</span>
              <span :class="['pill', { on: form.gaming === '否' }]" @click="form.gaming = '否'">📚 不打</span>
            </div>
          </div>
          <div class="reg-field half">
            <label>是否打鼾</label>
            <div class="pill-group">
              <span :class="['pill', { on: form.snoring === '否' }]" @click="form.snoring = '否'">😴 不打鼾</span>
              <span :class="['pill', { on: form.snoring === '是' }]" @click="form.snoring = '是'">💤 打鼾</span>
            </div>
          </div>
        </div>

        <div class="reg-row">
          <div class="reg-field half">
            <label>返回宿舍时间</label>
            <div class="pill-group triple">
              <span :class="['pill', { on: form.returnTime === '早归' }]" @click="form.returnTime = '早归'">🏠 早归</span>
              <span :class="['pill', { on: form.returnTime === '正常' }]" @click="form.returnTime = '正常'">🕐 正常</span>
              <span :class="['pill', { on: form.returnTime === '晚归' }]" @click="form.returnTime = '晚归'">🌃 晚归</span>
            </div>
          </div>
          <div class="reg-field half">
            <label>噪音容忍度</label>
            <div class="pill-group triple">
              <span :class="['pill', { on: form.noiseTolerance === '安静' }]" @click="form.noiseTolerance = '安静'">🔇 安静</span>
              <span :class="['pill', { on: form.noiseTolerance === '正常' }]" @click="form.noiseTolerance = '正常'">🔊 正常</span>
              <span :class="['pill', { on: form.noiseTolerance === '热闹' }]" @click="form.noiseTolerance = '热闹'">🎉 热闹</span>
            </div>
          </div>
        </div>

        <p class="reg-error" v-if="errorMessage">{{ errorMessage }}</p>

        <div class="reg-actions">
          <button class="reg-btn outline" @click="step = 1">← 上一步</button>
          <button class="reg-btn primary" @click="submitRegister">完成注册</button>
        </div>
      </div>

      <p class="reg-back"><a href="#" @click.prevent="$emit('back')">← 返回登录</a></p>
    </div>
  </div>
</template>

<script>
import { register } from '../api.js'

const HOBBY_OPTIONS = ['篮球','足球','羽毛球','跑步','游泳','健身','编程','阅读','音乐','电影','游戏','摄影','绘画','旅行','美食']

const HOBBY_ICONS = {
  '篮球':'🏀','足球':'⚽','羽毛球':'🏸','跑步':'🏃','游泳':'🏊','健身':'💪',
  '编程':'💻','阅读':'📖','音乐':'🎵','电影':'🎬','游戏':'🎮','摄影':'📷',
  '绘画':'🎨','旅行':'✈️','美食':'🍜'
}

export default {
  name: 'RegisterForm',
  emits: ['registered', 'back'],
  data() {
    return {
      step: 1,
      errorMessage: '',
      hobbyOptions: HOBBY_OPTIONS,
      form: {
        username: '', password: '', confirmPassword: '', realName: '', phone: '',
        gender: '男', majorClass: '', sleepHabit: '晚睡', smoking: '否',
        hobbies: [], cleanliness: '整洁', gaming: '是', snoring: '否',
        returnTime: '正常', noiseTolerance: '正常', preferredRoomType: '4人间', preferredBed: ''
      }
    }
  },
  methods: {
    hobbyIcon(h) { return HOBBY_ICONS[h] || '•' },
    toggleHobby(h) {
      const i = this.form.hobbies.indexOf(h)
      if (i >= 0) this.form.hobbies.splice(i, 1)
      else this.form.hobbies.push(h)
    },
    nextStep() {
      this.errorMessage = ''
      if (!this.form.username || !this.form.password || !this.form.realName) {
        this.errorMessage = '请填写用户名、密码和姓名'
        return
      }
      if (this.form.password !== this.form.confirmPassword) {
        this.errorMessage = '两次密码不一致'
        return
      }
      if (this.form.password.length < 6) {
        this.errorMessage = '密码至少6位'
        return
      }
      this.step = 2
    },
    async submitRegister() {
      this.errorMessage = ''
      if (!this.form.gender || !this.form.majorClass || !this.form.sleepHabit
          || !this.form.smoking || !this.form.cleanliness || !this.form.gaming
          || !this.form.snoring || !this.form.returnTime || !this.form.noiseTolerance) {
        this.errorMessage = '请完善所有特征信息'
        return
      }
      if (this.form.phone && !/^1[3-9]\d{9}$/.test(this.form.phone.trim())) {
        this.errorMessage = '请输入正确的手机号'
        return
      }
      try {
        const payload = {
          username: this.form.username,
          password: this.form.password,
          realName: this.form.realName,
          phone: this.form.phone,
          gender: this.form.gender,
          majorClass: this.form.majorClass,
          sleepHabit: this.form.sleepHabit,
          smoking: this.form.smoking,
          hobbies: this.form.hobbies.join(','),
          cleanliness: this.form.cleanliness,
          gaming: this.form.gaming,
          snoring: this.form.snoring,
          returnTime: this.form.returnTime,
          noiseTolerance: this.form.noiseTolerance,
          preferredRoomType: this.form.preferredRoomType,
          preferredBed: this.form.preferredBed
        }
        const user = await register(payload)
        this.$emit('registered', user)
      } catch (e) {
        this.errorMessage = e.message
      }
    }
  }
}
</script>
