package com.bishe.dormitory.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ChatService {
    private final JdbcTemplate jdbcTemplate;

    public ChatService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Map<String, Object>> getContacts(Long userId) {
        String userRoom = jdbcTemplate.queryForObject(
                "SELECT room_no FROM sys_user WHERE id=?", String.class, userId);

        if (userRoom == null) {
            // Unassigned student: show admin and dormkeepers only
            return jdbcTemplate.queryForList(
                    "SELECT id, real_name AS realName, role, room_no AS roomNo, phone, " +
                            "CASE WHEN role='admin' THEN 0 ELSE 1 END AS sort_order " +
                            "FROM sys_user WHERE role IN ('admin','dormkeeper') ORDER BY sort_order, id");
        }

        // Assigned student: show roommates (same room prefix) + admin + dormkeepers
        String roomPrefix = userRoom.replaceAll(" · \\d+号床.*$", "").trim();
        return jdbcTemplate.queryForList(
                "SELECT id, real_name AS realName, role, room_no AS roomNo, phone, " +
                        "CASE WHEN room_no LIKE ? THEN 0 WHEN role='dormkeeper' THEN 1 WHEN role='admin' THEN 2 ELSE 3 END AS sort_order " +
                        "FROM sys_user WHERE id != ? AND (room_no LIKE ? OR role IN ('admin','dormkeeper')) " +
                        "ORDER BY sort_order, id",
                roomPrefix + "%", userId, roomPrefix + "%");
    }

    public List<Map<String, Object>> getMessages(Long userId, Long withUserId) {
        return jdbcTemplate.queryForList(
                "SELECT m.id, m.from_user_id AS fromUserId, m.to_user_id AS toUserId, " +
                        "m.content, m.is_read AS isRead, m.created_at AS createdAt, " +
                        "u.real_name AS fromName " +
                        "FROM chat_message m JOIN sys_user u ON m.from_user_id = u.id " +
                        "WHERE (m.from_user_id=? AND m.to_user_id=?) OR (m.from_user_id=? AND m.to_user_id=?) " +
                        "ORDER BY m.id ASC",
                userId, withUserId, withUserId, userId);
    }

    public void markAsRead(Long userId, Long fromUserId) {
        jdbcTemplate.update(
                "UPDATE chat_message SET is_read=1 WHERE to_user_id=? AND from_user_id=? AND is_read=0",
                userId, fromUserId);
    }

    public Map<String, Object> getUnreadCounts(Long userId) {
        List<Map<String, Object>> counts = jdbcTemplate.queryForList(
                "SELECT from_user_id AS fromUserId, COUNT(*) AS cnt " +
                        "FROM chat_message WHERE to_user_id=? AND is_read=0 GROUP BY from_user_id",
                userId);
        Map<String, Object> result = new HashMap<>();
        result.put("counts", counts);
        int total = counts.stream().mapToInt(c -> ((Number) c.get("cnt")).intValue()).sum();
        result.put("total", total);
        return result;
    }

    public Map<String, Object> sendMessage(Long fromUserId, Long toUserId, String content) {
        jdbcTemplate.update(
                "INSERT INTO chat_message(from_user_id, to_user_id, content) VALUES(?,?,?)",
                fromUserId, toUserId, content);

        Long msgId = jdbcTemplate.queryForObject("SELECT LAST_INSERT_ID()", Long.class);

        Map<String, Object> msg = new HashMap<>();
        msg.put("id", msgId);
        msg.put("fromUserId", fromUserId);
        msg.put("toUserId", toUserId);
        msg.put("content", content);
        msg.put("isRead", 0);
        msg.put("createdAt", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        return msg;
    }
}
