# 项目改进建议

> 本文档列出了当前项目在安全性、架构、代码质量、论文配套等方面需要改进的地方，按优先级排序。
> 
> **状态说明**：✅ 已完成　⏳ 待完成

---

## P0 — 答辩前必须修复

### 1. ✅ 密码明文存储 — 已完成

**已完成内容**：
- `pom.xml` 新增 `spring-security-crypto` 依赖
- `UserService.login()` 改用 BCrypt 验证，支持自动升级旧明文密码
- `UserService.create()` 写入时 BCrypt 加密
- `DatabaseInitializer.seedUser()` 种子密码 BCrypt 加密
- `application.properties` 添加环境变量注释提示

### 2. ✅ 无服务端权限控制 — 已完成

**已完成内容**：
- 新增 `common/TokenStore.java` — ConcurrentHashMap 管理 token→user
- 新增 `interceptor/LoginInterceptor.java` — 拦截 `/api/**`，校验 `Authorization: Bearer <token>`
- 新增 `config/WebConfig.java` — 注册拦截器，排除 `/api/auth/login`
- `AuthController.login()` 返回 token，新增 `/api/auth/logout` 端点
- 前端 `api.js` 请求自动携带 token，401 自动登出
- 前端认证失败时直接报错，去除离线 fallback

### 3. ✅ chat_message 种子数据不幂等 — 已完成

**已完成内容**：
- `DatabaseInitializer.seedChatMessage()` 新增 COUNT 判断，每次启动前检查是否已存在

---

## P1 — 显著提升毕设质量

### 4. ✅ 添加单元测试 — 已完成

**已完成内容**：
- 新增 `src/test/java/.../controller/AuthControllerTest.java`
- 7 个测试用例：loginSuccess、loginFail、loginEmptyCredentials、recordsList、createRecord、deleteRecord、unauthorizedRequest
- 使用 `@WebMvcTest(controllers = {AuthController.class, RecordController.class})` + MockBean

### 5. ✅ 前端部分页面数据硬编码 — 已完成

**已完成内容**：
- `StudentDashboard.vue` 公告列表、报修件数、宿舍号、余额、卫生评分均改为 props 接收
- `MyDorm.vue` 宿舍名、床位、设施、评分均改为 props 接收
- `App.vue` 新增 `loadStudentDashboard()` 方法，从后端 API 拉取公告/报修/费用/卫生数据
- `App.vue` 新增 `myBedNumber`、`dormKeeperName`、`defaultFacilities` 等计算属性

### 6. ✅ 缺少分页 — 已完成

**已完成内容**：
- `RecordService.list()` 增加 page/size 参数，SQL 添加 `LIMIT ? OFFSET ?`，返回 `PageResult`
- `UserService.list()` 同步增加分页
- `RecordController` / `UserController` 增加 page/size 请求参数
- 前端 `api.js` 增加 page/size 参数
- 前端所有使用 `fetchRecords`/`fetchUsers` 的组件适配分页响应（`data.rows || data`）

### 7. ✅ 参数校验不完整 — 已完成

**已完成内容**：
- `AuthController.login()` 新增 username/password 空值校验

---

## P2 — 论文配套工作

### 8. ER 图

**需要画出**：3 张表（`sys_user`、`biz_record`、`chat_message`）的字段、类型、主键外键关系和业务含义。

推荐用 Mermaid 画，直接嵌入 Markdown：

```mermaid
erDiagram
    sys_user ||--o{ biz_record : "owner"
    sys_user ||--o{ chat_message : "from/to"
    sys_user {
        BIGINT id PK
        VARCHAR username UK
        VARCHAR password
        VARCHAR real_name
        VARCHAR role
        VARCHAR phone
        VARCHAR room_no
        TIMESTAMP created_at
    }
```

### 9. 系统架构图

需要包含：浏览器 → Vue 前端（Vite） → HTTP REST API → Spring Boot Controller → Service → JDBC → MySQL，以及 WebSocket 长连接通道。

### 10. 用例图 / 功能模块图

用三角色（学生/管理员/宿管）分别画用例图，每个角色 8-10 个用例。

### 11. 论文目录建议

