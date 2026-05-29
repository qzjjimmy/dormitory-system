# 智能宿舍分配算法 实现计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task.

**Goal:** 为宿舍管理系统增加学生注册、特征收集、智能宿舍分配算法三大功能
**Architecture:** 在现有 Spring Boot + Vue 3 架构上扩展：后端新增 `RoomAssignmentService`（贪心匹配算法）+ `AssignmentController`，前端新增注册页/特征补全弹窗/智能分配页三个组件
**Tech Stack:** Java 8+, Spring Boot, JDBC Template, Vue 3 (Options API), BCrypt

---

## Phase 1: 数据库 & 模型层

### Task 1.1 — 扩展 sys_user 表结构

**文件:** `C:\DormMS\dormitory-system\src\main\java\com\bishe\dormitory\config\DatabaseInitializer.java`

**步骤 1.1a — 添加 ALTER TABLE 列**

在 `run()` 方法中，`CREATE TABLE IF NOT EXISTS sys_user` 语句之后、`seedUser` 调用之前，添加 10 个列的迁移逻辑：

```java
// -- Migration: add feature columns for smart dorm assignment --
addColumnIfNotExists("ALTER TABLE sys_user ADD COLUMN gender VARCHAR(10)");
addColumnIfNotExists("ALTER TABLE sys_user ADD COLUMN major_class VARCHAR(80)");
addColumnIfNotExists("ALTER TABLE sys_user ADD COLUMN sleep_habit VARCHAR(10)");
addColumnIfNotExists("ALTER TABLE sys_user ADD COLUMN smoking VARCHAR(10)");
addColumnIfNotExists("ALTER TABLE sys_user ADD COLUMN hobbies VARCHAR(200)");
addColumnIfNotExists("ALTER TABLE sys_user ADD COLUMN cleanliness VARCHAR(10)");
addColumnIfNotExists("ALTER TABLE sys_user ADD COLUMN gaming VARCHAR(10)");
addColumnIfNotExists("ALTER TABLE sys_user ADD COLUMN snoring VARCHAR(10)");
addColumnIfNotExists("ALTER TABLE sys_user ADD COLUMN return_time VARCHAR(10)");
addColumnIfNotExists("ALTER TABLE sys_user ADD COLUMN noise_tolerance VARCHAR(10)");
```

**步骤 1.1b — 添加辅助方法**

在 `DatabaseInitializer` 类末尾添加：

```java
private void addColumnIfNotExists(String sql) {
    try {
        jdbcTemplate.execute(sql);
    } catch (Exception e) {
        // Column already exists — safe to ignore
    }
}
```

**验证:** 启动应用，检查日志无异常，数据库表包含新列。

---

### Task 1.2 — 更新 SysUser 模型类

**文件:** `C:\DormMS\dormitory-system\src\main\java\com\bishe\dormitory\model\SysUser.java`

在 `roomNo` 字段之后、`createdAt` 之前，添加 10 个新字段及其 getter/setter：

```java
private String gender;
private String majorClass;
private String sleepHabit;
private String smoking;
private String hobbies;
private String cleanliness;
private String gaming;
private String snoring;
private String returnTime;
private String noiseTolerance;

// Getters
public String getGender() { return gender; }
public void setGender(String gender) { this.gender = gender; }
public String getMajorClass() { return majorClass; }
public void setMajorClass(String majorClass) { this.majorClass = majorClass; }
public String getSleepHabit() { return sleepHabit; }
public void setSleepHabit(String sleepHabit) { this.sleepHabit = sleepHabit; }
public String getSmoking() { return smoking; }
public void setSmoking(String smoking) { this.smoking = smoking; }
public String getHobbies() { return hobbies; }
public void setHobbies(String hobbies) { this.hobbies = hobbies; }
public String getCleanliness() { return cleanliness; }
public void setCleanliness(String cleanliness) { this.cleanliness = cleanliness; }
public String getGaming() { return gaming; }
public void setGaming(String gaming) { this.gaming = gaming; }
public String getSnoring() { return snoring; }
public void setSnoring(String snoring) { this.snoring = snoring; }
public String getReturnTime() { return returnTime; }
public void setReturnTime(String returnTime) { this.returnTime = returnTime; }
public String getNoiseTolerance() { return noiseTolerance; }
public void setNoiseTolerance(String noiseTolerance) { this.noiseTolerance = noiseTolerance; }
```

也要更新带参构造器支持新字段（可选，但为了方便后续使用建议添加）。

**验证:** 编译通过，无报错。

---

### Task 1.3 — 种子用户特征数据

**文件:** `C:\DormMS\dormitory-system\src\main\java\com\bishe\dormitory\config\DatabaseInitializer.java`

在 `run()` 方法末尾、`seedChatMessage` 调用之后，添加种子用户的特征 UPDATE：

```java
// -- Seed user feature data for smart assignment demo --
jdbcTemplate.update("UPDATE sys_user SET gender='男',major_class='智能科学与技术2025级',sleep_habit='晚睡',smoking='否',hobbies='编程,游戏,电影',cleanliness='整洁',gaming='是',snoring='否',return_time='晚归',noise_tolerance='正常' WHERE username='student'");
jdbcTemplate.update("UPDATE sys_user SET gender='男',major_class='智能科学与技术2025级',sleep_habit='早睡',smoking='否',hobbies='篮球,音乐,阅读',cleanliness='整洁',gaming='是',snoring='否',return_time='早归',noise_tolerance='安静' WHERE username='linruhai'");
jdbcTemplate.update("UPDATE sys_user SET gender='男',major_class='智能科学与技术2025级',sleep_habit='晚睡',smoking='否',hobbies='游戏,电影,健身',cleanliness='一般',gaming='是',snoring='是',return_time='晚归',noise_tolerance='热闹' WHERE username='qinxingrui'");
jdbcTemplate.update("UPDATE sys_user SET gender='男',major_class='智能科学与技术2025级',sleep_habit='早睡',smoking='否',hobbies='阅读,编程,音乐',cleanliness='整洁',gaming='是',snoring='否',return_time='正常',noise_tolerance='安静' WHERE username='chenjiahe'");
```

**验证:** 启动应用，用 student/linruhai 等账号登录，确认 profileComplete 生效。

---

## Phase 2: 后端 — 注册 & 特征接口

### Task 2.1 — 扩展 UserService.create() 和 login()

**文件:** `C:\DormMS\dormitory-system\src\main\java\com\bishe\dormitory\service\UserService.java`

**步骤 2.1a — 扩展 create() 方法**

将现有的 `create()` 方法替换为支持特征字段的版本：

```java
public void create(Map<String, String> user) {
    String rawPassword = user.getOrDefault("password", "123456");
    jdbcTemplate.update(
        "INSERT INTO sys_user(username,password,real_name,role,phone,room_no,gender,major_class,sleep_habit,smoking,hobbies,cleanliness,gaming,snoring,return_time,noise_tolerance) " +
        "VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
        user.get("username"), passwordEncoder.encode(rawPassword), user.get("realName"),
        user.getOrDefault("role", "student"), user.get("phone"), user.get("roomNo"),
        user.get("gender"), user.get("majorClass"), user.get("sleepHabit"),
        user.get("smoking"), user.get("hobbies"), user.get("cleanliness"),
        user.get("gaming"), user.get("snoring"), user.get("returnTime"),
        user.get("noiseTolerance"));
}
```

**步骤 2.1b — 修改 login() 返回值，增加 profileComplete 字段**

在 `login()` 方法的 `user.remove("password");` 之前，添加：

```java
// Check profile completeness for students
String role = (String) user.get("role");
boolean profileComplete = true;
if ("student".equals(role)) {
    profileComplete = user.get("gender") != null && user.get("sleep_habit") != null
        && user.get("smoking") != null && user.get("hobbies") != null
        && user.get("cleanliness") != null && user.get("gaming") != null
        && user.get("snoring") != null && user.get("return_time") != null
        && user.get("noise_tolerance") != null && user.get("major_class") != null;
}
user.put("profileComplete", profileComplete);
```

