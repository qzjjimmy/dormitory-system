package com.bishe.dormitory.controller;

import com.bishe.dormitory.common.ApiResponse;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class HealthController {
    private final JdbcTemplate jdbcTemplate;

    public HealthController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping("/api/health")
    public ApiResponse<Map<String, Object>> health() {
        Map<String, Object> info = new HashMap<>();
        try {
            jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            info.put("db", "connected");
        } catch (Exception e) {
            info.put("db", "disconnected: " + e.getMessage());
        }
        info.put("status", "running");
        info.put("timestamp", System.currentTimeMillis());
        return ApiResponse.ok(info);
    }
}