```
第一章 绪论
  1.1 项目背景与意义
  1.2 国内外研究现状
  1.3 论文组织结构

第二章 相关技术概述
  2.1 Spring Boot 框架
  2.2 Vue.js 与 Vite
  2.3 MySQL 数据库
  2.4 WebSocket 实时通讯
  2.5 DeepSeek AI 集成

第三章 系统需求分析
  3.1 功能性需求（三角色用例图 + 用例描述）
  3.2 非功能性需求

第四章 系统设计
  4.1 系统架构设计（架构图）
  4.2 数据库设计（ER 图 + 表结构说明）
  4.3 接口设计（RESTful API 列表）
  4.4 模块划分（Controller-Service-DB 三层）

第五章 系统实现
  5.1 开发环境
  5.2 登录与权限模块
  5.3 宿舍管理核心模块
  5.4 即时通讯模块（WebSocket）
  5.5 AI 智能问答（DeepSeek）

第六章 系统测试
  6.1 单元测试
  6.2 功能测试用例

第七章 总结与展望
```

---

## P3 — 可选优化（时间允许再做）

### 15. ✅ 添加 .gitignore — 已完成

已添加 `.gitignore`，排除 `target/`、`node_modules/`、`*.log`、`.idea/`、`*.iml`、`dist/`、`.vscode/`、`*.class`、`*.jar`。

### 12. ⏳ 添加 Swagger 接口文档

引入 `springdoc-openapi`，自动生成 API 文档页面，方便演示和答辩。

```xml
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-ui</artifactId>
    <version>1.7.0</version>
</dependency>
```

### 13. 前端路由改为 Vue Router

当前用 v-if/v-else 做页面切换，前端路由已有 `router.js` 定义了菜单，但未使用 `vue-router` 做 URL 路由。接入 `vue-router` 后可以支持浏览器后退前进、URL 直链。

### 14. 日志切面

写一个 AOP 切面记录所有 Controller 方法调用的入参和耗时，展示时能体现"系统的可观测性"。

### 15. 添加 .gitignore

当前项目可能缺少 `.gitignore`，应排除 `target/`、`node_modules/`、`*.log`、`.idea/` 等。

建议的 `.gitignore`：
```
target/
node_modules/
*.log
.idea/
*.iml
dist/
```

---

## 改动文件清单汇总

| 优先级 | 改动 | 涉及文件 | 状态 |
|--------|------|----------|:----:|
| P0 | 密码加密 | `UserService.java`, `DatabaseInitializer.java`, `application.properties` | ✅ |
| P0 | 权限拦截 | 新增 `LoginInterceptor.java`, `WebConfig.java`, `TokenStore.java` | ✅ |
| P0 | 消息幂等 | `DatabaseInitializer.java` seedChatMessage | ✅ |
| P0 | 参数校验 | `AuthController.java` login 空值校验 | ✅ |
| P1 | 单元测试 | 新增 `AuthControllerTest.java` (7 tests) | ✅ |
| P1 | 数据对接 | `StudentDashboard.vue`, `MyDorm.vue`, `App.vue` | ✅ |
| P1 | 分页 | `RecordService.java`, `UserService.java`, `RecordController.java`, `UserController.java`, `api.js` | ✅ |
| P3 | .gitignore | `.gitignore` | ✅ |
| P2 | ER图/架构图 | 写文档（可内嵌在论文中） | ⏳ |
| P3 | Swagger | 引入 springdoc-openapi | ⏳ |
| P3 | Vue-Router | 前端路由改造 | ⏳ |
| P3 | AOP 日志 | 新增切面 | ⏳ |

---

## 优秀毕设评估 & 算法集成建议

### 项目当前亮点

与同类毕设相比，本项目已具备以下竞争优势：

| 维度 | 现状 | 竞争力 |
|------|------|:------:|
| 安全性 | BCrypt 加密 + Token 鉴权 + 拦截器 | ⭐⭐⭐ |
| 实时通讯 | WebSocket 双向推送 + 未读角标 | ⭐⭐⭐ |
| AI 集成 | DeepSeek 真实 API 调用（非模拟） | ⭐⭐⭐⭐ |
| 数据规模 | 100+ 条多类型种子数据，覆盖真实场景 | ⭐⭐⭐ |
| 分页查询 | LIMIT/OFFSET 完整实现 | ⭐⭐⭐ |
| 天气集成 | 第三方 API 实时数据 | ⭐⭐⭐ |
| 前后端分离 | Vue 3 + Vite + Spring Boot，独立部署 | ⭐⭐⭐ |
| 单元测试 | 7 个测试用例覆盖核心流程 | ⭐⭐ |

**结论**：当前项目在"系统实现"层面已经达到优秀毕设的门槛，但与真正获奖级别的毕设相比，缺少**算法层面的创新**。评委更看重"你做了什么别人不会做的事"。

---

### 核心问题：算法的切入点

