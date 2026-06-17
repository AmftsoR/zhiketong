package com.zhiketong.controller;

import com.zhiketong.common.R;
import com.zhiketong.entity.User;
import com.zhiketong.service.UserService;
import com.zhiketong.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * 用户相关接口
 */
@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 登录
     * POST /api/user/login
     * 请求体: { "username": "xxx", "password": "xxx" }
     */
    @PostMapping("/login")
    public R<Map<String, Object>> login(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");
        User user = userService.login(username, password);
        if (user != null) {
            String token = JwtUtil.generate(user.getId(), user.getUsername(), user.getRole());
            return R.ok(Map.of(
                    "token", token,
                    "user", user
            ));
        }
        return R.fail("用户名或密码错误");
    }

    /**
     * 注册
     * POST /api/user/register
     * 请求体: { "username": "xxx", "password": "xxx", "realName": "张三", "role": "student" }
     */
    @PostMapping("/register")
    public R<String> register(@RequestBody User user) {
        // 检查用户名是否已存在
        if (userService.lambdaQuery().eq(User::getUsername, user.getUsername()).count() > 0) {
            return R.fail("用户名已存在");
        }
        boolean success = userService.register(user);
        return success ? R.ok("注册成功") : R.fail("注册失败");
    }

    /**
     * 获取当前登录用户信息（JWT）
     * GET /api/user/current — 由 LoginInterceptor 验证 JWT 并注入 userId
     */
    @GetMapping("/current")
    public R<Map<String, Object>> getCurrentUser(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return R.fail(401, "未登录");
        }
        User user = userService.getById(userId);
        if (user == null) {
            return R.fail(404, "用户不存在");
        }
        user.setPassword(null);
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
     * 根据 ID 获取用户信息
     * GET /api/user/{id}
     */
    @GetMapping("/{id}")
    public R<User> getUserById(@PathVariable Long id) {
        User user = userService.getById(id);
        if (user != null) {
            user.setPassword(null);
            return R.ok(user);
        }
        return R.fail("用户不存在");
    }
}
