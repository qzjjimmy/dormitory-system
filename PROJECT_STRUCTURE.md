# 学生宿舍管理系统 — 项目结构文档

## 目录总览

```
dormitory-system/
├── pom.xml                                  # Maven 项目配置
├── start.bat                                # 一键启动脚本
├── stop.bat                                 # 一键停止脚本
├── .gitignore                               # Git 忽略规则
├── database.sql                             # 数据库建表 SQL（备用）
├── PROJECT_STRUCTURE.md                     # 本文档
├── PROJECT_FEATURES.md                      # 功能文档
├── IMPROVEMENTS.md                          # 改进建议（含完成状态）
├── docs/
│   └── superpowers/
│       ├── specs/
│       │   └── 2026-05-29-smart-dorm-assignment-design.md  # 智能分配算法设计文档
│       └── plans/
│           └── 2026-05-29-smart-dorm-assignment-plan.md   # 智能分配算法实现计划
├── src/main/
│   ├── java/com/bishe/dormitory/
│   │   ├── DormitorySystemApplication.java  # Spring Boot 入口
│   │   ├── config/
│   │   │   ├── CorsConfig.java              # 跨域配置
│   │   │   ├── DatabaseInitializer.java     # 数据库初始化 & 种子数据（BCrypt 加密）
│   │   │   ├── WebConfig.java               # 拦截器注册（登录鉴权）
│   │   │   └── WebSocketConfig.java         # WebSocket 注册配置
│   │   ├── common/
│   │   │   ├── ApiResponse.java             # 统一响应体封装
│   │   │   ├── GlobalExceptionHandler.java  # 全局异常处理
│   │   │   ├── PageResult.java              # 分页结果封装（已接入查询）
│   │   │   └── TokenStore.java              # Token 存储（内存 Map）
│   │   ├── controller/
│   │   │   ├── AuthController.java          # 登录/登出（含 token 签发 + 参数校验）
│   │   │   ├── DashboardController.java     # 控制台统计接口
│   │   │   ├── HealthController.java        # 健康检查接口
│   │   │   ├── RecordController.java        # 业务记录 CRUD 接口（已接入分页）
│   │   │   ├── UserController.java          # 用户管理接口（已接入分页）
│   │   │   ├── AiController.java            # AI 对话接口 (DeepSeek API)
│   │   │   ├── ChatController.java          # 即时通讯 REST 接口
│   │   │   ├── AssignmentController.java     # 智能分配接口 (run/recommend/confirm)
│   │   │   └── WeatherController.java       # 实时天气接口 (wttr.in)
│   │   ├── interceptor/
│   │   │   └── LoginInterceptor.java        # 登录拦截器（Bearer Token 校验）
│   │   ├── service/
│   │   │   ├── RecordService.java           # 业务记录逻辑（已接入分页 LIMIT/OFFSET）
│   │   │   ├── UserService.java             # 用户管理逻辑（BCrypt 密码 + 分页 + 特征 + 注册）
│   │   │   ├── ChatService.java             # 聊天消息逻辑
│   │   │   └── RoomAssignmentService.java   # 智能宿舍分配算法（两阶段贪心匹配 + 调宿推荐）
│   │   ├── model/
│   │   │   ├── SysUser.java                 # 系统用户实体
│   │   │   └── BizRecord.java               # 业务记录实体
│   │   └── websocket/
│   │       └── ChatWebSocketHandler.java    # WebSocket 连接管理 & 消息推送
│   └── resources/
│       ├── application.properties           # 应用配置
│       └── static/index.html                # 默认静态首页
├── src/test/
│   └── java/com/bishe/dormitory/controller/
│       └── AuthControllerTest.java          # 单元测试（7 个用例）
└── frontend/
    ├── package.json                         # npm 项目配置
    ├── index.html                           # Vite 入口 HTML
    └── src/
        ├── main.js                          # Vue 应用入口
        ├── App.vue                          # 根组件 (登录 + 布局 + 路由分发)
        ├── router.js                        # 菜单配置 & 角色权限路由
        ├── api.js                           # 后端 API 封装层
        ├── style.css                        # 全局样式
        └── components/
            ├── StudentDashboard.vue         # 学生端首页工作台
            ├── MyDorm.vue                   # 我的宿舍 (室友/床位/设施/评分)
            ├── AdminDashboard.vue           # 管理端控制台
            ├── RecordList.vue               # 通用记录列表 (表格+CRUD)
            ├── AccountSettings.vue          # 账户设置
            ├── AiChat.vue                   # AI 智能助手 (多轮对话+历史，按用户隔离)
            ├── ChatView.vue                 # WebSocket 即时通讯
            ├── RegisterForm.vue             # 新生注册（两页表单 + 特征采集）
            ├── ProfileCompletionModal.vue   # 老用户特征补全弹窗
            ├── SmartAssignment.vue          # 智能宿舍分配（管理员：矩阵 + 调宿推荐）
            ├── RatingPage.vue               # 服务评价
            ├── VisitorPage.vue              # 访客预约
            ├── TransferPage.vue             # 调宿申请
            ├── CheckinManage.vue            # 入住管理 (管理员)
            └── RoomManage.vue               # 房间管理 (管理员)
```