**步骤 2.1c — 修改 login() 的 SELECT 查询，加入特征字段**

将 login() 中的 SELECT 语句改为：

```java
List<Map<String, Object>> users = jdbcTemplate.queryForList(
    "SELECT id,username,password,real_name AS realName,role,phone,room_no AS roomNo," +
    "gender,major_class AS majorClass,sleep_habit AS sleepHabit,smoking,hobbies," +
    "cleanliness,gaming,snoring,return_time AS returnTime,noise_tolerance AS noiseTolerance " +
    "FROM sys_user WHERE username=?", username);
```

**验证:** student 登录应返回 `profileComplete: true`，新注册未填特征的用户应返回 `false`。

---

### Task 2.2 — 添加 PUT /api/user/profile 接口

**文件:** `C:\DormMS\dormitory-system\src\main\java\com\bishe\dormitory\service\UserService.java`

添加新方法：

```java
public void updateProfile(Long userId, Map<String, String> profile) {
    jdbcTemplate.update(
        "UPDATE sys_user SET gender=?,major_class=?,sleep_habit=?,smoking=?,hobbies=?," +
        "cleanliness=?,gaming=?,snoring=?,return_time=?,noise_tolerance=? WHERE id=?",
        profile.get("gender"), profile.get("majorClass"), profile.get("sleepHabit"),
        profile.get("smoking"), profile.get("hobbies"), profile.get("cleanliness"),
        profile.get("gaming"), profile.get("snoring"), profile.get("returnTime"),
        profile.get("noiseTolerance"), userId);
}
```

**文件:** `C:\DormMS\dormitory-system\src\main\java\com\bishe\dormitory\controller\AuthController.java`

在 `logout()` 方法之后添加：

```java
@PostMapping("/register")
public ApiResponse<Map<String, Object>> register(@RequestBody Map<String, String> request) {
    String username = request.get("username");
    String password = request.get("password");
    String realName = request.get("realName");
    if (username == null || username.trim().isEmpty()
            || password == null || password.trim().isEmpty()
            || realName == null || realName.trim().isEmpty()) {
        return ApiResponse.fail("用户名、密码、姓名不能为空");
    }
    request.put("role", "student");
    request.put("roomNo", null);
    userService.create(request);
    // Auto-login after registration
    Map<String, Object> user = userService.login(username, password);
    String token = tokenStore.createToken(user);
    Map<String, Object> result = new HashMap<>(user);
    result.put("token", token);
    return ApiResponse.ok(result);
}
```

**文件:** `C:\DormMS\dormitory-system\src\main\java\com\bishe\dormitory\controller\UserController.java`

添加 profile 更新端点：

```java
@PutMapping("/profile")
public ApiResponse<String> updateProfile(@RequestBody Map<String, String> profile, HttpServletRequest request) {
    Map<String, Object> currentUser = (Map<String, Object>) request.getAttribute("user");
    String role = (String) currentUser.get("role");
    if (!"student".equals(role)) {
        return ApiResponse.fail("仅学生可更新个人特征");
    }
    Long userId = ((Number) currentUser.get("id")).longValue();
    userService.updateProfile(userId, profile);
    return ApiResponse.ok("特征更新成功");
}
```

注意：`UserController` 需要使用 `HttpServletRequest`，需从 `LoginInterceptor` 的 `request.setAttribute("user", user)` 中获取。检查 `LoginInterceptor` 确认 `user` 属性名。

**验证:** 
1. `POST /api/auth/register` 注册新用户，检查返回含 token
2. `PUT /api/user/profile` 更新特征，检查数据库更新

---

### Task 2.3 — 注册接口用户名唯一校验

**文件:** `C:\DormMS\dormitory-system\src\main\java\com\bishe\dormitory\service\UserService.java`

在 `create()` 方法开头添加重复检查：

```java
public void create(Map<String, String> user) {
    Integer count = jdbcTemplate.queryForObject(
        "SELECT COUNT(*) FROM sys_user WHERE username=?", Integer.class, user.get("username"));
    if (count != null && count > 0) {
        throw new IllegalArgumentException("用户名已存在");
    }
    // ... 原有插入逻辑 ...
}
```

**验证:** 用已存在的用户名注册，应返回"用户名已存在"。

---

## Phase 3: 智能分配算法

### Task 3.1 — 创建 RoomAssignmentService

**文件:** `C:\DormMS\dormitory-system\src\main\java\com\bishe\dormitory\service\RoomAssignmentService.java`（新建）

