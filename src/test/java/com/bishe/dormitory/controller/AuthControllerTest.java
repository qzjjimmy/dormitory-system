package com.bishe.dormitory.controller;

import com.bishe.dormitory.common.TokenStore;
import com.bishe.dormitory.service.RecordService;
import com.bishe.dormitory.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = {AuthController.class, RecordController.class})
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private RecordService recordService;

    @MockBean
    private TokenStore tokenStore;

    private Map<String, Object> mockUser;
    private String mockToken;

    @BeforeEach
    void setUp() {
        mockUser = new HashMap<>();
        mockUser.put("id", 2L);
        mockUser.put("username", "student");
        mockUser.put("realName", "测试学生");
        mockUser.put("role", "student");
        mockUser.put("phone", "13800000001");
        mockUser.put("roomNo", "芙蓉楼3 · 519室 · 2号床");

        mockToken = "test-token-12345";
    }

    @Test
    void loginSuccess() throws Exception {
        when(userService.login("student", "123456")).thenReturn(mockUser);
        when(tokenStore.createToken(any())).thenReturn(mockToken);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"student\",\"password\":\"123456\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.token").value(mockToken))
                .andExpect(jsonPath("$.data.username").value("student"));
    }

    @Test
    void loginFail() throws Exception {
        when(userService.login("student", "wrong"))
                .thenThrow(new IllegalArgumentException("账号或密码错误"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"student\",\"password\":\"wrong\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void loginEmptyCredentials() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"\",\"password\":\"\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("用户名和密码不能为空"));
    }

    @Test
    void recordsList() throws Exception {
        List<Map<String, Object>> rows = new ArrayList<>();
        Map<String, Object> record = new HashMap<>();
        record.put("id", 1);
        record.put("title", "测试记录");
        record.put("category", "repair");
        record.put("status", "待处理");
        rows.add(record);

        Map<String, Object> pageResult = new HashMap<>();
        pageResult.put("rows", rows);
        pageResult.put("total", 1L);
        pageResult.put("page", 1);
        pageResult.put("size", 20);
        pageResult.put("pages", 1);

        when(recordService.list(eq("repair"), eq(""), eq(1), eq(20))).thenReturn(pageResult);
        when(tokenStore.getUser(mockToken)).thenReturn(mockUser);

        mockMvc.perform(get("/api/records")
                        .param("category", "repair")
                        .header("Authorization", "Bearer " + mockToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.rows[0].title").value("测试记录"))
                .andExpect(jsonPath("$.data.total").value(1));
    }

    @Test
    void createRecord() throws Exception {
        when(tokenStore.getUser(mockToken)).thenReturn(mockUser);

        mockMvc.perform(post("/api/records")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + mockToken)
                        .content("{\"category\":\"repair\",\"title\":\"测试报修\",\"owner\":\"测试学生\",\"location\":\"芙蓉楼3-519\",\"amount\":0,\"status\":\"待处理\",\"content\":\"单元测试创建的记录\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value("created"));
    }

    @Test
    void deleteRecord() throws Exception {
        when(tokenStore.getUser(mockToken)).thenReturn(mockUser);

        mockMvc.perform(delete("/api/records/1")
                        .header("Authorization", "Bearer " + mockToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value("deleted"));
    }

    @Test
    void unauthorizedRequest() throws Exception {
        mockMvc.perform(get("/api/records")
                        .param("category", "repair"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false));
    }
}
