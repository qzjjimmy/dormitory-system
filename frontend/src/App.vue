<template>
  <section v-if="!currentUser" class="login-page">
    <!-- Login card -->
    <div v-if="!showRegister" class="login-card">
      <div class="login-logo">
        <div class="app-icon">⌂</div>
        <div>
          <h1>学生宿舍管理系统</h1>
          <p>智慧宿舍管理系统 · Smart Dormitory</p>
        </div>
      </div>
      <div class="field">
        <label>用户名</label>
        <input v-model="loginForm.username" placeholder="admin / student / dormkeeper">
      </div>
      <div class="field">
        <label>密码</label>
        <input v-model="loginForm.password" type="password" placeholder="123456" @keyup.enter="login">
      </div>
      <button class="blue-btn block" @click="login">登录</button>
      <p class="error">{{ loginMessage }}</p>
      <div class="demo-users">
        <button @click="useDemo('student')">学生端</button>
        <button @click="useDemo('admin')">管理员端</button>
        <button @click="useDemo('dormkeeper')">宿管端</button>
      </div>
      <p class="register-link">
        还没有账号？<a href="#" @click.prevent="showRegister = true">立即注册</a>
      </p>
    </div>

    <!-- Register card -->
    <RegisterForm v-else @registered="onRegistered" @back="showRegister = false" />
  </section>

  <section v-else class="student-shell">
    <ProfileCompletionModal v-if="showProfileModal" @completed="onProfileCompleted" />
    <aside class="side-menu">
      <div class="menu-brand">
        <button class="collapse-btn">☰</button>
        <div class="home-icon">⌂</div>
        <strong>学生宿舍管理系统</strong>
      </div>
      <nav>
        <button
          v-for="item in visibleMenus"
          :key="item.key + item.roles.join()"
          :class="{ active: activeMenu && activeMenu.key === item.key && activeMenu.section === item.section }"
          @click="selectMenu(item)"
        >
          <span>{{ item.icon }}</span>
          {{ item.label }}
        </button>
      </nav>
    </aside>

    <main class="page">
      <header class="top-bar">
        <div class="breadcrumb">
          <b>{{ currentSection }}</b>
          <span>/</span>
          <em>{{ activeMenu ? activeMenu.label : '' }}</em>
        </div>
        <div class="user-actions">
          <button class="bell">⌕</button>
          <div class="user-dropdown" :class="{ open: menuOpen }">
            <button class="user-trigger" @click="menuOpen = !menuOpen">
              <span class="avatar">{{ userInitial }}</span>
              <strong>{{ displayName }}</strong>
              <i class="arrow">▾</i>
            </button>
            <div v-if="menuOpen" class="dropdown-panel" @click.stop>
              <div class="dp-user-info">
                <span class="avatar" style="width:36px;height:36px;font-size:14px">{{ userInitial }}</span>
                <div>
                  <b>{{ displayName }}</b>
                  <p>{{ roleLabel(currentUser.role) }}</p>
                </div>
              </div>
              <div class="dp-divider"></div>
              <button class="dp-item" @click="selectMenuByKey('account'); menuOpen = false">
                <span>⚙</span> 个人设置
              </button>
              <button class="dp-item" @click="selectMenuByKey('chat'); menuOpen = false">
                <span>▻</span> 智能通话
              </button>
              <div class="dp-divider"></div>
              <button class="dp-item danger" @click="logout">
                <span>↪</span> 退出登录
              </button>
            </div>
          </div>
        </div>
        <div v-if="menuOpen" class="dp-overlay" @click="menuOpen = false"></div>
      </header>

      <!-- Student Dashboard -->
      <template v-if="activeMenu && activeMenu.key === 'dashboard' && currentUser.role === 'student'">
        <StudentDashboard
          :display-name="displayName"
          :roommates="roommates"
          :beijing-date-text="beijingDateText"
          :weather-text="weatherText"
          :announcements="studentStats.announcements"
          :repair-count="studentStats.repairCount"
          :dorm-room="currentUser.roomNo || ''"
          :fee-info="studentStats.feeTotal"
          :hygiene-score="studentStats.hygieneScore"
          :rank-text="studentStats.rank"
          :last-check-result="studentStats.lastCheck"
          @go-dorm="selectMenuByKey('myDorm')"
          @go-repair="selectMenuByKey('repair')"
        />
      </template>

      <!-- My Dorm -->
      <template v-else-if="activeMenu && activeMenu.key === 'myDorm' && currentUser.role === 'student'">
        <MyDorm
          :roommates="roommates"
          :dorm-name="currentUser.roomNo || '未分配宿舍'"
          :bed-count="roommates.length > 4 ? 6 : 4"
          :occupancy-status="roommates.length >= (roommates.length > 4 ? 6 : 4) ? '已住满' : '空' + ((roommates.length > 4 ? 6 : 4) - roommates.length) + '床'"
          :my-bed="myBedNumber"
          :checkin-date="'2025/9/1'"
          :dorm-keeper="dormKeeperName"
          :hygiene-score="studentStats.hygieneScore"
          :rank-num="12"
          :last-check-score="98"
          :last-check-result="studentStats.lastCheck"
          :facilities="defaultFacilities"
        />
      </template>

      <!-- Admin/Dormkeeper Dashboard -->
      <template v-else-if="activeMenu && activeMenu.key === 'dashboard'">
        <AdminDashboard
          :stats="stats"
          :category-name="(cat) => categoryName(cat)"
          :bar-width="(v) => barWidth(v)"
        />
      </template>

      <!-- AI Chat -->
      <template v-else-if="activeMenu && activeMenu.key === 'ai'">
        <AiChat :user-initial="userInitial" />
      </template>

      <!-- Chat (智能通话) -->
      <template v-else-if="activeMenu && activeMenu.key === 'chat'">
        <ChatView :current-user-id="currentUser.id" />
      </template>

      <!-- Account Settings -->
      <template v-else-if="activeMenu && activeMenu.key === 'account'">
        <AccountSettings :current-user="currentUser" :display-name="displayName" />
      </template>

      <!-- Visitor Page -->
      <template v-else-if="activeMenu && activeMenu.key === 'visitor' && currentUser.role === 'student'">
        <VisitorPage :display-name="displayName" />
      </template>

      <!-- Transfer Page -->
      <template v-else-if="activeMenu && activeMenu.key === 'transfer' && currentUser.role === 'student'">
        <TransferPage :display-name="displayName" :current-room="currentUser.roomNo || ''" />
      </template>

      <!-- Rating Page -->
      <template v-else-if="activeMenu && activeMenu.key === 'rating'">
        <RatingPage :display-name="displayName" />
      </template>

      <!-- Checkin Management (admin/dormkeeper) -->
      <template v-else-if="activeMenu && activeMenu.key === 'student' && currentUser.role !== 'student'">
        <CheckinManage />
      </template>

      <!-- Room Management (admin/dormkeeper) -->
      <template v-else-if="activeMenu && activeMenu.key === 'room' && currentUser.role !== 'student'">
        <RoomManage />
      </template>

      <!-- Smart Assignment (admin) -->
      <template v-else-if="activeMenu && activeMenu.key === 'assignment'">
        <SmartAssignment />
      </template>

      <!-- Generic Record List (repair, fee, visitor, announcement, etc.) -->
      <template v-else-if="activeMenu">
        <RecordList
          :title="activeMenu.label"
          :desc="activeMenu.desc"
          :records="records"
          :editing="!!editingRecord"
          :form="editingRecord || {}"
          @search="onSearch"
          @create="newRecord"
          @edit="editRecord"
          @remove="removeRecord"
          @save="saveRecord"
          @cancel="editingRecord = null"
        />
      </template>

      <!-- Fallback -->
      <template v-else>
        <section class="content-card">
          <div class="card-title"><h2>欢迎使用</h2></div>
          <p>请从左侧菜单选择功能模块。</p>
        </section>
      </template>

      <footer class="footer">
        智慧宿舍管理系统 · Smart Dormitory <b>{{ roleLabel(currentUser.role) }} v1.0</b>
      </footer>
    </main>
  </section>
