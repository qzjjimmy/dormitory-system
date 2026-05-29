package com.bishe.dormitory.controller;

import com.bishe.dormitory.common.ApiResponse;
import com.bishe.dormitory.service.RecordService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/records")
public class RecordController {
    private final RecordService recordService;

    public RecordController(RecordService recordService) {
        this.recordService = recordService;
    }

    @GetMapping
    public ApiResponse<Map<String, Object>> list(@RequestParam String category,
                                                  @RequestParam(required = false, defaultValue = "") String keyword,
                                                  @RequestParam(required = false, defaultValue = "1") int page,
                                                  @RequestParam(required = false, defaultValue = "20") int size) {
        return ApiResponse.ok(recordService.list(category, keyword, page, size));
    }

    @PostMapping
    public ApiResponse<String> create(@RequestBody Map<String, Object> record) {
        recordService.validateRecord(record);
        recordService.create(record);
        return ApiResponse.ok("created");
    }

    @PutMapping("/{id}")
    public ApiResponse<String> update(@PathVariable Long id, @RequestBody Map<String, Object> record) {
        recordService.validateRecord(record);
        recordService.update(id, record);
        return ApiResponse.ok("updated");
    }

    @DeleteMapping("/{id}")
    public ApiResponse<String> delete(@PathVariable Long id) {
        recordService.delete(id);
        return ApiResponse.ok("deleted");
    }
}
