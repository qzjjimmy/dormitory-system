<template>
  <div class="chat-shell">
    <!-- Contact List Sidebar -->
    <aside class="chat-sidebar">
      <div class="chat-sidebar-header">
        <div class="search-box">
          <span>⌕</span>
          <input v-model="searchText" placeholder="搜索联系人" @input="filterContacts">
        </div>
      </div>
      <div class="contact-list">
        <div
          v-for="contact in filteredContacts"
          :key="contact.id"
          class="contact-item"
          :class="{ active: activeContact && activeContact.id === contact.id }"
          @click="selectContact(contact)"
        >
          <div class="contact-avatar" :style="{ background: avatarColor(contact.realName) }">
            {{ contact.realName.slice(0, 1) }}
          </div>
          <div class="contact-info">
            <div class="contact-top">
              <b>{{ contact.realName }}</b>
              <span class="contact-time">{{ formatBriefTime(contact.lastTime) }}</span>
            </div>
            <div class="contact-bot">
              <span class="contact-preview">{{ contact.lastMsg || roleLabel(contact.role) }}</span>
              <span v-if="contact.unread > 0" class="unread-badge">{{ contact.unread > 99 ? '99+' : contact.unread }}</span>
            </div>
          </div>
        </div>
        <div v-if="filteredContacts.length === 0" class="no-contact">暂无联系人</div>
      </div>
    </aside>

    <!-- Chat Area -->
    <main class="chat-main" v-if="activeContact">
      <header class="chat-header">
        <div class="chat-header-info">
          <b>{{ activeContact.realName }}</b>
          <span>{{ roleLabel(activeContact.role) }} · {{ activeContact.roomNo || '未设置' }}</span>
        </div>
      </header>

      <div class="chat-messages" ref="msgContainer">
        <div
          v-for="msg in messages"
          :key="msg.id"
          class="msg-row"
          :class="{ mine: msg.fromUserId === currentUserId }"
        >
          <div class="msg-bubble" :class="{ mine: msg.fromUserId === currentUserId }">
            <div class="msg-text">{{ msg.content }}</div>
          </div>
        </div>
        <div v-if="messages.length === 0" class="chat-hint">
          暂无消息，发送一条消息开始聊天吧
        </div>
      </div>

      <footer class="chat-input-bar">
        <textarea
          v-model="inputText"
          @keydown.enter.exact.prevent="sendMessage"
          placeholder="输入消息..."
          rows="1"
          ref="inputRef"
        ></textarea>
        <button class="send-btn" @click="sendMessage" :disabled="!inputText.trim()">发送</button>
      </footer>
    </main>

    <!-- Empty state -->
    <main class="chat-main chat-empty" v-else>
      <div class="empty-chat-hint">
        <div class="empty-icon">💬</div>
        <h3>智慧通讯</h3>
        <p>选择一个联系人开始聊天</p>
      </div>
    </main>
  </div>
</template>

<script>
import { fetchContacts, fetchMessages, sendMessage, fetchUnread } from '../api.js'

const ROLE_MAP = { admin: '管理员', student: '学生', dormkeeper: '宿管员' }
const COLORS = ['#4b8fe8', '#5aac6e', '#d98c45', '#c47d8b', '#6c8ebf', '#8c7db5', '#b58d7f', '#8798a7']