</template>

<script>
import StudentDashboard from './components/StudentDashboard.vue'
import MyDorm from './components/MyDorm.vue'
import AdminDashboard from './components/AdminDashboard.vue'
import AiChat from './components/AiChat.vue'
import AccountSettings from './components/AccountSettings.vue'
import RecordList from './components/RecordList.vue'
import ChatView from './components/ChatView.vue'
import VisitorPage from './components/VisitorPage.vue'
import TransferPage from './components/TransferPage.vue'
import RatingPage from './components/RatingPage.vue'
import CheckinManage from './components/CheckinManage.vue'
import RoomManage from './components/RoomManage.vue'
import RegisterForm from './components/RegisterForm.vue'
import ProfileCompletionModal from './components/ProfileCompletionModal.vue'
import SmartAssignment from './components/SmartAssignment.vue'
import { getVisibleMenus, getMenuByKey, roleLabel, categoryName, needsRecords, isTodoStatus } from './router.js'
import { login as apiLogin, logout as apiLogout, fetchDashboard, fetchRecords, createRecord, updateRecord, deleteRecord, fetchWeather, fetchUsers } from './api.js'

export default {
  name: 'App',
  components: { StudentDashboard, MyDorm, AdminDashboard, AiChat, AccountSettings, RecordList, ChatView, VisitorPage, TransferPage, RatingPage, CheckinManage, RoomManage, RegisterForm, ProfileCompletionModal, SmartAssignment },
  data() {
    return {
      currentUser: JSON.parse(sessionStorage.getItem('dorm-user') || 'null'),
      loginForm: { username: 'student', password: '123456' },
      loginMessage: '',
      showRegister: false,
      showProfileModal: false,
      activeMenu: null,
      menuOpen: false,
      stats: {},
      studentStats: { announcements: [], repairCount: 0, feeTotal: '¥ 0.00', hygieneScore: 0, rank: '-', lastCheck: '-' },
      records: [],
      keyword: '',
      editingRecord: null,
      now: new Date(),
      weatherText: '加载中...',
      roommates: []
    }
  },
  computed: {
    visibleMenus() {
      const hasDorm = this.currentUser && this.currentUser.roomNo && this.currentUser.roomNo !== 'null'
      return this.currentUser ? getVisibleMenus(this.currentUser.role, hasDorm) : []
    },
    displayName() {
      return this.currentUser ? this.currentUser.realName : ''
    },
    userInitial() {
      return this.displayName ? this.displayName.slice(0, 1) : '用'
    },
    currentSection() {
      return this.activeMenu ? this.activeMenu.section : ''
    },
    beijingDateText() {
      const parts = new Intl.DateTimeFormat('zh-CN', {
        timeZone: 'Asia/Shanghai',
        year: 'numeric',
        month: 'long',
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit',
        hour12: false
      }).formatToParts(this.now)
      const value = type => parts.find(part => part.type === type)?.value || ''
      const weekday = new Intl.DateTimeFormat('zh-CN', {
        timeZone: 'Asia/Shanghai',
        weekday: 'long'
      }).format(this.now)
      return `北京时间 ${value('year')}年${value('month')}月${value('day')}日 ${weekday} ${value('hour')}:${value('minute')}`
    },
    myBedNumber() {
      const roomNo = this.currentUser ? this.currentUser.roomNo : ''
      const match = roomNo.match(/(\d+)号床/)
      return match ? parseInt(match[1]) : 2
    },
    dormKeeperName() {
      // Derive keeper from room number: building number → corresponding keeper
      const roomNo = this.currentUser ? this.currentUser.roomNo : ''
      if (roomNo.includes('芙蓉楼1')) return '李宿管'
      if (roomNo.includes('芙蓉楼2')) return '王宿管'
      if (roomNo.includes('芙蓉楼3')) return '张宿管'
      return '张宿管'
    },
    defaultFacilities() {
      return [
        { name: '照明灯具', desc: '全部正常', icon: '☼', status: 'normal', label: '正常' },
        { name: '空调设备', desc: '制冷正常 · 26°C', icon: '❄', status: 'normal', label: '运行中' },
        { name: '宿舍网络', desc: 'WiFi 已连接', icon: '☍', status: 'normal', label: '已连接' },
        { name: '直饮水', desc: '滤芯剩余 15 天', icon: '♨', status: 'warn', label: '滤芯待换' }
      ]
    }
  },
  mounted() {
    if (this.currentUser) this.selectMenu(this.visibleMenus[0])
    this.clockTimer = setInterval(() => { this.now = new Date() }, 1000)
    this.loadWeather()
  },
  beforeUnmount() {
    clearInterval(this.clockTimer)
  },
  methods: {
    roleLabel,
    categoryName(cat) { return categoryName(this.visibleMenus, cat) },
    useDemo(username) {
      this.loginForm = { username, password: '123456' }
    },
    async login() {
      try {
        const user = await apiLogin(this.loginForm)
        this.currentUser = user
        sessionStorage.setItem('dorm-user', JSON.stringify(user))
        this.loginMessage = ''
        if (user.profileComplete === false) {
          this.showProfileModal = true
          return
        }
        this.selectMenu(getVisibleMenus(user.role, !!user.roomNo)[0])
      } catch (error) {
        this.loginMessage = error.message
      }
    },
    logout() {
      apiLogout().catch(() => {})
      sessionStorage.removeItem('dorm-user')
      this.currentUser = null
      this.activeMenu = null
    },
    selectMenu(menu) {
      this.activeMenu = menu
      this.keyword = ''
      this.editingRecord = null
      if (menu.key === 'dashboard' && this.currentUser.role === 'student') this.loadStudentDashboard()
      else if (menu.key === 'dashboard' && this.currentUser.role !== 'student') this.loadDashboard()
      else if (needsRecords(menu.key)) this.loadRecords()
    },
    selectMenuByKey(key) {
      const menu = getMenuByKey(this.visibleMenus, key)
      if (menu) this.selectMenu(menu)
    },
    async loadDashboard() {
      this.stats = await fetchDashboard()
    },
    async loadWeather() {
      try {
        const data = await fetchWeather()
        this.weatherText = data.text || '福州市连江县 · 多云 26°C'
      } catch (e) {
        this.weatherText = '福州市连江县 · 多云 26°C'
      }
    },
    async loadStudentDashboard() {
      try {
        const [announcementData, repairData, feeData, hygieneData] = await Promise.all([
          fetchRecords('announcement', '', 1, 10),
          fetchRecords('repair', '', 1, 100),
          fetchRecords('fee', '', 1, 100),
          fetchRecords('hygiene', '', 1, 10)
        ])
        const announcements = announcementData.rows || []
        const repairs = repairData.rows || []
        const fees = feeData.rows || []
        const hygieneRecords = hygieneData.rows || []

        const myRoom = this.currentUser.roomNo || ''
        // Extract exact room: "芙蓉楼3 · 519室 · 2号床" → "芙蓉楼3 · 519室"
        const roomMatch = myRoom.match(/^(.+室)/)
        const exactRoom = roomMatch ? roomMatch[1] : ''

        // Filter by owner only when no room; by owner+room when assigned
        const isMyRecord = (r) => {
          if (r.owner === this.displayName) return true
          if (exactRoom && r.location && r.location.includes(exactRoom)) return true
          return false
        }

        const myRepairs = repairs.filter(isMyRecord)
        const myFees = fees.filter(isMyRecord)
        const unpaidFees = myFees.filter(f => f.status === '待缴费')
        const totalUnpaid = unpaidFees.reduce((sum, f) => sum + (f.amount || 0), 0)

        const myHygiene = hygieneRecords
          .filter(h => exactRoom && h.location && h.location.includes(exactRoom))
          .sort((a, b) => (b.createdAt || '').localeCompare(a.createdAt || ''))
        const latestHygiene = myHygiene[0]

        this.studentStats = {
          announcements,
          repairCount: myRepairs.length,
          feeTotal: totalUnpaid > 0 ? `¥ ${totalUnpaid.toFixed(2)} (待缴)` : '¥ 0.00',
          hygieneScore: latestHygiene ? parseInt(latestHygiene.content.match(/\d+/) || '0') : 0,
          rank: latestHygiene ? '第 12 名' : '-',
          lastCheck: latestHygiene ? latestHygiene.status : '暂无'
        }

        // Load dynamic roommates from same room
        const allUsers = await fetchUsers('', 1, 200)
        const allStudents = (allUsers.rows || []).filter(u => u.role === 'student')
        if (exactRoom) {
          this.roommates = allStudents
            .filter(u => u.roomNo && u.roomNo.startsWith(exactRoom))
            .map(u => ({
              name: u.realName,
              bed: (u.roomNo || '').match(/(\d+号床)/)?.[0] || '-',
              tag: u.id === this.currentUser.id ? '我' : '',
              status: '在寝',
              avatar: (u.realName || '?').slice(0, 1),
              color: '#8097bd',
              role: 'student'
            }))
        } else {
          this.roommates = []
        }
      } catch (e) {
        // Silently fail — keep defaults
      }
    },
    async loadRecords() {
      const data = await fetchRecords(this.activeMenu.key, this.keyword)
      this.records = data.rows || data
    },
    onSearch(kw) {
      this.keyword = kw
      this.loadRecords()
    },
    newRecord() {
      this.editingRecord = {
        category: this.activeMenu.key,
        title: '',
        owner: this.displayName,
        location: '明德楼 101',
        amount: 0,
        status: '待处理',
        content: ''
      }
    },
    editRecord(row) {
      this.editingRecord = { ...row }
    },
    async saveRecord(record) {
      const isEdit = !!record.id
      if (isEdit) {
        await updateRecord(record.id, record)
      } else {
        await createRecord(record)
      }
      this.editingRecord = null
      this.loadRecords()
    },
    async removeRecord(id) {
      if (!window.confirm('确认删除这条记录？')) return
      await deleteRecord(id)
      this.loadRecords()
    },
    barWidth(value) {
      const values = (this.stats.categories || []).map(item => item.value)
      const max = Math.max(...values, 1)
      return `${Math.max(8, value / max * 100)}%`
    },
    onRegistered(user) {
      this.currentUser = user
      sessionStorage.setItem('dorm-user', JSON.stringify(user))
      this.showRegister = false
      this.selectMenu(getVisibleMenus(user.role)[0])
    },
    onProfileCompleted() {
      this.showProfileModal = false
      this.currentUser = JSON.parse(sessionStorage.getItem('dorm-user') || 'null')
      this.selectMenu(this.visibleMenus[0])
    }
  }
}
</script>
