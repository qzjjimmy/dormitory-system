<template>
  <div class="ai-layout">
    <!-- Left: conversation history sidebar -->
    <aside class="chat-history-panel">
      <div class="history-header">
        <h3>对话历史</h3>
        <button class="blue-btn" @click="newConversation" style="padding:6px 12px;font-size:13px">+ 新对话</button>
      </div>
      <div class="history-list">
        <div
          v-for="(conv, index) in conversations"
          :key="index"
          class="history-item"
          :class="{ active: currentIndex === index }"
          @click="switchTo(index)"
        >
          <b>{{ conv.title || '新对话' }}</b>
          <span>{{ conv.messages.length }} 条消息 · {{ timeLabel(conv.updatedAt) }}</span>
          <button class="history-del" @click.stop="deleteConv(index)">×</button>
        </div>
        <div v-if="conversations.length === 0" class="empty" style="padding:40px 0">暂无历史对话</div>
      </div>
    </aside>

    <!-- Center: chat messages -->
    <section class="content-card chat-main-area">
      <div class="card-title inline">
        <h2>AI助手</h2>
        <span>{{ activeTitle }}</span>
        <button class="text-btn" @click="newConversation">+ 新对话</button>
      </div>

      <div class="chat-stream" ref="stream">
        <div v-for="(msg, index) in activeMessages" :key="index" class="message" :class="msg.role">
          <span class="msg-avatar">{{ msg.role === 'user' ? userInitial : 'AI' }}</span>
          <div class="msg-bubble">
            <p>{{ msg.text }}</p>
          </div>
        </div>
      </div>

      <div class="chat-composer">
        <textarea
          v-model="question"
          placeholder="请输入问题，例如：帮我写一个报修申请"
          rows="2"
          @keyup.enter.exact.prevent="send"
        ></textarea>
        <button class="blue-btn" @click="send" :disabled="!question.trim() || sending">发送</button>
      </div>
    </section>

    <!-- Right: suggestion panel -->
    <aside class="helper-panel">
      <div class="card-title"><h3>智能建议</h3></div>
      <div v-for="item in suggestions" :key="item.title" class="suggestion" @click="quickAsk(item.title)">
        <b>{{ item.title }}</b>
        <p>{{ item.meta }}</p>
      </div>
      <div class="card-title" style="margin-top:24px"><h3>热门咨询</h3></div>
      <div class="hot-list">
        <span @click="quickAsk('宿舍报修流程')">宿舍报修流程</span>
        <span @click="quickAsk('访客预约审核')">访客预约审核</span>
        <span @click="quickAsk('调宿申请条件')">调宿申请条件</span>
        <span @click="quickAsk('水电费缴纳')">水电费缴纳</span>
      </div>
    </aside>
  </div>
</template>

<script>
import { askAi } from '../api.js'

const STORAGE_KEY = 'dorm-ai-history'

const DEFAULT_SUGGESTIONS = [
  { title: '宿舍报修流程怎么提交？', meta: '进入报修申请，填写问题类型、位置和联系电话即可。' },
  { title: '访客预约需要提前多久？', meta: '建议至少提前 2 小时提交，等待宿管审核。' },
  { title: '调宿申请审核多久？', meta: '通常 1-3 个工作日完成审核。' },
  { title: '帮我生成一份空调报修说明', meta: 'AI 会帮你生成规范的报修内容。' }
]

function loadHistory() {
  try {
    const data = localStorage.getItem(STORAGE_KEY)
    return data ? JSON.parse(data) : []
  } catch (e) {
    return []
  }
}

function saveHistory(conversations) {
  localStorage.setItem(STORAGE_KEY, JSON.stringify(conversations))
}

export default {
  name: 'AiChat',
  props: {
    userInitial: { type: String, default: '用' }
  },
  data() {
    const saved = loadHistory()
    const hasSaved = saved.length > 0
    return {
      currentIndex: 0,
      conversations: hasSaved ? saved : [
        {
          title: '新对话',
          messages: [{ role: 'assistant', text: '你好，我是智能助手，可以帮你生成报修说明、查询宿舍制度、说明访客预约和调宿流程。' }],
          updatedAt: Date.now()
        }
      ],
      question: '',
      sending: false,
      suggestions: DEFAULT_SUGGESTIONS
    }
  },
  computed: {
    activeMessages() {
      const conv = this.conversations[this.currentIndex]
      return conv ? conv.messages : []
    },
    activeTitle() {
      const conv = this.conversations[this.currentIndex]
      return conv ? conv.title || '智能问答' : '智能问答'
    }
  },
  methods: {
    newConversation() {
      this.conversations.unshift({
        title: '新对话',
        messages: [{ role: 'assistant', text: '你好，我是智能助手，有什么可以帮助你的？' }],
        updatedAt: Date.now()
      })
      this.currentIndex = 0
      this.save()
      this.$nextTick(() => this.scrollDown())
    },
    switchTo(index) {
      this.currentIndex = index
      this.$nextTick(() => this.scrollDown())
    },
    deleteConv(index) {
      if (!window.confirm('确认删除此对话？')) return
      this.conversations.splice(index, 1)
      if (this.currentIndex >= this.conversations.length) {
        this.currentIndex = Math.max(0, this.conversations.length - 1)
      }
      if (this.conversations.length === 0) {
        this.newConversation()
      }
      this.save()
    },
    async send() {
      const text = this.question.trim()
      if (!text || this.sending) return
      this.question = ''

      const conv = this.conversations[this.currentIndex]
      if (!conv) return

      conv.messages.push({ role: 'user', text })
      // Auto-title using first user message
      if (conv.title === '新对话') {
        conv.title = text.length > 20 ? text.slice(0, 18) + '…' : text
      }
      conv.updatedAt = Date.now()
      this.sending = true

      try {
        const data = await askAi(text)
        conv.messages.push({ role: 'assistant', text: data.answer || '已收到你的问题，正在处理中……' })
      } catch (e) {
        conv.messages.push({ role: 'assistant', text: '已收到你的问题。建议先确认宿舍号、事项类型和紧急程度，并在对应模块提交申请。' })
      }
      this.sending = false
      this.save()
      this.$nextTick(() => this.scrollDown())
    },
    quickAsk(text) {
      this.question = text
    },
    scrollDown() {
      this.$nextTick(() => {
        const el = this.$refs.stream
        if (el) el.scrollTop = el.scrollHeight
      })
    },
    timeLabel(ts) {
      if (!ts) return ''
      const d = new Date(ts)
      const pad = n => String(n).padStart(2, '0')
      return `${d.getMonth() + 1}/${d.getDate()} ${pad(d.getHours())}:${pad(d.getMinutes())}`
    },
    save() {
      saveHistory(this.conversations)
    }
  },
  mounted() {
    this.$nextTick(() => this.scrollDown())
  }
}
</script>