export default {
  name: 'ChatView',
  props: {
    currentUserId: { type: Number, required: true }
  },
  data() {
    return {
      contacts: [],
      activeContact: null,
      messages: [],
      inputText: '',
      searchText: '',
      ws: null,
      unreadPollTimer: null
    }
  },
  computed: {
    filteredContacts() {
      if (!this.searchText.trim()) return this.contacts
      const kw = this.searchText.toLowerCase()
      return this.contacts.filter(c =>
        c.realName.toLowerCase().includes(kw) ||
        this.roleLabel(c.role).includes(kw) ||
        (c.roomNo && c.roomNo.includes(kw))
      )
    }
  },
  async mounted() {
    await this.loadContacts()
    this.connectWebSocket()
    this.unreadPollTimer = setInterval(() => this.refreshUnread(), 5000)
  },
  beforeUnmount() {
    if (this.ws) this.ws.close()
    if (this.unreadPollTimer) clearInterval(this.unreadPollTimer)
  },
  methods: {
    roleLabel(role) { return ROLE_MAP[role] || role },
    avatarColor(name) {
      let hash = 0
      for (let i = 0; i < name.length; i++) hash = name.charCodeAt(i) + ((hash << 5) - hash)
      return COLORS[Math.abs(hash) % COLORS.length]
    },
    formatBriefTime(time) {
      if (!time) return ''
      const d = new Date(time)
      const now = new Date()
      const pad = n => String(n).padStart(2, '0')
      if (d.toDateString() === now.toDateString()) {
        return `${pad(d.getHours())}:${pad(d.getMinutes())}`
      }
      return `${d.getMonth() + 1}/${d.getDate()}`
    },

    async loadContacts() {
      try {
        this.contacts = await fetchContacts(this.currentUserId)
        await this.refreshUnread()
        // Sort: unread first, then by last message time
        this.contacts.sort((a, b) => (b.unread || 0) - (a.unread || 0))
      } catch (e) {
        console.error('Failed to load contacts', e)
      }
    },

    async refreshUnread() {
      try {
        const data = await fetchUnread(this.currentUserId)
        const counts = data.counts || []
        for (const c of this.contacts) {
          const found = counts.find(x => x.fromUserId === c.id)
          c.unread = found ? found.cnt : 0
        }
        this.contacts.sort((a, b) => (b.unread || 0) - (a.unread || 0))
      } catch (e) { /* ignore */ }
    },

    async selectContact(contact) {
      this.activeContact = contact
      this.messages = []
      try {
        this.messages = await fetchMessages(this.currentUserId, contact.id)
        contact.unread = 0
        this.$nextTick(() => this.scrollToBottom())
      } catch (e) {
        console.error('Failed to load messages', e)
      }
    },

    async sendMessage() {
      const text = this.inputText.trim()
      if (!text || !this.activeContact) return
      this.inputText = ''

      try {
        const msg = await sendMessage(this.currentUserId, this.activeContact.id, text)
        this.messages.push(msg)
        // Update contact's last message
        this.activeContact.lastMsg = text
        this.activeContact.lastTime = msg.createdAt
        this.$nextTick(() => this.scrollToBottom())
      } catch (e) {
        console.error('Send failed', e)
      }
    },

    scrollToBottom() {
      const el = this.$refs.msgContainer
      if (el) el.scrollTop = el.scrollHeight
    },

    connectWebSocket() {
      const protocol = location.protocol === 'https:' ? 'wss:' : 'ws:'
      const wsUrl = `${protocol}//${location.host}/ws/chat?userId=${this.currentUserId}`
      this.ws = new WebSocket(wsUrl)
      this.ws.onmessage = (event) => {
        try {
          const data = JSON.parse(event.data)
          if (data.type === 'new_message' && data.message) {
            const msg = data.message
            // If this chat is currently open with the sender, append message
            if (this.activeContact && this.activeContact.id === msg.fromUserId) {
              this.messages.push(msg)
              this.$nextTick(() => this.scrollToBottom())
              fetchMessages(this.currentUserId, msg.fromUserId)
            }
            // Update contact preview
            const contact = this.contacts.find(c => c.id === msg.fromUserId)
            if (contact) {
              contact.lastMsg = msg.content
              contact.lastTime = msg.createdAt
              if (!this.activeContact || this.activeContact.id !== msg.fromUserId) {
                contact.unread = (contact.unread || 0) + 1
              }
              this.contacts.sort((a, b) => (b.unread || 0) - (a.unread || 0))
            }
          }
          if (data.type === 'dorm_assigned') {
            // Update sessionStorage and reload to refresh menus
            const user = JSON.parse(sessionStorage.getItem('dorm-user') || '{}')
            user.roomNo = (data.roomNo || '') + ' · ' + (data.bedNo || '')
            sessionStorage.setItem('dorm-user', JSON.stringify(user))
            window.location.reload()
          }
        } catch (e) { /* ignore */ }
      }
      this.ws.onclose = () => {
        // Reconnect after 3 seconds
        setTimeout(() => this.connectWebSocket(), 3000)
      }
    },

    filterContacts() {
      // computed handles this
    }
  }
}
</script>

