package com.example.demo.security;

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

    private final SecretKey key = Keys.hmacShaKeyFor(
            Base64.getDecoder().decode("YOUR_256_BIT_SECRET")
    );

    public String generate(String userId, LoginType type, String sessionId) {
        return Jwts.builder()
                .subject(userId)
                .claim("loginType", type.name())
                .claim("sessionId", sessionId)
                .issuedAt(new Date())
                .expiration(Date.from(Instant.now().plus(1, ChronoUnit.HOURS)))
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
