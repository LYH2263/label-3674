package com.label.community.security;

import com.label.community.common.BusinessException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

public final class JwtUtil {
    private static final long EXPIRE_MS = 1000L * 60 * 60 * 24;
    private static final String DEFAULT_SECRET = "smart-community-default-secret-3674-smart-community";

    private JwtUtil() {
    }

    private static SecretKey key() {
        String secret = System.getenv().getOrDefault("APP_JWT_SECRET", DEFAULT_SECRET);
        byte[] bytes = secret.getBytes(StandardCharsets.UTF_8);
        if (bytes.length < 32) {
            byte[] padded = new byte[32];
            System.arraycopy(bytes, 0, padded, 0, Math.min(bytes.length, 32));
            bytes = padded;
        }
        return Keys.hmacShaKeyFor(bytes);
    }

    public static String createToken(Long userId, String username, String role) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + EXPIRE_MS);
        return Jwts.builder()
            .setSubject(String.valueOf(userId))
            .claim("username", username)
            .claim("role", role)
            .setIssuedAt(now)
            .setExpiration(exp)
            .signWith(key(), SignatureAlgorithm.HS256)
            .compact();
    }

    public static AuthUser parse(String token) {
        try {
            Claims claims = Jwts.parserBuilder().setSigningKey(key()).build().parseClaimsJws(token).getBody();
            Long userId = Long.valueOf(claims.getSubject());
            String username = claims.get("username", String.class);
            String role = claims.get("role", String.class);
            return new AuthUser(userId, username, role);
        } catch (Exception ex) {
            throw new BusinessException(401, 40101, "登录状态无效，请重新登录");
        }
    }
}
