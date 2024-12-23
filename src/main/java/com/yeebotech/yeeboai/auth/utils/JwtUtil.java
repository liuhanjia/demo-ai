package com.yeebotech.yeeboai.auth.utils;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.SignatureAlgorithm;
import java.security.Key;
import java.util.Date;

public class JwtUtil {

    // 从环境变量获取 JWT 密钥
    private static final String SECRET_KEY_STRING = "yeebotech-ai-256-bit-secret-key!!";
    private static final Key SECRET_KEY = Keys.hmacShaKeyFor(SECRET_KEY_STRING.getBytes());

    // 解析 JWT token 获取 Claims（payload 数据）
    public static Claims parseToken(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(SECRET_KEY) // 使用从环境变量读取的密钥
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            // 如果 token 无效或过期，将抛出异常
            System.err.println("解析 token 失败: " + e.getMessage());
            return null;
        }
    }

    // 获取 userID
    public static Long getUserIdFromToken(String token) {
        // 如果 token 包含 Bearer 前缀，去除它
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);  // 去除 "Bearer " 前缀
        }
        Claims claims = parseToken(token);
        if (claims != null) {
            // 通过 claims 获取 userID
            // 使用 Integer 类型，如果 userID 是 Integer 类型，直接转换
            Integer userId = claims.get("user_id", Integer.class);

            // 如果需要返回 Long 类型，可以在这里转换
            if (userId != null) {
                return Long.valueOf(userId);
            }
        }
        return null;
    }

}
