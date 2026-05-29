const BASE_URL = '/api'

function getToken() {
  const user = JSON.parse(sessionStorage.getItem('dorm-user') || 'null')
  return user ? user.token : null
}

async function request(url, options = {}) {
  const token = getToken()
  const headers = { 'Content-Type': 'application/json' }
  if (token) {
    headers['Authorization'] = 'Bearer ' + token
  }
  const response = await fetch(BASE_URL + url, {
    headers,
    ...options
  })
  const payload = await response.json()
  if (!payload.success) {
    if (response.status === 401) {
      sessionStorage.removeItem('dorm-user')
      window.location.reload()
    }
    throw new Error(payload.message || '请求失败')
  }
  return payload.data
}

export function login(credentials) {
  return request('/auth/login', {
    method: 'POST',
    body: JSON.stringify(credentials)
  })
}

export function logout() {
  return request('/auth/logout', { method: 'POST' })
}

export function fetchDashboard() {
  return request('/dashboard')
}

export function fetchRecords(category, keyword = '', page = 1, size = 20) {
  return request(`/records?category=${category}&keyword=${encodeURIComponent(keyword)}&page=${page}&size=${size}`)
}

export function createRecord(record) {
  return request('/records', {
    method: 'POST',
    body: JSON.stringify(record)
  })
}

export function updateRecord(id, record) {
  return request(`/records/${id}`, {
    method: 'PUT',
    body: JSON.stringify(record)
  })
}

export function deleteRecord(id) {
  return request(`/records/${id}`, { method: 'DELETE' })
}

export function askAi(question) {
  return request('/ai/chat', {
    method: 'POST',
    body: JSON.stringify({ question })
  })
}

export function fetchUsers(keyword = '', page = 1, size = 20) {
  return request(`/users?keyword=${encodeURIComponent(keyword)}&page=${page}&size=${size}`)
}

// Chat APIs
export function fetchContacts(userId) {
  return request(`/chat/contacts?userId=${userId}`)
}

export function fetchMessages(userId, withUserId) {
  return request(`/chat/messages?userId=${userId}&withUserId=${withUserId}`)
}

export function sendMessage(fromUserId, toUserId, content) {
  return request('/chat/send', {
    method: 'POST',
    body: JSON.stringify({ fromUserId, toUserId, content })
  })
}

export function fetchUnread(userId) {
  return request(`/chat/unread?userId=${userId}`)
}

export function fetchWeather() {
  return request('/weather')
}
