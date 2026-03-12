package com.example.demo.filters;

import com.example.demo.model.LoginType;
import com.example.demo.component.JwtProvider;
import com.example.demo.service.AuthService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT 認證過濾器 (無 Spring Security 版本)
 *
 * 功能：
 * - 驗證 JWT 是否合法與未過期
 * - 檢查 sessionId 是否有效 (Redis / AuthService)
 * - 支援單端登出 / 互踢
 */
@Component
public class AuthFilter extends OncePerRequestFilter {

    private final AuthService authService;
    private final JwtProvider jwtProvider;

    public AuthFilter(AuthService authService, JwtProvider jwtProvider) {
        this.authService = authService;
        this.jwtProvider = jwtProvider;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            try {
                String token = authHeader.substring(7);
                Claims claims = jwtProvider.parse(token);

                String userId = claims.getSubject();
                LoginType loginType = LoginType.valueOf(claims.get("loginType", String.class));
                String sessionId = claims.get("sessionId", String.class);

                // 驗證 session
                if (!authService.isSessionValidBySessionId(userId, loginType, sessionId)) {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType("application/json");
                    response.getWriter().write("{\"error\":\"token invalid or expired\"}");
                    return; // 停止 filter chain
                }

                // 放入 request attribute，controller 可用
                request.setAttribute("userId", userId);
                request.setAttribute("loginType", loginType);

            } catch (Exception ex) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json");
                response.getWriter().write("{\"error\":\"invalid token\"}");
                return; // 停止 filter chain
            }
        }

        filterChain.doFilter(request, response);
    }
}