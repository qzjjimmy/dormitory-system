package com.bishe.dormitory.service;

import com.bishe.dormitory.common.PageResult;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class UserService {
    private final JdbcTemplate jdbcTemplate;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public UserService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Map<String, Object> login(String username, String password) {
        List<Map<String, Object>> users = jdbcTemplate.queryForList(
                "SELECT id,username,password,real_name AS realName,role,phone,room_no AS roomNo," +
                "gender,major_class AS majorClass,sleep_habit AS sleepHabit,smoking,hobbies," +
                "cleanliness,gaming,snoring,return_time AS returnTime,noise_tolerance AS noiseTolerance," +
                "preferred_room_type AS preferredRoomType,preferred_bed AS preferredBed " +
                "FROM sys_user WHERE username=?",
                username);
        if (users.isEmpty()) {
            throw new IllegalArgumentException("账号或密码错误");
        }
        Map<String, Object> user = users.get(0);
        String storedPassword = (String) user.get("password");

        // BCrypt verification; fall back to plaintext for legacy passwords, then re-hash
        boolean match;
        if (storedPassword != null && storedPassword.startsWith("$2a$")) {
            match = passwordEncoder.matches(password, storedPassword);
        } else {
            match = password.equals(storedPassword);
            if (match) {
                // Upgrade legacy plaintext password to BCrypt
                jdbcTemplate.update("UPDATE sys_user SET password=? WHERE id=?",
                        passwordEncoder.encode(password), user.get("id"));
            }
        }

        if (!match) {
            throw new IllegalArgumentException("账号或密码错误");
        }

        // Remove password field before returning to frontend
        user.remove("password");

        // Check profile completeness for students
        String role = (String) user.get("role");
        boolean profileComplete = true;
        if ("student".equals(role)) {
            profileComplete = user.get("gender") != null && user.get("sleepHabit") != null
                && user.get("smoking") != null && user.get("hobbies") != null
                && user.get("cleanliness") != null && user.get("gaming") != null
                && user.get("snoring") != null && user.get("returnTime") != null
                && user.get("noiseTolerance") != null && user.get("majorClass") != null;
        }
        user.put("profileComplete", profileComplete);

        return user;
    }

    public Map<String, Object> list(String keyword, int page, int size) {
        String like = "%" + keyword + "%";
        int offset = (page - 1) * size;
        if (keyword == null || keyword.isEmpty()) {
            List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                    "SELECT id,username,real_name AS realName,role,phone,room_no AS roomNo,created_at AS createdAt," +
                    "gender,major_class AS majorClass,sleep_habit AS sleepHabit,smoking,hobbies," +
                    "cleanliness,gaming,snoring,return_time AS returnTime,noise_tolerance AS noiseTolerance," +
                    "preferred_room_type AS preferredRoomType,preferred_bed AS preferredBed " +
                    "FROM sys_user ORDER BY id DESC LIMIT ? OFFSET ?",
                    size, offset);
            Long total = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM sys_user", Long.class);
            return new PageResult<>(rows, total != null ? total : 0, page, size).toMap();
        }
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT id,username,real_name AS realName,role,phone,room_no AS roomNo,created_at AS createdAt," +
                "gender,major_class AS majorClass,sleep_habit AS sleepHabit,smoking,hobbies," +
                "cleanliness,gaming,snoring,return_time AS returnTime,noise_tolerance AS noiseTolerance," +
                "preferred_room_type AS preferredRoomType,preferred_bed AS preferredBed " +
                "FROM sys_user WHERE username LIKE ? OR real_name LIKE ? OR role LIKE ? ORDER BY id DESC LIMIT ? OFFSET ?",
                like, like, like, size, offset);
        Long total = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM sys_user WHERE username LIKE ? OR real_name LIKE ? OR role LIKE ?",
                Long.class, like, like, like);
        return new PageResult<>(rows, total != null ? total : 0, page, size).toMap();
    }

    public void create(Map<String, String> user) {
        String username = user.get("username");
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM sys_user WHERE username=?", Integer.class, username);
        if (count != null && count > 0) {
            throw new IllegalArgumentException("用户名已存在");
        }
        String rawPassword = user.getOrDefault("password", "123456");
        jdbcTemplate.update(
                "INSERT INTO sys_user(username,password,real_name,role,phone,room_no,gender,major_class,sleep_habit,smoking,hobbies,cleanliness,gaming,snoring,return_time,noise_tolerance,preferred_room_type,preferred_bed) " +
                "VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
                username, passwordEncoder.encode(rawPassword), user.get("realName"),
                user.getOrDefault("role", "student"), user.get("phone"), user.get("roomNo"),
                user.get("gender"), user.get("majorClass"), user.get("sleepHabit"),
                user.get("smoking"), user.get("hobbies"), user.get("cleanliness"),
                user.get("gaming"), user.get("snoring"), user.get("returnTime"),
                user.get("noiseTolerance"), user.get("preferredRoomType"), user.get("preferredBed"));
    }

    public void update(Long id, Map<String, String> user) {
        jdbcTemplate.update("UPDATE sys_user SET real_name=?,role=?,phone=?,room_no=? WHERE id=?",
                user.get("realName"), user.get("role"), user.get("phone"), user.get("roomNo"), id);
    }

    public void delete(Long id) {
        jdbcTemplate.update("DELETE FROM sys_user WHERE id=?", id);
    }

    public void validateUser(Map<String, String> user, boolean isCreate) {
        if (isCreate) {
            if (user.get("username") == null || user.get("username").trim().isEmpty()) {
                throw new IllegalArgumentException("用户名不能为空");
            }
        }
        if (user.get("realName") == null || user.get("realName").trim().isEmpty()) {
            throw new IllegalArgumentException("姓名不能为空");
        }
        if (user.get("role") == null || user.get("role").trim().isEmpty()) {
            throw new IllegalArgumentException("角色不能为空");
        }
    }

    public void updateProfile(Long userId, Map<String, String> profile) {
        jdbcTemplate.update(
                "UPDATE sys_user SET gender=?,major_class=?,sleep_habit=?,smoking=?,hobbies=?," +
                "cleanliness=?,gaming=?,snoring=?,return_time=?,noise_tolerance=?," +
                "preferred_room_type=?,preferred_bed=?,phone=? WHERE id=?",
                profile.get("gender"), profile.get("majorClass"), profile.get("sleepHabit"),
                profile.get("smoking"), profile.get("hobbies"), profile.get("cleanliness"),
                profile.get("gaming"), profile.get("snoring"), profile.get("returnTime"),
                profile.get("noiseTolerance"), profile.get("preferredRoomType"), profile.get("preferredBed"),
                profile.get("phone"), userId);
    }
}