---

## 架构分层

项目采用 **Controller → Service → JDBC** 三层架构：

```
浏览器 (Vue 3)
    │  HTTP REST / WebSocket
    ▼
Controller   ←  参数校验、路由映射、响应封装
    │
    ▼
Service      ←  业务逻辑、数据校验、SQL 组装
    │
    ▼
JdbcTemplate ←  JDBC 直接操作 MySQL
```

Controller 不直接操作数据库，所有 SQL 和业务逻辑封装在 Service 层中，方便单元测试和后续扩展（例如替换为 MyBatis）。

---

## 后端文件说明

### 入口

| 文件 | 说明 |
|------|------|
| `DormitorySystemApplication.java` | Spring Boot 启动类，`@SpringBootApplication` 自动装配 |

### config — 配置层

| 文件 | 说明 |
|------|------|
| `CorsConfig.java` | CORS 跨域配置，允许前端 `localhost:5173` 请求后端 `/api/**` 路径 |
| `DatabaseInitializer.java` | 实现 `CommandLineRunner`，启动时自动建表并插入种子数据（7 个用户 + 100+ 条业务记录 + 聊天消息）。用户密码采用 BCrypt 加密，聊天消息采用幂等判断避免重复插入 |
| `WebConfig.java` | 注册 `LoginInterceptor`，拦截 `/api/**` 路径，排除 `/api/auth/login`、`/api/auth/register` |
| `WebSocketConfig.java` | 注册 WebSocket 端点 `/ws/chat`，绑定 `ChatWebSocketHandler` |

### common — 公共组件

| 文件 | 说明 |
|------|------|
| `ApiResponse.java` | 统一 API 响应体 `{ success, message, data }`，提供静态工厂方法 |
| `GlobalExceptionHandler.java` | `@RestControllerAdvice` 全局异常拦截，`IllegalArgumentException` → 400，其他 → 500 |
| `PageResult.java` | 分页结果封装 `{ rows, total, page, size, pages }`，RecordService 和 UserService 的 list 方法已接入 |
| `TokenStore.java` | Token 存储管理，`ConcurrentHashMap` 维护 `token → user` 映射，提供 create/get/remove 方法 |

### interceptor — 拦截器层

| 文件 | 说明 |
|------|------|
| `LoginInterceptor.java` | 从 `Authorization: Bearer <token>` 头取 token，校验有效性后将用户信息存入 `request.setAttribute`，无效返回 401 |

### controller — 接口层

