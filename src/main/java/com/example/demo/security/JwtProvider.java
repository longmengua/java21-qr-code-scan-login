package com.example.demo.security;

import com.example.demo.config.JwtProperties;
import com.example.demo.model.LoginType;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Date;

@Component
public class JwtProvider {

    private final SecretKey key;
    private final long expirationMinutes;

    public JwtProvider(JwtProperties properties) {
        // 從 yml 讀取 secret
        byte[] decoded = Base64.getDecoder().decode(properties.getSecret());
        this.key = Keys.hmacShaKeyFor(decoded);
        this.expirationMinutes = properties.getExpirationInMinutes();
    }

    public String generate(String userId, LoginType type, String sessionId) {
        return Jwts.builder()
                .setSubject(userId)
                .claim("loginType", type.name())
                .claim("sessionId", sessionId)
                .setIssuedAt(new Date())
                .setExpiration(Date.from(Instant.now().plus(expirationMinutes, ChronoUnit.MINUTES)))
                .signWith(key)
                .compact();
    }

    public Claims parse(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
