package com.bishe.dormitory.controller;

import com.bishe.dormitory.common.ApiResponse;
import com.bishe.dormitory.service.ChatService;
import com.bishe.dormitory.websocket.ChatWebSocketHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chat")
public class ChatController {
    private final ChatService chatService;
    private final ChatWebSocketHandler wsHandler;

    public ChatController(ChatService chatService, ChatWebSocketHandler wsHandler) {
        this.chatService = chatService;
        this.wsHandler = wsHandler;
    }

    @GetMapping("/contacts")
    public ApiResponse<List<Map<String, Object>>> contacts(@RequestParam Long userId) {
        return ApiResponse.ok(chatService.getContacts(userId));
    }

    @GetMapping("/messages")
    public ApiResponse<List<Map<String, Object>>> messages(
            @RequestParam Long userId,
            @RequestParam Long withUserId) {
        List<Map<String, Object>> msgs = chatService.getMessages(userId, withUserId);
        chatService.markAsRead(userId, withUserId);
        return ApiResponse.ok(msgs);
    }

    @GetMapping("/unread")
    public ApiResponse<Map<String, Object>> unreadCount(@RequestParam Long userId) {
        return ApiResponse.ok(chatService.getUnreadCounts(userId));
    }

    @PostMapping("/send")
    public ApiResponse<Map<String, Object>> send(@RequestBody Map<String, Object> body) {
        long fromUserId = ((Number) body.get("fromUserId")).longValue();
        long toUserId = ((Number) body.get("toUserId")).longValue();
        String content = (String) body.get("content");

        if (content == null || content.trim().isEmpty()) {
            return ApiResponse.fail("消息内容不能为空");
        }

        Map<String, Object> msg = chatService.sendMessage(fromUserId, toUserId, content);

        // Push via WebSocket to recipient if online
        Map<String, Object> pushPayload = new java.util.HashMap<>();
        pushPayload.put("type", "new_message");
        pushPayload.put("message", msg);
        wsHandler.pushToUser(toUserId, pushPayload);

        return ApiResponse.ok(msg);
    }
}
