package com.zhiketong.config;

import org.springframework.stereotype.Component;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 简易 Token 管理器 — 内存存储，重启失效
 */
@Component
public class TokenManager {

    private final Map<String, Long> tokenStore = new ConcurrentHashMap<>();

    /**
     * 创建 token 并绑定用户 ID
     */
    public String createToken(Long userId) {
        String token = UUID.randomUUID().toString().replace("-", "");
        tokenStore.put(token, userId);
        return token;
    }

    /**
     * 根据 token 获取用户 ID，null 表示无效/过期
     */
    public Long getUserId(String token) {
        if (token == null || token.isBlank()) return null;
        return tokenStore.get(token);
    }

    /**
     * 移除 token（登出）
     */
    public void removeToken(String token) {
        if (token != null) tokenStore.remove(token);
    }
}