目前项目是纯 CRUD + API 集成，论文中缺少算法描述章节。以下按**投入产出比**排序，推荐优先实现：

---

### 方案一：智能宿舍分配算法（推荐 ⭐⭐⭐⭐⭐）

**简介**：新生入学或调宿时，系统根据学生特征自动推荐最优宿舍分配方案。

**算法**：带权重的贪心匹配 + 匈牙利算法

**数据输入**：
- 学生特征向量：专业、年级、作息习惯（早睡/晚睡）、是否抽烟、兴趣爱好
- 宿舍约束：每间容量、已占用床位、楼层/朝向偏好

**实现方式**：
1. 新增 `sys_user` 字段：`sleep_time`（早/晚）、`smoking`（是/否）
2. 新增 `RoomAssignmentService`，计算学生间兼容性得分（余弦相似度）
3. 用贪心算法或匈牙利算法求解最优分配
4. 前端新增"智能分配"页面，展示分配结果及匹配度

**论文可写**：3.2 智能分配算法设计（问题建模 + 贪心算法 + 匈牙利算法对比）+ 5.X 算法实现与实验

---

### 方案二：卫生评分趋势预测（推荐 ⭐⭐⭐⭐）

**简介**：根据历史卫生检查数据，预测宿舍未来评分走势，提前预警高风险宿舍。

**算法**：线性回归 或 滑动平均

**数据输入**：`biz_record` 中 `category='hygiene'` 的历史评分数据

**实现方式**：
1. `HygienePredictionService` 读取某宿舍近 12 个月评分
2. 用最小二乘法拟合线性趋势
3. 预测下月评分，低于阈值（如 75 分）则标记为"重点关注"
4. 在宿管端工作台展示预测预警

**论文可写**：3.3 基于线性回归的卫生评分预测模型 + 6.2 预测准确率实验

---

### 方案三：报修文本自动分类（推荐 ⭐⭐⭐⭐）

**简介**：学生提交报修描述后，系统自动识别类别（电器/水管/门窗/网络），减少人工分类。

**算法**：TF-IDF + 朴素贝叶斯 或 关键词匹配

**数据输入**：`biz_record` 中 `category='repair'` 的 `content` 字段

**实现方式**：
1. 预定义类别关键词库（空调/水龙头/灯管/网络等）
2. 或引入轻量 NLP：Jieba 分词 + TF-IDF 向量化 + 朴素贝叶斯分类
3. `RepairClassifierService.classify(content)` → 自动填充 `title` 字段

**论文可写**：3.4 基于文本分类的报修智能分派 + 6.3 分类准确率评估

---

### 方案四：智能排班调度（推荐 ⭐⭐⭐）

**简介**：宿管员卫生检查、巡楼排班自动生成，避免冲突。

**算法**：回溯搜索 或 遗传算法

**论文可写**：3.5 基于遗传算法的宿管排班优化

---

### 方案五：室友匹配推荐（推荐 ⭐⭐⭐）

**简介**：学生申请调宿时，系统推荐兼容度最高的室友组合。

**算法**：协同过滤 或 KNN 相似度匹配

**论文可写**：3.6 基于用户画像的室友推荐算法

---

### 论文加分清单

| 加分项 | 当前状态 | 建议 |
|--------|:------:|------|
| ER 图 | ⏳ | 用 Mermaid 画，嵌入论文 4.2 节 |
| 系统架构图 | ⏳ | 画 Vue→API→Service→JDBC→MySQL + WebSocket 通道 |
| 用例图 | ⏳ | 三角色各 8-10 用例 |
| 时序图 | ❌ | 选一个核心流程画（如登录鉴权、WebSocket 推送） |
| 算法伪代码 | ❌ | 从方案一/二/三中选一个写 |
| 测试覆盖率 | 7 tests | 补到 15+ 个用例，增加边界测试 |
| Swagger 文档 | ⏳ | 引入 springdoc-openapi |
| 压力测试 | ❌ | 用 JMeter 跑一个简单压测，写进 6.2 节 |

---

### 建议优先级路径

如果时间充足（> 2 周），按这个顺序来：

1. **P2 文档**：ER 图 + 架构图 + 用例图（1 天）
2. **Swagger**（半天）
3. **方案一 智能分配**：投入最高，论文产出最丰富（3-5 天）
4. **方案二 评分预测** 或 **方案三 报修分类**（2-3 天）
5. **时序图 + 测试扩充**（1 天）

完成以上后，论文可以从纯 CRUD 系统升级为"含智能算法的宿舍管理系统"，评优概率大幅提升。
