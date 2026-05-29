<template>
  <div class="call-layout">
    <aside class="contact-panel">
      <div class="card-title"><h3>联系人</h3></div>
      <div class="contact-list-vert">
        <div
          v-for="contact in contacts"
          :key="contact.id"
          class="contact-card"
          :class="{ active: activeContact && activeContact.id === contact.id }"
          @click="selectContact(contact)"
        >
          <span class="avatar-dot" :style="{ background: avatarColor(contact.realName) }">{{ contact.realName.slice(0, 1) }}</span>
          <div>
            <b>{{ contact.realName }}</b>
            <p>{{ roleLabel(contact.role) }} · {{ contact.roomNo || '未设置' }}</p>
          </div>
          <em :class="{ online: contact.online }">{{ contact.online ? '在线' : '离线' }}</em>
        </div>
      </div>
    </aside>

    <section class="content-card chat-area" v-if="activeContact">
      <div class="card-title inline">
        <h3>{{ activeContact.realName }}</h3>
        <span>{{ roleLabel(activeContact.role) }}</span>
      </div>
      <div class="chat-stream" ref="msgContainer">
        <div v-for="msg in messages" :key="msg.id" class="message" :class="msg.fromUserId === currentUserId ? 'user' : 'assistant'">
          <span>{{ msg.fromUserId === currentUserId ? '我' : activeContact.realName.slice(0, 1) }}</span>
          <p>{{ msg.content }}</p>
        </div>
        <div v-if="messages.length === 0" class="chat-hint">发送一条消息开始对话</div>
      </div>
      <div class="chat-composer">
        <input v-model="inputText" placeholder="请输入消息" @keyup.enter="sendMsg">
        <button class="blue-btn" @click="sendMsg" :disabled="!inputText.trim()">发送</button>
      </div>
    </section>

    <section class="content-card chat-empty" v-else>
      <div class="empty-chat-hint">
        <div class="empty-icon">💬</div>
        <h3>智慧通讯</h3>
        <p>选择一个联系人开始通话</p>
      </div>
    </section>
  </div>
</template>

<script>
import { fetchContacts, fetchMessages, sendMessage } from '../api.js'

const ROLE_MAP = { admin: '管理员', student: '学生', dormkeeper: '宿管员' }
const COLORS = ['#4b8fe8', '#5aac6e', '#d98c45', '#c47d8b', '#6c8ebf', '#8c7db5', '#b58d7f', '#8798a7']

export default {
  name: 'CallPage',
  props: {
    currentUserId: { type: Number, required: true }
  },
  data() {
    return {
      contacts: [],
      activeContact: null,
      messages: [],
      inputText: ''
    }
  },
  async mounted() {
    try {
      this.contacts = await fetchContacts(this.currentUserId)
      // Simulate online status
      this.contacts.forEach(c => {
        c.online = Math.random() > 0.3
      })
    } catch (e) { /* ignore */ }
  },
  methods: {
    roleLabel(role) { return ROLE_MAP[role] || role },
    avatarColor(name) {
      let hash = 0
      for (let i = 0; i < name.length; i++) hash = name.charCodeAt(i) + ((hash << 5) - hash)
      return COLORS[Math.abs(hash) % COLORS.length]
    },
    async selectContact(contact) {
      this.activeContact = contact
      try {
        this.messages = await fetchMessages(this.currentUserId, contact.id)
        this.$nextTick(() => this.scrollDown())
      } catch (e) { /* ignore */ }
    },
    async sendMsg() {
      const text = this.inputText.trim()
      if (!text || !this.activeContact) return
      this.inputText = ''
      try {
        const msg = await sendMessage(this.currentUserId, this.activeContact.id, text)
        this.messages.push(msg)
        this.$nextTick(() => this.scrollDown())
      } catch (e) { /* ignore */ }
    },
    scrollDown() {
      const el = this.$refs.msgContainer
      if (el) el.scrollTop = el.scrollHeight
    }
  }
}
</script>
