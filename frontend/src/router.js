const MENUS = [
  { key: 'dashboard', label: '服务台', icon: '◉', roles: ['student'], section: '学生服务', desc: '学生首页工作台' },
  { key: 'myDorm', label: '我的宿舍', icon: '⌂', roles: ['student'], section: '学生服务', desc: '宿舍信息与室友' },
  { key: 'repair', label: '报修申请', icon: '⚙', roles: ['student', 'admin', 'dormkeeper'], section: '学生服务', desc: '提交维修申请' },
  { key: 'fee', label: '费用查询', icon: '▣', roles: ['student', 'admin'], section: '学生服务', desc: '水电费账单与缴费' },
  { key: 'announcement', label: '校园公告', icon: '▤', roles: ['student', 'admin', 'dormkeeper'], section: '学生服务', desc: '查看系统公告' },
  { key: 'visitor', label: '访客预约', icon: '♟', roles: ['student', 'dormkeeper'], section: '学生服务', desc: '提交访客来访申请' },
  { key: 'rating', label: '服务评价', icon: '☆', roles: ['student'], section: '学生服务', desc: '评价宿舍服务与维修' },
  { key: 'transfer', label: '调宿申请', icon: '↔', roles: ['student', 'admin'], section: '学生服务', desc: '申请换宿舍/换床位' },
  { key: 'ai', label: 'AI助手', icon: '☏', roles: ['student'], section: '学生服务', desc: '宿舍相关问题咨询' },
  { key: 'chat', label: '智能通话', icon: '▻', roles: ['student', 'admin', 'dormkeeper'], section: '学生服务', desc: '与舍友和宿管实时聊天' },
  { key: 'feedback', label: '意见反馈', icon: '✎', roles: ['student'], section: '学生服务', desc: '提交意见与建议' },
  { key: 'account', label: '账户设置', icon: '⚙', roles: ['student', 'admin', 'dormkeeper'], section: '学生服务', desc: '个人信息管理' },
  { key: 'dashboard', label: '控制台', icon: '◉', roles: ['admin', 'dormkeeper'], section: '后台管理', desc: '数据概览与统计图表' },
  { key: 'building', label: '楼栋管理', icon: '▥', roles: ['admin'], section: '后台管理', desc: '宿舍楼 CRUD' },
  { key: 'room', label: '房间管理', icon: '▤', roles: ['admin', 'dormkeeper'], section: '后台管理', desc: '房间信息与床位配置' },
  { key: 'student', label: '学生管理', icon: '◎', roles: ['admin', 'dormkeeper'], section: '后台管理', desc: '学生信息与入住状态' },
  { key: 'dormkeeper', label: '宿管员管理', icon: '◈', roles: ['admin'], section: '后台管理', desc: '宿管人员配置' },
  { key: 'hygiene', label: '卫生检查', icon: '✓', roles: ['dormkeeper'], section: '宿管工作', desc: '宿舍卫生打分记录' },
  { key: 'lateReturn', label: '晚归登记', icon: '◷', roles: ['dormkeeper'], section: '宿管工作', desc: '晚归学生记录' },
  { key: 'item', label: '物品出入', icon: '▧', roles: ['dormkeeper'], section: '宿管工作', desc: '大件物品出入登记' }
]

const ROLE_LABELS = { admin: '管理员', student: '学生端', dormkeeper: '宿管员' }

const PAGES_WITHOUT_RECORDS = ['dashboard', 'myDorm', 'ai', 'account', 'chat']

export function getVisibleMenus(role) {
  if (!role) return []
  return MENUS.filter(item => item.roles.includes(role))
}

export function getMenuByKey(menus, key) {
  return menus.find(item => item.key === key)
}

export function roleLabel(role) {
  return ROLE_LABELS[role] || role
}

export function categoryName(menus, category) {
  const menu = menus.find(item => item.key === category)
  return menu ? menu.label : category
}

export function needsRecords(menuKey) {
  return !PAGES_WITHOUT_RECORDS.includes(menuKey)
}

export function isTodoStatus(status) {
  return ['待处理', '待审核', '审核中', '待缴费', '待回复'].includes(status)
}