| 文件 | 路径 | 说明 |
|------|------|------|
| `AuthController.java` | `POST /api/auth/login`, `/api/auth/logout`, `/api/auth/register` | 登录验证（BCrypt + 签发 token + 参数空值校验），登出清除 token，注册自动登录 |
| `DashboardController.java` | `GET /api/dashboard` | 控制台统计，调用 `RecordService.dashboard()` |
| `HealthController.java` | `GET /api/health` | 健康检查，验证数据库连接状态 |
| `RecordController.java` | `GET/POST/PUT/DELETE /api/records` | 业务记录 CRUD，支持分页参数 page/size，调用 `RecordService` |
| `UserController.java` | `GET/POST/PUT/DELETE /api/users`, `PUT /api/users/profile`, `PUT /api/users/password` | 用户管理 CRUD + 分页 + 特征更新 + 修改密码 |
| `AiController.java` | `POST /api/ai/chat` | AI 对话，对接 DeepSeek API（真实调用，key 降级自动走本地回复） |
| `ChatController.java` | `GET/POST /api/chat/*` | 即时通讯 REST API，调用 `ChatService`，发送后通过 `ChatWebSocketHandler` 推送 |
| `WeatherController.java` | `GET /api/weather` | 通过 wttr.in 免费 API 实时获取福州市连江县天气，无需登录 |
| `AssignmentController.java` | `POST /api/assignment/run`, `GET /api/assignment/recommend/{id}`, `POST /api/assignment/confirm`, `GET /api/assignment/heatmap`, `GET /api/assignment/rooms` | 智能宿舍分配：运行算法、调宿推荐、确认分配+WebSocket通知、热力图数据、已有宿舍查询 |

### service — 业务层

| 文件 | 对应表 | 主要方法 |
|------|--------|----------|
| `RecordService.java` | `biz_record` | `list(category, keyword, page, size)` → `PageResult` / `create(record)` / `update(id, record)` / `delete(id)` / `dashboard()` / `validateRecord(record)` |
| `UserService.java` | `sys_user` | `login(username, password)` 使用 BCrypt 验证 + 自动升级明文密码 + 返回 profileComplete / `list(keyword, page, size)` → `PageResult` / `create(user)` BCrypt 加密含 12 特征字段 / `update(id, user)` / `delete(id)` / `updateProfile(userId, profile)` / `validateUser(user, isCreate)` |
| `ChatService.java` | `chat_message` | `getContacts(userId)` 未分配学生仅见管理员/宿管，分配后显示室友 / `getMessages(userId, withUserId)` / `markAsRead(userId, fromUserId)` / `getUnreadCounts(userId)` / `sendMessage(from, to, content)` |
| `RoomAssignmentService.java` | — | `executeAssignment()` 两阶段贪心分配 / `recommendTransfer(studentId)` Top-3 调宿推荐 / `confirmAssignment(assignments)` 写入 room_no / `compatibilityScore(a, b)` 10 维加权评分 |

### model — 实体

| 文件 | 对应表 | 字段 |
|------|--------|------|
| `SysUser.java` | `sys_user` | id / username / password / realName / role / phone / roomNo / gender / majorClass / sleepHabit / smoking / hobbies / cleanliness / gaming / snoring / returnTime / noiseTolerance / preferredRoomType / preferredBed / createdAt |
| `BizRecord.java` | `biz_record` | id / category / title / owner / location / amount / status / content / createdAt / updatedAt |

### websocket — 即时通讯

| 文件 | 说明 |
|------|------|
| `ChatWebSocketHandler.java` | 管理 WebSocket 连接（`ConcurrentHashMap<userId, session>`），提供 `pushToUser(id, payload)` 实时推送，`isOnline(id)` 在线状态查询 |

### resources — 配置

| 文件 | 说明 |
|------|------|
| `application.properties` | 服务端口 8080、MySQL 数据源、DeepSeek API 可选配置 |
| `static/index.html` | Spring Boot 默认静态首页（前端由 Vite 独立运行，此文件可忽略） |

### test — 测试

| 文件 | 说明 |
|------|------|
| `AuthControllerTest.java` | `@WebMvcTest` 单元测试，覆盖登录成功/失败/空参数、记录查询/创建/删除、未授权访问共 7 个用例 |

---

## 前端文件说明

### 入口 & 环境

