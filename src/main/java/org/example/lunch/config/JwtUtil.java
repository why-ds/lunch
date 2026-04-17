package org.example.lunch.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT 토큰 생성/검증 유틸
 */
@Component
public class JwtUtil {

    // 비밀키 (32자 이상)
    private static final String SECRET = "lunch-app-secret-key-must-be-32-chars!!";
    private static final long EXPIRATION = 1000 * 60 * 60 * 24; // 24시간
    private final SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));

    /** 토큰 생성 */
    public String generateToken(String userId, String role) {
        return Jwts.builder()
                .subject(userId)
                .claim("role", role)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION))
                .signWith(key)
                .compact();
    }

    /** 토큰에서 userId 추출 */
    public String getUserId(String token) {
        return getClaims(token).getSubject();
    }

    /** 토큰에서 role 추출 */
    public String getRole(String token) {
        return getClaims(token).get("role", String.class);
    }

    /** 토큰 유효성 검증 */
    public boolean validateToken(String token) {
        try {
            getClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}