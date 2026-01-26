package com.example.demo.security;

import com.example.demo.model.LoginType;
import com.example.demo.service.SessionCoordinator;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;
    private final SessionCoordinator sessionCoordinator;

    public JwtAuthFilter(JwtProvider jwtProvider, SessionCoordinator sessionCoordinator) {
        this.jwtProvider = jwtProvider;
        this.sessionCoordinator = sessionCoordinator;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String auth = request.getHeader("Authorization");
        if (auth == null || !auth.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        Claims claims = jwtProvider.parse(auth.substring(7));
        String userId = claims.getSubject();
        LoginType type = LoginType.valueOf(claims.get("loginType", String.class));
        String sessionId = claims.get("sessionId", String.class);

        if (!sessionCoordinator.validate(userId, type, sessionId)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        filterChain.doFilter(request, response);
    }
}