```java
package com.bishe.dormitory.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class RoomAssignmentService {
    private final JdbcTemplate jdbcTemplate;

    public RoomAssignmentService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // -- Student DTO for assignment --
    public static class StudentProfile {
        public Long id;
        public String realName;
        public String gender;
        public String majorClass;
        public String sleepHabit;
        public String smoking;
        public List<String> hobbies = new ArrayList<>();
        public String cleanliness;
        public String gaming;
        public String snoring;
        public String returnTime;
        public String noiseTolerance;

        static StudentProfile fromMap(Map<String, Object> row) {
            StudentProfile s = new StudentProfile();
            s.id = ((Number) row.get("id")).longValue();
            s.realName = (String) row.get("realName");
            s.gender = (String) row.get("gender");
            s.majorClass = (String) row.get("majorClass");
            s.sleepHabit = (String) row.get("sleepHabit");
            s.smoking = (String) row.get("smoking");
            String h = (String) row.get("hobbies");
            if (h != null && !h.isEmpty()) {
                s.hobbies = Arrays.asList(h.split(","));
            }
            s.cleanliness = (String) row.get("cleanliness");
            s.gaming = (String) row.get("gaming");
            s.snoring = (String) row.get("snoring");
            s.returnTime = (String) row.get("returnTime");
            s.noiseTolerance = (String) row.get("noiseTolerance");
            return s;
        }
    }

    // -- Room DTO --
    public static class RoomSlot {
        public String roomNo;
        public List<StudentProfile> members = new ArrayList<>();
        public int capacity = 4;

        public int available() { return capacity - members.size(); }
    }

    // -- Compatibility score between two students (0-100) --
    public int compatibilityScore(StudentProfile a, StudentProfile b) {
        // Hard constraint: gender must match
        if (a.gender == null || b.gender == null || !a.gender.equals(b.gender)) {
            return 0;
        }
        int score = 0;

        // Sleep habit (weight 25)
        if (nullSafeEquals(a.sleepHabit, b.sleepHabit)) score += 25;
        // Smoking (weight 20)
        if (nullSafeEquals(a.smoking, b.smoking)) score += 20;
        // Cleanliness (weight 15) — 3 levels
        score += levelScore(a.cleanliness, b.cleanliness, 15, 7.5);
        // Gaming (weight 10)
        if (nullSafeEquals(a.gaming, b.gaming)) score += 10;
        // Snoring (weight 10)
        if (nullSafeEquals(a.snoring, b.snoring)) score += 10;
        // Return time (weight 10) — 3 levels
        score += levelScore(a.returnTime, b.returnTime, 10, 5);
        // Noise tolerance (weight 5) — 3 levels
        score += levelScore(a.noiseTolerance, b.noiseTolerance, 5, 2.5);
        // Hobbies (weight 5) — shared ratio
        score += hobbyScore(a.hobbies, b.hobbies);

        return score;
    }

    private boolean nullSafeEquals(String a, String b) {
        if (a == null && b == null) return true;
        if (a == null || b == null) return false;
        return a.equals(b);
    }

    private int levelScore(String a, String b, int max, double step) {
        if (nullSafeEquals(a, b)) return max;
        if (a == null || b == null) return 0;
        // 整洁/一般/随意 or 早归/正常/晚归 or 安静/正常/热闹
        Map<String, Integer> order = new LinkedHashMap<>();
        // We'll use a simple approach: same=full, adjacent=half, far=0
        List<List<String>> levels = Arrays.asList(
            Arrays.asList("整洁", "一般", "随意"),
            Arrays.asList("早归", "正常", "晚归"),
            Arrays.asList("安静", "正常", "热闹")
        );
        for (List<String> lvl : levels) {
            int ia = lvl.indexOf(a);
            int ib = lvl.indexOf(b);
            if (ia >= 0 && ib >= 0) {
                int diff = Math.abs(ia - ib);
                if (diff == 0) return max;
                if (diff == 1) return (int)(max - step);
                return 0;
            }
        }
        return nullSafeEquals(a, b) ? max : 0;
    }

    private int hobbyScore(List<String> a, List<String> b) {
        if (a.isEmpty() || b.isEmpty()) return 0;
        Set<String> setA = new HashSet<>(a);
        Set<String> setB = new HashSet<>(b);
        Set<String> union = new HashSet<>(setA);
        union.addAll(setB);
        if (union.isEmpty()) return 0;
        int shared = 0;
        for (String ha : setA) {
            if (setB.contains(ha)) shared++;
        }
        return (int) Math.round(5.0 * shared / union.size());
    }

    // -- Phase 1: Same-class assignment --
    public List<RoomSlot> assignByClass(List<StudentProfile> students) {
        List<RoomSlot> allRooms = new ArrayList<>();
        Map<String, List<StudentProfile>> groups = new LinkedHashMap<>();
        for (StudentProfile s : students) {
            String key = s.majorClass != null ? s.majorClass : "未分班";
            groups.computeIfAbsent(key, k -> new ArrayList<>()).add(s);
        }

        int roomSeq = 1;
        for (Map.Entry<String, List<StudentProfile>> entry : groups.entrySet()) {
            List<StudentProfile> group = entry.getValue();
            // Sort by a simple heuristic: separate smokers
            group.sort((a, b) -> {
                if (!nullSafeEquals(a.sleepHabit, b.sleepHabit)) return a.sleepHabit.compareTo(b.sleepHabit);
                return a.realName.compareTo(b.realName);
            });

            while (!group.isEmpty()) {
                RoomSlot room = new RoomSlot();
                room.roomNo = "芙蓉楼3 · " + (500 + roomSeq) + "室";
                roomSeq++;

                // Pick the first student as anchor
                StudentProfile anchor = group.remove(0);
                room.members.add(anchor);

                // Fill remaining 3 beds with best matches from the same class
                for (int bed = 1; bed < 4 && !group.isEmpty(); bed++) {
                    int bestIdx = -1;
                    int bestScore = -1;
                    for (int i = 0; i < group.size(); i++) {
                        int totalScore = 0;
                        for (StudentProfile m : room.members) {
                            totalScore += compatibilityScore(group.get(i), m);
                        }
                        int avgScore = room.members.isEmpty() ? 0 : totalScore / room.members.size();
                        if (avgScore > bestScore) {
                            bestScore = avgScore;
                            bestIdx = i;
                        }
                    }
                    if (bestIdx >= 0) {
                        room.members.add(group.remove(bestIdx));
                    }
                }
                allRooms.add(room);
            }
        }
        return allRooms;
    }

    // -- Phase 2: Cross-class filling --
    public List<RoomSlot> fillRemaining(List<RoomSlot> rooms, List<StudentProfile> unassigned) {
        List<StudentProfile> remaining = new ArrayList<>(unassigned);
        for (RoomSlot room : rooms) {
            while (room.available() > 0 && !remaining.isEmpty()) {
                int bestIdx = -1;
                int bestScore = -1;
                for (int i = 0; i < remaining.size(); i++) {
                    int totalScore = 0;
                    for (StudentProfile m : room.members) {
                        totalScore += compatibilityScore(remaining.get(i), m);
                    }
                    int avgScore = room.members.isEmpty() ? 50 : totalScore / room.members.size();
                    if (avgScore > bestScore) {
                        bestScore = avgScore;
                        bestIdx = i;
                    }
                }
                if (bestIdx >= 0 && bestScore > 0) {
                    room.members.add(remaining.remove(bestIdx));
                } else {
                    break;
                }
            }
        }
        return rooms;
    }

    // -- Full pipeline: execute both phases --
    public Map<String, Object> executeAssignment() {
        // Fetch all unassigned students (room_no is NULL) with complete profiles
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
            "SELECT id,real_name AS realName,gender,major_class AS majorClass," +
            "sleep_habit AS sleepHabit,smoking,hobbies,cleanliness,gaming,snoring," +
            "return_time AS returnTime,noise_tolerance AS noiseTolerance " +
            "FROM sys_user WHERE role='student' AND room_no IS NULL");

        List<StudentProfile> students = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            students.add(StudentProfile.fromMap(row));
        }

        if (students.isEmpty()) {
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("success", true);
            result.put("message", "没有待分配的学生");
            result.put("rooms", Collections.emptyList());
            result.put("unassigned", Collections.emptyList());
            return result;
        }

        // Phase 1: same-class assignment
        List<RoomSlot> rooms = assignByClass(students);
        // Collect remaining unassigned from phase 1
        List<StudentProfile> stillUnassigned = new ArrayList<>();
        for (StudentProfile s : students) {
            boolean found = false;
            for (RoomSlot r : rooms) {
                for (StudentProfile m : r.members) {
                    if (m.id.equals(s.id)) { found = true; break; }
                }
                if (found) break;
            }
            if (!found) stillUnassigned.add(s);
        }

        // Phase 2: cross-class filling
        if (!stillUnassigned.isEmpty()) {
            fillRemaining(rooms, stillUnassigned);
        }

        // Collect final unassigned
        List<StudentProfile> finalUnassigned = new ArrayList<>();
        for (StudentProfile s : students) {
            boolean found = false;
            for (RoomSlot r : rooms) {
                for (StudentProfile m : r.members) {
                    if (m.id.equals(s.id)) { found = true; break; }
                }
                if (found) break;
            }
            if (!found) finalUnassigned.add(s);
        }

        // Build response
        List<Map<String, Object>> roomList = new ArrayList<>();
        for (RoomSlot r : rooms) {
            Map<String, Object> rm = new LinkedHashMap<>();
            rm.put("roomNo", r.roomNo);
            List<Map<String, Object>> memberList = new ArrayList<>();
            int totalScore = 0;
            int pairCount = 0;
            for (StudentProfile m : r.members) {
                Map<String, Object> mb = new LinkedHashMap<>();
                mb.put("id", m.id);
                mb.put("realName", m.realName);
                mb.put("majorClass", m.majorClass);
                mb.put("sleepHabit", m.sleepHabit);
                mb.put("smoking", m.smoking);
                mb.put("cleanliness", m.cleanliness);
                mb.put("gaming", m.gaming);
                memberList.add(mb);
                // Calculate pairwise scores for avg
                for (StudentProfile other : r.members) {
                    if (!m.id.equals(other.id)) {
                        totalScore += compatibilityScore(m, other);
                        pairCount++;
                    }
                }
            }
            rm.put("members", memberList);
            rm.put("avgCompatibility", pairCount > 0 ? totalScore / pairCount : 100);
            roomList.add(rm);
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("success", true);
        result.put("rooms", roomList);
        result.put("unassignedCount", finalUnassigned.size());
        return result;
    }

    // -- Transfer recommendation: find top-3 rooms for a student --
    public List<Map<String, Object>> recommendTransfer(Long studentId) {
        Map<String, Object> studentRow = jdbcTemplate.queryForMap(
            "SELECT id,real_name AS realName,gender,major_class AS majorClass," +
            "sleep_habit AS sleepHabit,smoking,hobbies,cleanliness,gaming,snoring," +
            "return_time AS returnTime,noise_tolerance AS noiseTolerance " +
            "FROM sys_user WHERE id=?", studentId);

        StudentProfile student = StudentProfile.fromMap(studentRow);

        // Find all rooms with available beds (room_no not null, group by room_no prefix)
        List<Map<String, Object>> allRooms = jdbcTemplate.queryForList(
            "SELECT id,real_name AS realName,room_no AS roomNo,gender,major_class AS majorClass," +
            "sleep_habit AS sleepHabit,smoking,hobbies,cleanliness,gaming,snoring," +
            "return_time AS returnTime,noise_tolerance AS noiseTolerance " +
            "FROM sys_user WHERE role='student' AND room_no IS NOT NULL");

        // Group by room prefix (e.g., "芙蓉楼3 · 519室")
        Map<String, List<Map<String, Object>>> roomGroups = new LinkedHashMap<>();
        for (Map<String, Object> row : allRooms) {
            String roomNo = (String) row.get("roomNo");
            if (roomNo == null) continue;
            // Extract building + room number prefix (before bed info)
            String prefix = roomNo.replaceAll(" · \\d+号床.*$", "");
            roomGroups.computeIfAbsent(prefix, k -> new ArrayList<>()).add(row);
        }

        // Score each room (only those with < 4 members)
        List<Map<String, Object>> recommendations = new ArrayList<>();
        for (Map.Entry<String, List<Map<String, Object>>> entry : roomGroups.entrySet()) {
            if (entry.getValue().size() >= 4) continue; // full

            int totalScore = 0;
            int count = 0;
            boolean genderMismatch = false;
            for (Map<String, Object> row : entry.getValue()) {
                String theirGender = (String) row.get("gender");
                if (theirGender != null && !theirGender.equals(student.gender)) {
                    genderMismatch = true;
                    break;
                }
                StudentProfile other = StudentProfile.fromMap(row);
                totalScore += compatibilityScore(student, other);
                count++;
            }
            if (genderMismatch) continue;

            int avgScore = count > 0 ? totalScore / count : 50;

            Map<String, Object> rec = new LinkedHashMap<>();
            rec.put("roomNo", entry.getKey());
            rec.put("currentCount", entry.getValue().size());
            rec.put("compatibility", avgScore);
            // Reason string
            StringBuilder reason = new StringBuilder();
            if (count == 0) {
                reason.append("空宿舍");
            } else {
                // Simple reason based on most matching traits
                List<String> matches = new ArrayList<>();
                boolean allSameSleep = true, allSameSmoke = true;
                for (Map<String, Object> row : entry.getValue()) {
                    if (!nullSafeEquals(student.sleepHabit, (String) row.get("sleepHabit"))) allSameSleep = false;
                    if (!nullSafeEquals(student.smoking, (String) row.get("smoking"))) allSameSmoke = false;
                }
                if (allSameSleep) matches.add("作息一致");
                if (allSameSmoke) matches.add("同为不抽烟");
                reason.append(String.join("、", matches));
                if (reason.length() == 0) reason.append("部分兼容");
            }
            rec.put("reason", reason.toString());
            recommendations.add(rec);
        }

        // Sort by compatibility desc, take top 3
        recommendations.sort((a, b) -> ((Integer) b.get("compatibility")).compareTo((Integer) a.get("compatibility")));
        return recommendations.size() > 3 ? recommendations.subList(0, 3) : recommendations;
    }

    // -- Confirm assignment: update room_no for students --
    public void confirmAssignment(List<Map<String, Object>> assignments) {
        for (Map<String, Object> a : assignments) {
            Long studentId = ((Number) a.get("studentId")).longValue();
            String roomNo = (String) a.get("roomNo");
            String bedNo = (String) a.get("bedNo");
            jdbcTemplate.update("UPDATE sys_user SET room_no=? WHERE id=?", roomNo + " · " + bedNo, studentId);
        }
    }
}
```

