package com.bishe.dormitory.controller;

import com.bishe.dormitory.common.ApiResponse;
import com.bishe.dormitory.service.RoomAssignmentService;
import com.bishe.dormitory.websocket.ChatWebSocketHandler;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/assignment")
public class AssignmentController {
    private final RoomAssignmentService roomAssignmentService;
    private final ChatWebSocketHandler wsHandler;

    public AssignmentController(RoomAssignmentService roomAssignmentService, ChatWebSocketHandler wsHandler) {
        this.roomAssignmentService = roomAssignmentService;
        this.wsHandler = wsHandler;
    }

    @GetMapping("/heatmap")
    public ApiResponse<Map<String, Object>> heatmap() {
        return ApiResponse.ok(roomAssignmentService.computeHeatmap());
    }

    @GetMapping("/rooms")
    public ApiResponse<Map<String, Object>> rooms() {
        return ApiResponse.ok(roomAssignmentService.getAllRooms());
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

        // Push notification to each assigned student via WebSocket
        for (Map<String, Object> a : assignments) {
            Long studentId = ((Number) a.get("studentId")).longValue();
            String roomNo = (String) a.get("roomNo");
            String bedNo = (String) a.get("bedNo");
            Map<String, Object> payload = new HashMap<>();
            payload.put("type", "dorm_assigned");
            payload.put("message", "你的宿舍已分配：" + roomNo + " · " + bedNo);
            payload.put("roomNo", roomNo);
            payload.put("bedNo", bedNo);
            wsHandler.pushToUser(studentId, payload);
        }

        return ApiResponse.ok("分配已确认");
    }
}
