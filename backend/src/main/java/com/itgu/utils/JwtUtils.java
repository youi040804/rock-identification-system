package com.itgu.utils;

import com.itgu.Pojo.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;


import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class JwtUtils {

    // 固定长度安全 key，字节长度 ≥32
    private static final String SIGN_KEY = System.getenv("JWT_SECRET");
    private static final Long EXPIRE = 43200000L; // 12小时
    private static SecretKey SECRET_KEY;

    // 静态初始化块：生成 SecretKey 并打印 key 字节长度
static {
    if (SIGN_KEY == null || SIGN_KEY.isBlank()) {
        throw new IllegalStateException(
                "JWT_SECRET environment variable is required"
        );
    }

    byte[] keyBytes = SIGN_KEY.getBytes(StandardCharsets.UTF_8);

    if (keyBytes.length < 32) {
        throw new IllegalStateException(
                "JWT_SECRET must be at least 32 bytes"
        );
    }

    SECRET_KEY = Keys.hmacShaKeyFor(keyBytes);
}
    // 根据 Map 生成 JWT
    public static String getJwt(Map<String, Object> claims) {
        try {
            return Jwts.builder()
                    .addClaims(claims)
                    .signWith(SECRET_KEY, SignatureAlgorithm.HS256)
                    .setExpiration(new Date(System.currentTimeMillis() + EXPIRE))
                    .compact();
        } catch (Exception e) {
            System.err.println("生成 JWT 时出错: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    // 根据 User 对象生成 JWT
    public static String generateToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("id", user.getId());
        claims.put("username", user.getUsername());
        return getJwt(claims);
    }

    // 解析 JWT
    public static Claims parseJwt(String jwt) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(SECRET_KEY)
                    .build()
                    .parseClaimsJws(jwt)
                    .getBody();
        } catch (Exception e) {
            System.err.println("解析 JWT 时出错: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
}
