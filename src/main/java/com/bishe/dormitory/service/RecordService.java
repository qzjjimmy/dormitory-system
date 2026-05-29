package com.bishe.dormitory.service;

import com.bishe.dormitory.common.PageResult;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class RecordService {
    private final JdbcTemplate jdbcTemplate;

    public RecordService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Map<String, Object> list(String category, String keyword, int page, int size) {
        String like = "%" + keyword + "%";
        int offset = (page - 1) * size;
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT id,category,title,owner,location,amount,status,content,created_at AS createdAt,updated_at AS updatedAt " +
                        "FROM biz_record WHERE category=? AND (title LIKE ? OR owner LIKE ? OR location LIKE ? OR status LIKE ?) ORDER BY id DESC LIMIT ? OFFSET ?",
                category, like, like, like, like, size, offset);
        Long total = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM biz_record WHERE category=? AND (title LIKE ? OR owner LIKE ? OR location LIKE ? OR status LIKE ?)",
                Long.class, category, like, like, like, like);
        return new PageResult<>(rows, total != null ? total : 0, page, size).toMap();
    }

    public void create(Map<String, Object> record) {
        jdbcTemplate.update("INSERT INTO biz_record(category,title,owner,location,amount,status,content) VALUES(?,?,?,?,?,?,?)",
                text(record, "category"), text(record, "title"), text(record, "owner"), text(record, "location"),
                amount(record), text(record, "status"), text(record, "content"));
    }

    public void update(Long id, Map<String, Object> record) {
        jdbcTemplate.update("UPDATE biz_record SET title=?,owner=?,location=?,amount=?,status=?,content=? WHERE id=?",
                text(record, "title"), text(record, "owner"), text(record, "location"), amount(record),
                text(record, "status"), text(record, "content"), id);
    }

    public void delete(Long id) {
        jdbcTemplate.update("DELETE FROM biz_record WHERE id=?", id);
    }

    public Map<String, Object> dashboard() {
        Map<String, Object> data = new HashMap<>();
        data.put("users", jdbcTemplate.queryForObject("SELECT COUNT(*) FROM sys_user", Integer.class));
        data.put("rooms", countByCategory("room"));
        data.put("repairs", countByCategory("repair"));
        data.put("visitors", countByCategory("visitor"));
        data.put("fees", jdbcTemplate.queryForObject("SELECT COALESCE(SUM(amount),0) FROM biz_record WHERE category='fee'", Double.class));
        List<Map<String, Object>> categories = jdbcTemplate.queryForList(
                "SELECT category, COUNT(*) AS value FROM biz_record GROUP BY category ORDER BY value DESC");
        data.put("categories", categories);
        data.put("todos", jdbcTemplate.queryForList(
                "SELECT id,title,status,category,created_at AS createdAt FROM biz_record WHERE status IN ('待处理','待审核','审核中','待缴费','待回复') ORDER BY id DESC LIMIT 8"));
        return data;
    }

    public void validateRecord(Map<String, Object> record) {
        if (text(record, "title").trim().isEmpty()) {
            throw new IllegalArgumentException("标题不能为空");
        }
        if (text(record, "category").trim().isEmpty()) {
            throw new IllegalArgumentException("分类不能为空");
        }
        if (text(record, "status").trim().isEmpty()) {
            throw new IllegalArgumentException("状态不能为空");
        }
    }

    private Integer countByCategory(String category) {
        return jdbcTemplate.queryForObject("SELECT COUNT(*) FROM biz_record WHERE category=?", Integer.class, category);
    }

    private String text(Map<String, Object> record, String key) {
        Object value = record.get(key);
        return value == null ? "" : String.valueOf(value);
    }

    private BigDecimal amount(Map<String, Object> record) {
        Object value = record.get("amount");
        if (value == null || String.valueOf(value).trim().isEmpty()) {
            return BigDecimal.ZERO;
        }
        return new BigDecimal(String.valueOf(value));
    }
}