<style scoped>
.chat-shell {
  display: flex;
  height: calc(100vh - 82px - 42px);
  background: #f5f6fa;
  border-radius: 12px;
  overflow: hidden;
  margin: 0 44px;
}

/* Sidebar */
.chat-sidebar {
  width: 300px;
  min-width: 260px;
  background: #fff;
  border-right: 1px solid #e8eaef;
  display: flex;
  flex-direction: column;
}
.chat-sidebar-header {
  padding: 16px;
  border-bottom: 1px solid #f0f1f5;
}
.search-box {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  background: #f5f6fa;
  border-radius: 6px;
}
.search-box span {
  color: #b0b5c0;
  font-size: 18px;
}
.search-box input {
  border: 0;
  background: transparent;
  outline: none;
  flex: 1;
  font-size: 14px;
}
.contact-list {
  flex: 1;
  overflow-y: auto;
}
.contact-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 16px;
  cursor: pointer;
  transition: background .15s;
}
.contact-item:hover { background: #f5f7fa; }
.contact-item.active { background: #e8f0fe; }
.contact-avatar {
  width: 44px;
  height: 44px;
  border-radius: 6px;
  display: grid;
  place-items: center;
  color: #fff;
  font-weight: 700;
  font-size: 17px;
  flex-shrink: 0;
}
.contact-info {
  flex: 1;
  min-width: 0;
}
.contact-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 4px;
}
.contact-top b { font-size: 15px; }
.contact-time {
  font-size: 11px;
  color: #b5bac5;
}
.contact-bot {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.contact-preview {
  font-size: 13px;
  color: #9ba2ae;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 170px;
}
.unread-badge {
  background: #f5574c;
  color: #fff;
  border-radius: 10px;
  padding: 0 6px;
  font-size: 11px;
  min-width: 18px;
  text-align: center;
  line-height: 18px;
}
.no-contact {
  text-align: center;
  padding: 40px;
  color: #b5bac5;
}

/* Main chat area */
.chat-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
  background: #f5f6fa;
}
.chat-header {
  padding: 16px 24px;
  background: #fff;
  border-bottom: 1px solid #e8eaef;
  display: flex;
  align-items: center;
}
.chat-header-info b { display: block; font-size: 16px; }
.chat-header-info span { font-size: 12px; color: #9ba2ae; }

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 20px 24px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.msg-row { display: flex; }
.msg-row.mine { justify-content: flex-end; }
.msg-bubble {
  max-width: 65%;
  padding: 10px 14px;
  border-radius: 8px;
  background: #fff;
  box-shadow: 0 1px 2px rgba(0,0,0,.06);
  position: relative;
}
.msg-bubble.mine {
  background: #95ec69;
}
.msg-text {
  font-size: 14px;
  line-height: 1.6;
  word-break: break-word;
}
.chat-hint {
  text-align: center;
  color: #b5bac5;
  padding: 60px 0;
}

/* Input bar */
.chat-input-bar {
  display: flex;
  align-items: flex-end;
  gap: 12px;
  padding: 14px 20px;
  background: #fff;
  border-top: 1px solid #e8eaef;
}
.chat-input-bar textarea {
  flex: 1;
  border: 0;
  outline: none;
  resize: none;
  font-size: 14px;
  line-height: 1.5;
  padding: 8px 0;
  max-height: 100px;
  background: transparent;
}
.send-btn {
  border: 0;
  background: #4b8fe8;
  color: #fff;
  border-radius: 6px;
  padding: 8px 20px;
  font-weight: 700;
  cursor: pointer;
  flex-shrink: 0;
}
.send-btn:disabled {
  background: #c8d6e5;
  cursor: not-allowed;
}

/* Empty state */
.chat-empty {
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f6fa;
}
.empty-chat-hint {
  text-align: center;
}
.empty-icon {
  font-size: 56px;
  margin-bottom: 16px;
}
.empty-chat-hint h3 {
  margin: 0 0 8px;
  font-size: 20px;
  color: #4d5e70;
}
.empty-chat-hint p {
  color: #9ba2ae;
  margin: 0;
}

@media (max-width: 820px) {
  .chat-shell {
    margin: 0;
    height: calc(100vh - 140px);
  }
  .chat-sidebar {
    width: 100%;
    min-width: 0;
  }
  .chat-shell:has(.activeContact) .chat-sidebar {
    display: none;
  }
}
</style>