**验证:** 编译通过，无错误。

---

### Task 3.2 — 创建 AssignmentController

**文件:** `C:\DormMS\dormitory-system\src\main\java\com\bishe\dormitory\controller\AssignmentController.java`（新建）

```java
package com.bishe.dormitory.controller;

import com.bishe.dormitory.common.ApiResponse;
import com.bishe.dormitory.service.RoomAssignmentService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/assignment")
public class AssignmentController {
    private final RoomAssignmentService roomAssignmentService;

    public AssignmentController(RoomAssignmentService roomAssignmentService) {
        this.roomAssignmentService = roomAssignmentService;
    }

    // Execute batch assignment
    @PostMapping("/run")
    public ApiResponse<Map<String, Object>> runAssignment() {
        Map<String, Object> result = roomAssignmentService.executeAssignment();
        return ApiResponse.ok(result);
    }

    // Get transfer recommendations for a student
    @GetMapping("/recommend/{studentId}")
    public ApiResponse<?> recommendTransfer(@PathVariable Long studentId) {
        return ApiResponse.ok(roomAssignmentService.recommendTransfer(studentId));
    }

    // Confirm assignment (save to DB)
    @PostMapping("/confirm")
    public ApiResponse<String> confirmAssignment(@RequestBody Map<String, Object> body) {
        @SuppressWarnings("unchecked")
        java.util.List<Map<String, Object>> assignments = (java.util.List<Map<String, Object>>) body.get("assignments");
        roomAssignmentService.confirmAssignment(assignments);
        return ApiResponse.ok("分配已确认");
    }
}
```

**验证:** 编译通过。

---

## Phase 4: 前端 — API 层

### Task 4.1 — 扩展 api.js

**文件:** `C:\DormMS\dormitory-system\frontend\src\api.js`

在文件末尾 `export function fetchWeather()` 之前添加：

```javascript
// -- Registration --
export function register(profile) {
  return request('/auth/register', {
    method: 'POST',
    body: JSON.stringify(profile)
  })
}

// -- Profile update --
export function updateProfile(profile) {
  return request('/user/profile', {
    method: 'PUT',
    body: JSON.stringify(profile)
  })
}

// -- Smart Assignment --
export function runAssignment() {
  return request('/assignment/run', { method: 'POST' })
}

export function recommendTransfer(studentId) {
  return request(`/assignment/recommend/${studentId}`)
}

export function confirmAssignment(assignments) {
  return request('/assignment/confirm', {
    method: 'POST',
    body: JSON.stringify({ assignments })
  })
}
```

**验证:** 无语法错误。

---

## Phase 5: 前端 — 注册页面

### Task 5.1 — 创建 RegisterForm.vue

**文件:** `C:\DormMS\dormitory-system\frontend\src\components\RegisterForm.vue`（新建）

