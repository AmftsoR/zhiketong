package com.zhiketong.interceptor;

import com.zhiketong.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 登录拦截器 — 从 Authorization Header 提取 JWT 并注入 userId
 */
public class LoginInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                             Object handler) {
        // OPTIONS 预检直接放行
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.setStatus(401);
            return false;
        }

        String token = authHeader.substring(7);
        if (!JwtUtil.validate(token)) {
            response.setStatus(401);
            return false;
        }

        // 将 userId 和 role 注入 request attribute，Controller 可通过 request.getAttribute 获取
        request.setAttribute("userId", JwtUtil.getUserId(token));
        request.setAttribute("role", JwtUtil.getRole(token));
        return true;
    }
}