| 文件 | 说明 |
|------|------|
| `package.json` | npm 项目配置，依赖 Vue 3、Vite 5 |
| `index.html` | Vite 入口 HTML，标题"学生宿舍管理系统" |
| `main.js` | Vue 应用创建入口，挂载 App 和全局样式 |

### 核心架构

| 文件 | 说明 |
|------|------|
| `App.vue` | 根组件：登录页 + 系统壳（侧栏/顶栏/内容区），根据角色分发页面，右上角用户下拉菜单 |
| `router.js` | 菜单配置 `MENUS` 数组（key/label/icon/role/section），导出角色过滤、分类名映射等方法 |
| `api.js` | API 封装层：基于 `fetch` 的统一请求函数，自动携带 `Authorization` token，401 自动登出，接口均支持分页参数 |

### 组件

| 文件 | 适用角色 | 说明 |
|------|----------|------|
| `StudentDashboard.vue` | 学生 | 首页：欢迎语、北京时间、天气、公告、快捷卡片、评分圆环、室友 |
| `MyDorm.vue` | 学生 | 我的宿舍：宿舍信息、室友列表、床位布局、设施状态+卫生评分合并卡片 |
| `AdminDashboard.vue` | 管理员/宿管 | 控制台：统计卡片 + 分类柱状图 |
| `RecordList.vue` | 通用 | 通用列表：搜索框 + 数据表格 + 新增/编辑弹窗 + 删除 + 通过/驳回审核按钮 |
| `AccountSettings.vue` | 全部 | 账户设置：基本信息只读 + 电话编辑 + 密码修改 + 学生特征卡片网格 |
| `AiChat.vue` | 学生 | AI 助手：左侧对话历史 + 中间多轮消息气泡 + 右侧建议面板，历史按用户 ID 隔离 localStorage |
| `ChatView.vue` | 全部 | 即时通讯：联系人列表（未分配仅见管理员/宿管）+ WebSocket 实时消息 + 未读角标 |
| `RegisterForm.vue` | 学生（登录页） | 新生注册：两页表单（账号 + 10 特征）+ 房间/床位偏好选择 |
| `ProfileCompletionModal.vue` | 学生 | 老用户首次登录强制补全特征弹窗，不可跳过 |
| `SmartAssignment.vue` | 管理员 | 智能分配：热力图 + 宿舍得分仪表盘 + 床位矩阵 + 调宿 Top-3 + 分配前后对比 |
| `RatingPage.vue` | 学生 | 服务评价：综合评分圆环 + 评价记录 + 提交表单 |
| `VisitorPage.vue` | 学生 | 访客预约：记录表格 + 点击展开预约表单 |
| `TransferPage.vue` | 学生 | 调宿申请：记录表格 + 点击展开申请表 |
| `CheckinManage.vue` | 管理员/宿管 | 入住管理：统计卡片 + 学生卡片网格（对接 sys_user 真实数据） |
| `RoomManage.vue` | 管理员/宿管 | 房间管理：自动从 sys_user 聚合宿舍数据，统计+卡片+成员列表 |

### 样式

| 文件 | 说明 |
|------|------|
| `style.css` | 全局 CSS：登录页、侧栏、卡片、表格、表单、状态标签、床位布局、设施、评分、AI对话、通讯、管理面板、下拉菜单、响应式断点 |

---

## 外层文件

| 文件 | 说明 |
|------|------|
| `pom.xml` | Maven 配置：Spring Boot 2.7.18、JDBC、MySQL Connector、WebSocket、spring-security-crypto、Test |
| `database.sql` | 手动建表 SQL（备用，实际由 `DatabaseInitializer` 自动建表） |
| `start.bat` | 一键启动：分别打开后端 `mvn spring-boot:run` 和前端 `npm run dev` |
| `stop.bat` | 一键停止：关闭 8080 和 5173 端口进程 |
| `.gitignore` | Git 忽略规则，排除 target/ node_modules/ *.log .idea/ *.iml dist/ 等 |
| `PROJECT_FEATURES.md` | 项目功能文档 |
| `IMPROVEMENTS.md` | 后续改进建议（含完成状态标注） |