```html
<template>
  <div class="register-card">
    <div class="login-logo">
      <div class="app-icon">⌂</div>
      <div>
        <h1>新生注册</h1>
        <p>填写信息完成注册 · 第 {{ step }} / 2 步</p>
      </div>
    </div>

    <!-- Step 1: Account info -->
    <template v-if="step === 1">
      <div class="field">
        <label>用户名 <span class="required">*</span></label>
        <input v-model="form.username" placeholder="设置登录用户名">
      </div>
      <div class="field">
        <label>密码 <span class="required">*</span></label>
        <input v-model="form.password" type="password" placeholder="设置密码">
      </div>
      <div class="field">
        <label>确认密码 <span class="required">*</span></label>
        <input v-model="form.confirmPassword" type="password" placeholder="再次输入密码">
      </div>
      <div class="field">
        <label>真实姓名 <span class="required">*</span></label>
        <input v-model="form.realName" placeholder="请输入真实姓名">
      </div>
      <div class="field">
        <label>手机号</label>
        <input v-model="form.phone" placeholder="选填">
      </div>
      <button class="blue-btn block" @click="nextStep">下一步 →</button>
    </template>

    <!-- Step 2: Features -->
    <template v-if="step === 2">
      <div class="field">
        <label>性别 <span class="required">*</span></label>
        <div class="radio-group">
          <label class="radio-label"><input type="radio" v-model="form.gender" value="男"> 男</label>
          <label class="radio-label"><input type="radio" v-model="form.gender" value="女"> 女</label>
        </div>
      </div>
      <div class="field">
        <label>专业班级 <span class="required">*</span></label>
        <input v-model="form.majorClass" placeholder="如：智能科学与技术2025级">
      </div>
      <div class="field">
        <label>作息习惯 <span class="required">*</span></label>
        <div class="radio-group">
          <label class="radio-label"><input type="radio" v-model="form.sleepHabit" value="早睡"> 早睡</label>
          <label class="radio-label"><input type="radio" v-model="form.sleepHabit" value="晚睡"> 晚睡</label>
        </div>
      </div>
      <div class="field">
        <label>是否抽烟 <span class="required">*</span></label>
        <div class="radio-group">
          <label class="radio-label"><input type="radio" v-model="form.smoking" value="否"> 否</label>
          <label class="radio-label"><input type="radio" v-model="form.smoking" value="是"> 是</label>
        </div>
      </div>
      <div class="field">
        <label>兴趣爱好</label>
        <div class="tag-group">
          <span v-for="h in hobbyOptions" :key="h"
                :class="['tag', { active: form.hobbies.includes(h) }]"
                @click="toggleHobby(h)">{{ h }}</span>
        </div>
      </div>
      <div class="field">
        <label>卫生习惯 <span class="required">*</span></label>
        <div class="radio-group">
          <label class="radio-label"><input type="radio" v-model="form.cleanliness" value="整洁"> 整洁</label>
          <label class="radio-label"><input type="radio" v-model="form.cleanliness" value="一般"> 一般</label>
          <label class="radio-label"><input type="radio" v-model="form.cleanliness" value="随意"> 随意</label>
        </div>
      </div>
      <div class="field">
        <label>是否打游戏 <span class="required">*</span></label>
        <div class="radio-group">
          <label class="radio-label"><input type="radio" v-model="form.gaming" value="是"> 是</label>
          <label class="radio-label"><input type="radio" v-model="form.gaming" value="否"> 否</label>
        </div>
      </div>
      <div class="field">
        <label>是否打鼾 <span class="required">*</span></label>
        <div class="radio-group">
          <label class="radio-label"><input type="radio" v-model="form.snoring" value="否"> 否</label>
          <label class="radio-label"><input type="radio" v-model="form.snoring" value="是"> 是</label>
        </div>
      </div>
      <div class="field">
        <label>作息返回时间 <span class="required">*</span></label>
        <div class="radio-group">
          <label class="radio-label"><input type="radio" v-model="form.returnTime" value="早归"> 早归</label>
          <label class="radio-label"><input type="radio" v-model="form.returnTime" value="正常"> 正常</label>
          <label class="radio-label"><input type="radio" v-model="form.returnTime" value="晚归"> 晚归</label>
        </div>
      </div>
      <div class="field">
        <label>噪音容忍度 <span class="required">*</span></label>
        <div class="radio-group">
          <label class="radio-label"><input type="radio" v-model="form.noiseTolerance" value="安静"> 安静</label>
          <label class="radio-label"><input type="radio" v-model="form.noiseTolerance" value="正常"> 正常</label>
          <label class="radio-label"><input type="radio" v-model="form.noiseTolerance" value="热闹"> 热闹</label>
        </div>
      </div>
      <div class="step-buttons">
        <button class="outline-btn" @click="step = 1">← 上一步</button>
        <button class="blue-btn" @click="submitRegister">完成注册</button>
      </div>
    </template>

    <p class="error">{{ errorMessage }}</p>
    <p class="back-link"><a href="#" @click.prevent="$emit('back')">← 返回登录</a></p>
  </div>
</template>

<script>
import { register } from '../api.js'

const HOBBY_OPTIONS = ['篮球','足球','羽毛球','跑步','游泳','健身','编程','阅读','音乐','电影','游戏','摄影','绘画','旅行','美食']

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
        returnTime: '正常', noiseTolerance: '正常'
      }
    }
  },
  methods: {
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
          noiseTolerance: this.form.noiseTolerance
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
```

**验证:** 注册流程两页切换正常，提交调用 API。

---

### Task 5.2 — 修改 App.vue 登录页，集成注册入口

**文件:** `C:\DormMS\dormitory-system\frontend\src\App.vue`

**步骤 5.2a — 导入 RegisterForm**

在 `<script>` 顶部 import 部分添加：

```javascript
import RegisterForm from './components/RegisterForm.vue'
```

在 `components` 注册中加上 `RegisterForm`。

**步骤 5.2b — data 中添加 showRegister 状态**

```javascript
data() {
  return {
    // ... existing ...
    showRegister: false,
    // ...
  }
}
```

**步骤 5.2c — 修改登录区域模板**

将原有登录卡片部分改为条件渲染：

```html
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
```

**步骤 5.2d — 添加 onRegistered 方法**

在 methods 中：

```javascript
onRegistered(user) {
  this.currentUser = user
  sessionStorage.setItem('dorm-user', JSON.stringify(user))
  this.showRegister = false
  if (!user.profileComplete) {
    // Will be handled by mounted logic (after this method)
  }
  this.selectMenu(getVisibleMenus(user.role)[0])
}
```

**验证:** 登录页显示注册链接，点击切换到注册表单，注册成功后自动登录。

---

## Phase 6: 前端 — 特征补全

### Task 6.1 — 创建 ProfileCompletionModal.vue

**文件:** `C:\DormMS\dormitory-system\frontend\src\components\ProfileCompletionModal.vue`（新建）

