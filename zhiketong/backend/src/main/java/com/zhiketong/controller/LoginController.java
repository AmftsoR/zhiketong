package com.zhiketong.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zhiketong.common.R;
import com.zhiketong.config.TokenManager;
import com.zhiketong.entity.User;
import com.zhiketong.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class LoginController {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private TokenManager tokenManager;

    /**
     * 登录
     * POST /api/login
     * 请求体: {"username": "test_student", "password": "123456"}
     */
    @PostMapping("/login")
    public R<Map<String, Object>> login(@RequestBody Map<String, String> payload) {
        String username = payload.get("username");
        String password = payload.get("password");

        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            return R.fail("用户名和密码不能为空");
        }

        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username);
        User user = userMapper.selectOne(wrapper);

        if (user == null) {
            return R.fail("用户不存在");
        }

        if (!password.equals(user.getPassword())) {
            return R.fail("密码错误");
        }

        String token = tokenManager.createToken(user.getId());

        Map<String, Object> result = Map.of(
            "token", token,
            "user", Map.of(
                "id", user.getId(),
                "username", user.getUsername(),
                "realName", user.getRealName() != null ? user.getRealName() : "",
                "className", user.getClassName() != null ? user.getClassName() : "",
                "role", user.getRole()
            )
        );

        return R.ok(result);
    }

    /**
     * 获取当前登录用户信息
     * GET /api/user/current
     */
    @GetMapping("/user/current")
    public R<Map<String, Object>> currentUser(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        String token = extractToken(authHeader);
        Long userId = tokenManager.getUserId(token);
        if (userId == null) {
            return R.fail(401, "未登录或 token 已失效");
        }

        User user = userMapper.selectById(userId);
        if (user == null) {
            return R.fail("用户不存在");
        }

        Map<String, Object> userInfo = Map.of(
            "id", user.getId(),
            "username", user.getUsername(),
            "realName", user.getRealName() != null ? user.getRealName() : "",
            "className", user.getClassName() != null ? user.getClassName() : "",
            "role", user.getRole()
        );

        return R.ok(userInfo);
    }

    /**
     * 解析当前用户 ID（供其他 Controller 调用）
     */
    public Long resolveUserId(String authHeader) {
        String token = extractToken(authHeader);
        Long userId = tokenManager.getUserId(token);
        return userId != null ? userId : 1L; // 兼容旧代码，未登录默认返回 1
    }

    private String extractToken(String authHeader) {
        if (authHeader == null || authHeader.isBlank()) return null;
        if (authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return authHeader;
    }
}
