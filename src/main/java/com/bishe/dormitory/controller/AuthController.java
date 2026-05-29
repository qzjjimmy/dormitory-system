package com.bishe.dormitory.controller;

import com.bishe.dormitory.common.ApiResponse;
import com.bishe.dormitory.common.TokenStore;
import com.bishe.dormitory.service.UserService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final UserService userService;
    private final TokenStore tokenStore;

    public AuthController(UserService userService, TokenStore tokenStore) {
        this.userService = userService;
        this.tokenStore = tokenStore;
    }

    @PostMapping("/login")
    public ApiResponse<Map<String, Object>> login(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String password = request.get("password");
        if (username == null || username.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            return ApiResponse.fail("用户名和密码不能为空");
        }
        Map<String, Object> user = userService.login(username, password);
        String token = tokenStore.createToken(user);
        Map<String, Object> result = new HashMap<>(user);
        result.put("token", token);
        return ApiResponse.ok(result);
    }

    @PostMapping("/logout")
    public ApiResponse<String> logout(HttpServletRequest request) {
        String token = (String) request.getAttribute("token");
        if (token != null) {
            tokenStore.removeToken(token);
        }
        return ApiResponse.ok("logged out");
    }
}