```html
<template>
  <div class="modal-overlay">
    <div class="modal-card profile-modal">
      <h2>完善个人资料</h2>
      <p class="modal-desc">为了使用智能宿舍分配功能，请完善以下信息</p>

      <div class="field">
        <label>性别 <span class="required">*</span></label>
        <div class="radio-group">
          <label class="radio-label"><input type="radio" v-model="form.gender" value="男"> 男</label>
          <label class="radio-label"><input type="radio" v-model="form.gender" value="女"> 女</label>
        </div>
      </div>
      <div class="field">
        <label>专业班级 <span class="required">*</span></label>
        <input v-model="form.majorClass" placeholder="如：智能科学与技术2025级">
      </div>
      <div class="field">
        <label>作息习惯 <span class="required">*</span></label>
        <div class="radio-group">
          <label class="radio-label"><input type="radio" v-model="form.sleepHabit" value="早睡"> 早睡</label>
          <label class="radio-label"><input type="radio" v-model="form.sleepHabit" value="晚睡"> 晚睡</label>
        </div>
      </div>
      <div class="field">
        <label>是否抽烟 <span class="required">*</span></label>
        <div class="radio-group">
          <label class="radio-label"><input type="radio" v-model="form.smoking" value="否"> 否</label>
          <label class="radio-label"><input type="radio" v-model="form.smoking" value="是"> 是</label>
        </div>
      </div>
      <div class="field">
        <label>兴趣爱好</label>
        <div class="tag-group">
          <span v-for="h in hobbyOptions" :key="h"
                :class="['tag', { active: form.hobbies.includes(h) }]"
                @click="toggleHobby(h)">{{ h }}</span>
        </div>
      </div>
      <div class="field">
        <label>卫生习惯 <span class="required">*</span></label>
        <div class="radio-group">
          <label class="radio-label"><input type="radio" v-model="form.cleanliness" value="整洁"> 整洁</label>
          <label class="radio-label"><input type="radio" v-model="form.cleanliness" value="一般"> 一般</label>
          <label class="radio-label"><input type="radio" v-model="form.cleanliness" value="随意"> 随意</label>
        </div>
      </div>
      <div class="field">
        <label>是否打游戏 <span class="required">*</span></label>
        <div class="radio-group">
          <label class="radio-label"><input type="radio" v-model="form.gaming" value="是"> 是</label>
          <label class="radio-label"><input type="radio" v-model="form.gaming" value="否"> 否</label>
        </div>
      </div>
      <div class="field">
        <label>是否打鼾 <span class="required">*</span></label>
        <div class="radio-group">
          <label class="radio-label"><input type="radio" v-model="form.snoring" value="否"> 否</label>
          <label class="radio-label"><input type="radio" v-model="form.snoring" value="是"> 是</label>
        </div>
      </div>
      <div class="field">
        <label>作息返回时间 <span class="required">*</span></label>
        <div class="radio-group">
          <label class="radio-label"><input type="radio" v-model="form.returnTime" value="早归"> 早归</label>
          <label class="radio-label"><input type="radio" v-model="form.returnTime" value="正常"> 正常</label>
          <label class="radio-label"><input type="radio" v-model="form.returnTime" value="晚归"> 晚归</label>
        </div>
      </div>
      <div class="field">
        <label>噪音容忍度 <span class="required">*</span></label>
        <div class="radio-group">
          <label class="radio-label"><input type="radio" v-model="form.noiseTolerance" value="安静"> 安静</label>
          <label class="radio-label"><input type="radio" v-model="form.noiseTolerance" value="正常"> 正常</label>
          <label class="radio-label"><input type="radio" v-model="form.noiseTolerance" value="热闹"> 热闹</label>
        </div>
      </div>

      <p class="error">{{ error }}</p>
      <button class="blue-btn block" @click="submit">保存并进入系统</button>
    </div>
  </div>
</template>

<script>
import { updateProfile } from '../api.js'

const HOBBY_OPTIONS = ['篮球','足球','羽毛球','跑步','游泳','健身','编程','阅读','音乐','电影','游戏','摄影','绘画','旅行','美食']

export default {
  name: 'ProfileCompletionModal',
  emits: ['completed'],
  data() {
    return {
      error: '',
      hobbyOptions: HOBBY_OPTIONS,
      form: {
        gender: '男', majorClass: '', sleepHabit: '晚睡', smoking: '否',
        hobbies: [], cleanliness: '整洁', gaming: '是', snoring: '否',
        returnTime: '正常', noiseTolerance: '正常'
      }
    }
  },
  methods: {
    toggleHobby(h) {
      const i = this.form.hobbies.indexOf(h)
      if (i >= 0) this.form.hobbies.splice(i, 1)
      else this.form.hobbies.push(h)
    },
    async submit() {
      this.error = ''
      if (!this.form.gender || !this.form.majorClass || !this.form.sleepHabit
          || !this.form.smoking || !this.form.cleanliness || !this.form.gaming
          || !this.form.snoring || !this.form.returnTime || !this.form.noiseTolerance) {
        this.error = '请完善所有信息'
        return
      }
      try {
        await updateProfile({
          gender: this.form.gender,
          majorClass: this.form.majorClass,
          sleepHabit: this.form.sleepHabit,
          smoking: this.form.smoking,
          hobbies: this.form.hobbies.join(','),
          cleanliness: this.form.cleanliness,
          gaming: this.form.gaming,
          snoring: this.form.snoring,
          returnTime: this.form.returnTime,
          noiseTolerance: this.form.noiseTolerance
        })
        // Update local user state
        const user = JSON.parse(sessionStorage.getItem('dorm-user') || '{}')
        user.profileComplete = true
        sessionStorage.setItem('dorm-user', JSON.stringify(user))
        this.$emit('completed')
      } catch (e) {
        this.error = e.message
      }
    }
  }
}
</script>
```

**验证:** 弹窗不可关闭，提交后更新 sessionStorage。

---

### Task 6.2 — App.vue 集成特征补全弹窗

**文件:** `C:\DormMS\dormitory-system\frontend\src\App.vue`

**步骤 6.2a — 导入组件**

```javascript
import ProfileCompletionModal from './components/ProfileCompletionModal.vue'
```

在 `components` 中注册。

**步骤 6.2b — data 中添加状态**

```javascript
showProfileModal: false
```

**步骤 6.2c — 修改 login() 方法**

在 `login()` 的 try 块中，`this.currentUser = user` 之后添加：

```javascript
if (user.profileComplete === false) {
  this.showProfileModal = true
  return // Don't navigate to menu yet
}
```

**步骤 6.2d — 模板中添加弹窗**

在 `<section v-else class="student-shell">` 内部的顶部（sidebar 之前）添加：

```html
<ProfileCompletionModal v-if="showProfileModal" @completed="onProfileCompleted" />
```

**步骤 6.2e — 添加 onProfileCompleted 方法**

```javascript
onProfileCompleted() {
  this.showProfileModal = false
  // Refresh currentUser from sessionStorage
  this.currentUser = JSON.parse(sessionStorage.getItem('dorm-user') || 'null')
  this.selectMenu(this.visibleMenus[0])
}
```

**验证:** 用没有特征的账号登录（如 admin — 但 admin 应该返回 profileComplete=true。可临时注册新账号测试），弹窗出现。提交后进入主界面。

---

## Phase 7: 前端 — 账户设置改造

### Task 7.1 — 改造 AccountSettings.vue

**文件:** `C:\DormMS\dormitory-system\frontend\src\components\AccountSettings.vue`

完全替换为支持特征编辑的版本：

