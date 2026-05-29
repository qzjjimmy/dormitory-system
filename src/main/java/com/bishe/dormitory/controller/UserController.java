package com.bishe.dormitory.controller;

import com.bishe.dormitory.common.ApiResponse;
import com.bishe.dormitory.service.UserService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ApiResponse<Map<String, Object>> list(
            @RequestParam(required = false, defaultValue = "") String keyword,
            @RequestParam(required = false, defaultValue = "1") int page,
            @RequestParam(required = false, defaultValue = "20") int size) {
        return ApiResponse.ok(userService.list(keyword, page, size));
    }

    @PostMapping
    public ApiResponse<String> create(@RequestBody Map<String, String> user) {
        userService.validateUser(user, true);
        userService.create(user);
        return ApiResponse.ok("created");
    }

    @PutMapping("/{id}")
    public ApiResponse<String> update(@PathVariable Long id, @RequestBody Map<String, String> user) {
        userService.validateUser(user, false);
        userService.update(id, user);
        return ApiResponse.ok("updated");
    }

    @DeleteMapping("/{id}")
    public ApiResponse<String> delete(@PathVariable Long id) {
        userService.delete(id);
        return ApiResponse.ok("deleted");
    }

    @PutMapping("/profile")
    public ApiResponse<String> updateProfile(@RequestBody Map<String, String> profile, HttpServletRequest request) {
        @SuppressWarnings("unchecked")
        Map<String, Object> currentUser = (Map<String, Object>) request.getAttribute("currentUser");
        String role = (String) currentUser.get("role");
        if (!"student".equals(role)) {
            return ApiResponse.fail("仅学生可更新个人特征");
        }
        Long userId = ((Number) currentUser.get("id")).longValue();
        userService.updateProfile(userId, profile);
        return ApiResponse.ok("特征更新成功");
    }

    @PutMapping("/password")
    public ApiResponse<String> updatePassword(@RequestBody Map<String, String> body, HttpServletRequest request) {
        @SuppressWarnings("unchecked")
        Map<String, Object> currentUser = (Map<String, Object>) request.getAttribute("currentUser");
        Long userId = ((Number) currentUser.get("id")).longValue();
        String oldPassword = body.get("oldPassword");
        String newPassword = body.get("newPassword");
        if (oldPassword == null || oldPassword.trim().isEmpty()
                || newPassword == null || newPassword.trim().isEmpty()) {
            return ApiResponse.fail("密码不能为空");
        }
        if (newPassword.length() < 6) {
            return ApiResponse.fail("新密码至少6位");
        }
        userService.updatePassword(userId, oldPassword, newPassword);
        return ApiResponse.ok("密码修改成功");
    }
}
