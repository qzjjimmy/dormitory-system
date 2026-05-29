package com.bishe.dormitory.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.bishe.dormitory.common.ApiResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ai")
public class AiController {
    @Value("${deepseek.api-key:}")
    private String deepseekApiKey;

    @Value("${deepseek.base-url:https://api.deepseek.com}")
    private String deepseekBaseUrl;

    @Value("${deepseek.model:deepseek-chat}")
    private String deepseekModel;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @PostMapping("/chat")
    public ApiResponse<Map<String, String>> chat(@RequestBody Map<String, String> request) {
        String question = request.getOrDefault("question", "");
        String answer = callDeepSeek(question);
        Map<String, String> data = new HashMap<String, String>();
        data.put("answer", answer);
        return ApiResponse.ok(data);
    }

    private String callDeepSeek(String question) {
        if (deepseekApiKey == null || deepseekApiKey.trim().isEmpty()) {
            return fallbackAnswer(question);
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(deepseekApiKey.trim());

            Map<String, Object> payload = new HashMap<String, Object>();
            payload.put("model", deepseekModel);
            payload.put("temperature", 0.3);

            List<Map<String, String>> messages = new ArrayList<Map<String, String>>();
            Map<String, String> system = new HashMap<String, String>();
            system.put("role", "system");
            system.put("content", "你是学生宿舍管理系统的AI助手，只回答宿舍、报修、缴费、访客、调宿、卫生检查、晚归登记等校园宿舍事务问题。回答要简洁、明确、适合学生阅读。");
            messages.add(system);

            Map<String, String> user = new HashMap<String, String>();
            user.put("role", "user");
            user.put("content", question);
            messages.add(user);
            payload.put("messages", messages);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<Map<String, Object>>(payload, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(deepseekBaseUrl + "/chat/completions", entity, String.class);
            JsonNode root = objectMapper.readTree(response.getBody());
            return root.path("choices").path(0).path("message").path("content").asText(fallbackAnswer(question));
        } catch (Exception ex) {
            return "DeepSeek 暂时调用失败，已切换为本地回答。" + fallbackAnswer(question);
        }
    }

    private String fallbackAnswer(String question) {
        return "已收到你的宿舍问题：" + question + "。建议先确认宿舍号、事项类型和紧急程度；如涉及维修，请在报修申请中提交图片和详细位置，宿管员会在工作台派单处理。";
    }
}
