package com.bishe.dormitory.controller;

import com.bishe.dormitory.common.ApiResponse;
import com.bishe.dormitory.service.RecordService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {
    private final RecordService recordService;

    public DashboardController(RecordService recordService) {
        this.recordService = recordService;
    }

    @GetMapping
    public ApiResponse<Map<String, Object>> dashboard() {
        return ApiResponse.ok(recordService.dashboard());
    }
}