```html
<template>
  <section class="content-card">
    <div class="card-title">
      <h2>账户设置</h2>
      <span>个人信息管理</span>
    </div>

    <!-- Basic info (readonly) -->
    <div class="profile-form">
      <div><label>姓名</label><input :value="displayName" readonly></div>
      <div><label>角色</label><input :value="roleLabel" readonly></div>
      <div><label>宿舍/岗位</label><input :value="currentUser.roomNo || '未分配'" readonly></div>
      <div><label>电话</label><input :value="currentUser.phone || '-'" readonly></div>
      <div><label>用户名</label><input :value="currentUser.username" readonly></div>
      <div><label>注册时间</label><input :value="currentUser.createdAt || '-'" readonly></div>
    </div>

    <!-- Feature info (editable for students) -->
    <div v-if="currentUser.role === 'student'" class="profile-form feature-section">
      <h3>个人特征（用于智能宿舍分配）</h3>
      <p class="section-desc">修改后保存即可，影响后续宿舍分配和调宿推荐</p>

      <div><label>性别</label>
        <select v-model="form.gender">
          <option value="男">男</option><option value="女">女</option>
        </select>
      </div>
      <div><label>专业班级</label><input v-model="form.majorClass"></div>
      <div><label>作息习惯</label>
        <select v-model="form.sleepHabit">
          <option value="早睡">早睡</option><option value="晚睡">晚睡</option>
        </select>
      </div>
      <div><label>是否抽烟</label>
        <select v-model="form.smoking">
          <option value="否">否</option><option value="是">是</option>
        </select>
      </div>
      <div><label>兴趣爱好</label>
        <div class="tag-group">
          <span v-for="h in hobbyOptions" :key="h"
                :class="['tag', { active: formHobbies.includes(h) }]"
                @click="toggleHobby(h)">{{ h }}</span>
        </div>
      </div>
      <div><label>卫生习惯</label>
        <select v-model="form.cleanliness">
          <option value="整洁">整洁</option><option value="一般">一般</option><option value="随意">随意</option>
        </select>
      </div>
      <div><label>是否打游戏</label>
        <select v-model="form.gaming">
          <option value="是">是</option><option value="否">否</option>
        </select>
      </div>
      <div><label>是否打鼾</label>
        <select v-model="form.snoring">
          <option value="否">否</option><option value="是">是</option>
        </select>
      </div>
      <div><label>作息返回时间</label>
        <select v-model="form.returnTime">
          <option value="早归">早归</option><option value="正常">正常</option><option value="晚归">晚归</option>
        </select>
      </div>
      <div><label>噪音容忍度</label>
        <select v-model="form.noiseTolerance">
          <option value="安静">安静</option><option value="正常">正常</option><option value="热闹">热闹</option>
        </select>
      </div>
      <p class="error">{{ saveMessage }}</p>
      <button class="blue-btn" @click="saveProfile">保存特征</button>
    </div>
  </section>
</template>

<script>
import { updateProfile, fetchUsers } from '../api.js'

const HOBBY_OPTIONS = ['篮球','足球','羽毛球','跑步','游泳','健身','编程','阅读','音乐','电影','游戏','摄影','绘画','旅行','美食']

export default {
  name: 'AccountSettings',
  props: {
    currentUser: { type: Object, required: true },
    displayName: { type: String, default: '' }
  },
  data() {
    return {
      hobbyOptions: HOBBY_OPTIONS,
      form: { gender:'', majorClass:'', sleepHabit:'', smoking:'', hobbies:[], cleanliness:'', gaming:'', snoring:'', returnTime:'', noiseTolerance:'' },
      saveMessage: ''
    }
  },
  computed: {
    roleLabel() {
      const map = { admin: '管理员', student: '学生端', dormkeeper: '宿管员' }
      return map[this.currentUser.role] || this.currentUser.role
    },
    formHobbies() {
      return this.form.hobbies || []
    }
  },
  mounted() {
    if (this.currentUser.role === 'student') {
      this.loadProfile()
    }
  },
  methods: {
    async loadProfile() {
      try {
        const data = await fetchUsers('', 1, 100)
        const users = data.rows || []
        const me = users.find(u => u.id === this.currentUser.id)
        if (me) {
          this.form.gender = me.gender || '男'
          this.form.majorClass = me.majorClass || ''
          this.form.sleepHabit = me.sleepHabit || '晚睡'
          this.form.smoking = me.smoking || '否'
          this.form.hobbies = me.hobbies ? me.hobbies.split(',') : []
          this.form.cleanliness = me.cleanliness || '整洁'
          this.form.gaming = me.gaming || '是'
          this.form.snoring = me.snoring || '否'
          this.form.returnTime = me.returnTime || '正常'
          this.form.noiseTolerance = me.noiseTolerance || '正常'
        }
      } catch (e) {
        // Use defaults
      }
    },
    toggleHobby(h) {
      const arr = this.form.hobbies
      const i = arr.indexOf(h)
      if (i >= 0) arr.splice(i, 1)
      else arr.push(h)
    },
    async saveProfile() {
      this.saveMessage = ''
      try {
        await updateProfile({
          gender: this.form.gender,
          majorClass: this.form.majorClass,
          sleepHabit: this.form.sleepHabit,
          smoking: this.form.smoking,
          hobbies: this.form.hobbies.join(','),
          cleanliness: this.form.cleanliness,
          gaming: this.form.gaming,
          snoring: this.form.snoring,
          returnTime: this.form.returnTime,
          noiseTolerance: this.form.noiseTolerance
        })
        this.saveMessage = '保存成功'
        // Refresh user data
        const user = JSON.parse(sessionStorage.getItem('dorm-user') || '{}')
        user.profileComplete = true
        sessionStorage.setItem('dorm-user', JSON.stringify(user))
      } catch (e) {
        this.saveMessage = e.message
      }
    }
  }
}
</script>
```

**验证:** 学生角色登录后，账户设置页显示可编辑的特征表单，修改后保存成功。

---

## Phase 8: 前端 — 智能分配页面

### Task 8.1 — 创建 SmartAssignment.vue

**文件:** `C:\DormMS\dormitory-system\frontend\src\components\SmartAssignment.vue`（新建）

```html
<template>
  <section class="content-card">
    <div class="card-title">
      <h2>智能宿舍分配</h2>
      <span>基于学生特征的自动分配 & 调宿推荐</span>
    </div>

    <!-- Action area -->
    <div class="action-bar">
      <div class="stats-row">
        <span>待分配学生: <b>{{ summary.unassigned || 0 }}</b> 人</span>
        <span>已分配宿舍: <b>{{ summary.roomCount || 0 }}</b> 间</span>
        <span>空余床位: <b>{{ summary.emptyBeds || 0 }}</b> 床</span>
      </div>
      <button class="blue-btn" @click="runAssignment" :disabled="running">
        {{ running ? '分配中...' : '开始新生分配' }}
      </button>
      <p v-if="resultMessage" class="result-msg">{{ resultMessage }}</p>
    </div>

    <!-- Assignment result matrix -->
    <div v-if="rooms.length > 0" class="assignment-result">
      <h3>分配结果</h3>
      <div v-for="room in rooms" :key="room.roomNo" class="room-card">
        <div class="room-header">
          <strong>{{ room.roomNo }}</strong>
          <span class="compat-badge">兼容度均分: {{ room.avgCompatibility }}</span>
        </div>
        <div class="bed-grid">
          <div v-for="(m, idx) in room.members" :key="m.id" class="bed-cell occupied" :title="tooltip(m)">
            <span class="bed-num">{{ idx + 1 }}号床</span>
            <span class="bed-name">{{ m.realName }}</span>
            <span class="bed-class">{{ m.majorClass }}</span>
          </div>
          <div v-for="n in (4 - room.members.length)" :key="'e'+n" class="bed-cell empty">
            <span class="bed-num">{{ room.members.length + n }}号床</span>
            <span class="bed-name">空床位</span>
          </div>
        </div>
      </div>

      <button v-if="rooms.length > 0" class="green-btn block" @click="confirmAll">确认以上分配</button>
    </div>

    <!-- Transfer recommendation -->
    <div class="transfer-section">
      <h3>调宿匹配推荐</h3>
      <div class="transfer-row">
        <select v-model="selectedStudentId">
          <option :value="null" disabled>-- 选择待调宿学生 --</option>
          <option v-for="s in studentList" :key="s.id" :value="s.id">{{ s.realName }} ({{ s.majorClass }})</option>
        </select>
        <button class="blue-btn" @click="getRecommendations" :disabled="!selectedStudentId">推荐宿舍</button>
      </div>

      <div v-if="recommendations.length > 0" class="recommend-list">
        <div v-for="(rec, idx) in recommendations" :key="rec.roomNo" class="recommend-card">
          <div class="rec-rank">#{{ idx + 1 }}</div>
          <div class="rec-info">
            <strong>{{ rec.roomNo }}</strong>
            <span>现有 {{ rec.currentCount }}/4 人</span>
            <span class="rec-score">匹配度 {{ rec.compatibility }} 分</span>
            <span class="rec-reason">{{ rec.reason }}</span>
          </div>
        </div>
      </div>
      <p v-if="recommendations.length === 0 && selectedStudentId && recLoaded" class="dim">暂无推荐结果</p>
    </div>
  </section>
</template>

<script>
import { fetchUsers, runAssignment, recommendTransfer, confirmAssignment } from '../api.js'

export default {
  name: 'SmartAssignment',
  data() {
    return {
      running: false,
      resultMessage: '',
      rooms: [],
      summary: { unassigned: 0, roomCount: 0, emptyBeds: 0 },
      studentList: [],
      selectedStudentId: null,
      recommendations: [],
      recLoaded: false
    }
  },
  mounted() {
    this.loadSummary()
  },
  methods: {
    async loadSummary() {
      try {
        const data = await fetchUsers('', 1, 200)
        const all = data.rows || []
        const students = all.filter(u => u.role === 'student')
        const assigned = students.filter(s => s.roomNo)
        const unassigned = students.filter(s => !s.roomNo)
        // Estimate rooms from assigned student room_no prefixes
        const roomSet = new Set()
        assigned.forEach(s => {
          const prefix = (s.roomNo || '').replace(/ · \d+号床.*$/, '')
          if (prefix) roomSet.add(prefix)
        })
        this.summary.unassigned = unassigned.length
        this.summary.roomCount = roomSet.size
        this.summary.emptyBeds = (roomSet.size * 4) - assigned.length
        this.studentList = students
      } catch (e) {
        // ignore
      }
    },
    async runAssignment() {
      this.running = true
      this.resultMessage = ''
      try {
        const data = await runAssignment()
        this.rooms = data.rooms || []
        this.resultMessage = `分配完成！共 ${this.rooms.length} 间宿舍，${data.unassignedCount || 0} 人未分配`
      } catch (e) {
        this.resultMessage = '分配失败: ' + e.message
      } finally {
        this.running = false
      }
    },
    async getRecommendations() {
      if (!this.selectedStudentId) return
      this.recLoaded = false
      try {
        const data = await recommendTransfer(this.selectedStudentId)
        this.recommendations = data || []
        this.recLoaded = true
      } catch (e) {
        this.resultMessage = '推荐失败: ' + e.message
        this.recLoaded = true
      }
    },
    tooltip(m) {
      return `作息:${m.sleepHabit} 抽烟:${m.smoking} 卫生:${m.cleanliness} 游戏:${m.gaming}`
    },
    async confirmAll() {
      if (!confirm('确认执行此分配方案？将更新学生宿舍信息。')) return
      const assignments = []
      this.rooms.forEach(room => {
        room.members.forEach((m, idx) => {
          assignments.push({
            studentId: m.id,
            roomNo: room.roomNo,
            bedNo: (idx + 1) + '号床'
          })
        })
      })
      try {
        await confirmAssignment(assignments)
        alert('分配已确认！')
        this.loadSummary()
      } catch (e) {
        alert('确认失败: ' + e.message)
      }
    }
  }
}
</script>
```

