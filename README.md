# 学生宿舍管理系统

基于 Spring Boot + Vue 3 + MySQL 的智慧宿舍管理毕设项目，含学生端、管理员端、宿管员端。前后端分离，集成 DeepSeek AI 问答、WebSocket 实时通讯、**智能宿舍分配算法**。

## 本机环境适配

- JDK 8
- Maven 3.9.6
- MySQL 8.0
- Node.js 18 / npm 10

## 默认账号

| 角色 | 账号 | 密码 |
| --- | --- | --- |
| 管理员 | admin | 123456 |
| 学生 | student | 123456 |
| 宿管员 | dormkeeper | 123456 |

> 密码使用 BCrypt 加密存储。首次使用请先执行 `DROP DATABASE IF EXISTS dormitory_system` 清除旧数据。

## 启动后端

1. 确认 MySQL 正在运行。
2. 复制 `application.properties.example` 为 `src/main/resources/application.properties`，填入你的 MySQL 密码和 DeepSeek API Key。
3. 在项目目录执行：

```bash
mvn spring-boot:run
```

后端 API 地址：`http://localhost:8080`

应用首次启动会自动创建 `dormitory_system` 数据库和演示数据（密码以 BCrypt 哈希存储）。

## 启动前端

打开另一个终端：

```bash
cd frontend
npm install
npm run dev
```

前端页面地址：`http://localhost:5173`

Vite 已配置代理，前端请求 `/api` 会自动转发到 `http://localhost:8080`。

## 一键启停

- 双击 `start.bat` 启动后端 + 前端
- 双击 `stop.bat` 停止所有服务

## 安全特性

- **BCrypt 密码加密**：用户密码使用 `BCryptPasswordEncoder` 哈希存储，不可逆
- **Token 鉴权**：登录返回 UUID token，后续请求通过 `Authorization: Bearer <token>` 携带
- **接口拦截**：`LoginInterceptor` 拦截未授权请求，返回 401
- **参数校验**：所有 Controller 入口做空值校验

## 数据特性

- **分页查询**：`/api/records` 和 `/api/users` 支持 `page`/`size` 参数，`RecordService` 和 `UserService` 已接入 `LIMIT/OFFSET`
- **幂等种子**：所有种子数据均通过 `COUNT=0` 判断，多次启动不会重复插入
- **单元测试**：`AuthControllerTest` 覆盖 7 个测试用例

## 已实现模块

- 学生端：我的宿舍、报修申请、费用查询、公告浏览、访客预约、调宿申请、个人中心、AI智能问答、意见反馈
- 管理员端：控制台、楼栋管理、房间管理、学生管理、宿管员管理、费用管理、公告管理、调宿审核、个人中心
- 宿管员端：工作台、入住登记、报修处理、访客登记、卫生检查、晚归登记、物品出入、消息通知、个人中心

AI 智能问答已接入 DeepSeek API（deepseek-chat 模型），`application.properties` 中已配置 key。API 调用失败时自动降级为本地回复。首页实时天气通过 wttr.in 免费 API 获取福州市连江县天气数据。

## 智能宿舍分配算法

系统实现了基于多特征兼容性评分的两阶段贪心匹配算法：

1. **新生注册**：学生在登录页自主注册，填写性别、专业班级、作息习惯、是否抽烟、兴趣爱好等 10 项特征
2. **阶段一（同班优先）**：按专业班级分组，组内用兼容性评分两两配对，4/6人间逐间填满
3. **阶段二（跨班补位）**：收集空床位，贪心匹配最高兼容度的待分配学生
4. **调宿推荐**：计算与目标宿舍成员的兼容度，返回 Top-3 推荐

管理员在"智能分配"页面一键运行算法，查看分配结果矩阵和兼容度均分，确认后写入数据库。

**涉及文件**：`RoomAssignmentService.java`、`AssignmentController.java`、`SmartAssignment.vue`、`RegisterForm.vue`、`ProfileCompletionModal.vue`

## 样式说明

学生端已按视频截图重做：

- 白色左侧导航栏
- 顶部面包屑和头像区域
- 浅蓝色内容背景
- 服务台首页：欢迎语、宿舍安全横幅、公告（后端数据）、余额（后端数据）、报修（后端数据）、卫生评分（后端数据）、室友卡片
- 我的宿舍页：宿舍信息卡（用户数据）、室友列表、床位布局、设施运行状态、卫生月度指标
