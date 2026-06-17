package com.zhiketong.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT 工具类 — 生成和验证 Token
 */
public class JwtUtil {

    // 生产环境应放在配置文件
    private static final String SECRET = "zhiketong-jwt-secret-key-2024-this-is-32-bytes!!";
    private static final long EXPIRE_MS = 7 * 24 * 60 * 60 * 1000L; // 7 天

    private static SecretKey getKey() {
        return Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 生成 JWT token
     * @param userId   用户 ID
     * @param username 用户名
     * @param role     角色
     */
    public static String generate(Long userId, String username, String role) {
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("username", username)
                .claim("role", role)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + EXPIRE_MS))
                .signWith(getKey())
                .compact();
    }

    /**
     * 解析 token 中的 Claims
     */
    public static Claims parse(String token) {
        return Jwts.parser()
                .verifyWith(getKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * 提取 userId
     */
    public static Long getUserId(String token) {
        return Long.valueOf(parse(token).getSubject());
    }

    /**
     * 提取 role
     */
    public static String getRole(String token) {
        return parse(token).get("role", String.class);
    }

    /**
     * 验证 token 是否有效
     */
    public static boolean validate(String token) {
        try {
            parse(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
