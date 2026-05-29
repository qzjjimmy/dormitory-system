package com.bishe.dormitory.controller;

import com.bishe.dormitory.common.ApiResponse;
import com.bishe.dormitory.service.RoomAssignmentService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/assignment")
public class AssignmentController {
    private final RoomAssignmentService roomAssignmentService;

    public AssignmentController(RoomAssignmentService roomAssignmentService) {
        this.roomAssignmentService = roomAssignmentService;
    }

    @PostMapping("/run")
    public ApiResponse<Map<String, Object>> runAssignment() {
        Map<String, Object> result = roomAssignmentService.executeAssignment();
        return ApiResponse.ok(result);
    }

    @GetMapping("/recommend/{studentId}")
    public ApiResponse<List<Map<String, Object>>> recommendTransfer(@PathVariable Long studentId) {
        return ApiResponse.ok(roomAssignmentService.recommendTransfer(studentId));
    }

    @PostMapping("/confirm")
    public ApiResponse<String> confirmAssignment(@RequestBody Map<String, Object> body) {
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> assignments = (List<Map<String, Object>>) body.get("assignments");
        roomAssignmentService.confirmAssignment(assignments);
        return ApiResponse.ok("分配已确认");
    }
}