**验证:** 页面加载显示统计数据，点击按钮执行分配，结果表格展示。

---

### Task 8.2 — router.js 添加智能分配菜单

**文件:** `C:\DormMS\dormitory-system\frontend\src\router.js`

在 MENUS 数组中、admin 角色菜单区域（如 `building` 之前）添加：

```javascript
{ key: 'assignment', label: '智能分配', icon: '⚡', roles: ['admin'], section: '后台管理', desc: '智能宿舍分配与调宿推荐' },
```

在 `PAGES_WITHOUT_RECORDS` 数组中添加 `'assignment'`：

```javascript
const PAGES_WITHOUT_RECORDS = ['dashboard', 'myDorm', 'ai', 'account', 'chat', 'assignment']
```

**验证:** admin 角色登录后左侧菜单出现"智能分配"项。

---

### Task 8.3 — App.vue 注册 SmartAssignment 组件

**文件:** `C:\DormMS\dormitory-system\frontend\src\App.vue`

**步骤 8.3a — 导入**

```javascript
import SmartAssignment from './components/SmartAssignment.vue'
```

在 `components` 中注册。

**步骤 8.3b — 模板中添加渲染**

在模板中（如 `RatingPage` 渲染块之后）添加：

```html
<!-- Smart Assignment (admin) -->
<template v-else-if="activeMenu && activeMenu.key === 'assignment'">
  <SmartAssignment />
</template>
```

**验证:** 管理员角色点击"智能分配"菜单，页面正常渲染。

---

## Phase 9: 验证 & 集成测试

### Task 9.1 — 端到端验证清单

| # | 验证项 | 操作 | 预期结果 |
|---|--------|------|----------|
| 1 | 数据库迁移 | 启动应用 | sys_user 表增加 10 个新列，老数据不丢失 |
| 2 | 种子特征 | 用 student/123456 登录 | profileComplete=true，直接进主界面 |
| 3 | 新用户注册 | 登录页点击注册→填写两页→提交 | 自动登录，返回 profileComplete=true |
| 4 | 特征补全弹窗 | 手动把某个学生特征字段置 NULL→登录 | 弹窗出现，不可跳过，填完提交后进主界面 |
| 5 | 个人设置编辑 | student 登录→账户设置 | 显示特征编辑区，修改后保存 |
| 6 | 智能分配 | admin 登录→智能分配→开始分配 | 显示分配结果矩阵，有房间和成员 |
| 7 | 调宿推荐 | 智能分配页→选学生→推荐 | 显示 Top 3 推荐 |
| 8 | 确认分配 | 分配结果→确认 | 数据库 room_no 更新 |

---

## 附: 需要添加的 CSS 样式

以下样式需要在 `App.vue` 的 `<style>` 或全局样式表中添加：

```css
/* Register link */
.register-link { text-align: center; margin-top: 16px; font-size: 14px; color: #666; }
.register-link a { color: #4b8fe8; cursor: pointer; text-decoration: none; }

/* Modal overlay */
.modal-overlay { position: fixed; inset: 0; background: rgba(0,0,0,0.5); z-index: 1000; display: flex; align-items: center; justify-content: center; }
.modal-card { background: #fff; border-radius: 12px; padding: 32px; max-width: 560px; width: 90%; max-height: 85vh; overflow-y: auto; box-shadow: 0 8px 32px rgba(0,0,0,0.2); }
.modal-desc { color: #888; margin-bottom: 16px; font-size: 14px; }

/* Radio groups */
.radio-group { display: flex; gap: 16px; }
.radio-label { display: flex; align-items: center; gap: 4px; font-size: 14px; cursor: pointer; }

/* Tag select */
.tag-group { display: flex; flex-wrap: wrap; gap: 8px; }
.tag { padding: 4px 12px; border-radius: 16px; background: #f0f0f0; font-size: 13px; cursor: pointer; user-select: none; }
.tag.active { background: #4b8fe8; color: #fff; }

/* Step buttons */
.step-buttons { display: flex; gap: 12px; margin-top: 16px; }
.outline-btn { flex:1; padding: 10px; border: 1px solid #ddd; border-radius: 8px; background: #fff; cursor: pointer; font-size: 14px; }

/* Required marker */
.required { color: #e74c3c; }

/* Feature section */
.feature-section { margin-top: 24px; padding-top: 16px; border-top: 1px solid #eee; }
.feature-section h3 { margin-bottom: 4px; }
.section-desc { font-size: 13px; color: #999; margin-bottom: 16px; }

/* Assignment result */
.action-bar { background: #f8f9fa; padding: 16px; border-radius: 8px; margin-bottom: 20px; }
.stats-row { display: flex; gap: 24px; margin-bottom: 12px; font-size: 14px; }
.result-msg { color: #27ae60; font-size: 14px; margin-top: 8px; }
.room-card { background: #fff; border: 1px solid #e8e8e8; border-radius: 8px; padding: 16px; margin-bottom: 12px; }
.room-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px; }
.compat-badge { background: #e8f4fd; color: #4b8fe8; padding: 2px 10px; border-radius: 12px; font-size: 13px; }
.bed-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 8px; }
.bed-cell { padding: 10px; border-radius: 6px; text-align: center; font-size: 13px; }
.bed-cell.occupied { background: #f0f7ff; border: 1px solid #cce0ff; }
.bed-cell.empty { background: #fafafa; border: 1px dashed #ddd; color: #bbb; }
.bed-num { display: block; font-size: 11px; color: #999; }
.bed-name { display: block; font-weight: 600; }
.bed-class { display: block; font-size: 11px; color: #888; }

/* Transfer */
.transfer-section { margin-top: 32px; }
.transfer-row { display: flex; gap: 12px; align-items: center; margin-bottom: 16px; }
.transfer-row select { flex: 1; padding: 8px 12px; border: 1px solid #ddd; border-radius: 8px; font-size: 14px; }
.recommend-card { display: flex; gap: 12px; padding: 12px; border: 1px solid #e8e8e8; border-radius: 8px; margin-bottom: 8px; }
.rec-rank { font-size: 24px; font-weight: 700; color: #4b8fe8; min-width: 40px; text-align: center; }
.rec-info { display: flex; flex-direction: column; gap: 4px; }
.rec-score { color: #27ae60; font-weight: 600; }
.rec-reason { font-size: 13px; color: #888; }
.green-btn { background: #27ae60; color: #fff; padding: 10px 24px; border: none; border-radius: 8px; cursor: pointer; font-size: 14px; }
.dim { color: #bbb; font-size: 14px; }
```

**验证:** 新组件 UI 正常显示，样式美观。
