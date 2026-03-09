package com.example.demo.security;

import com.example.demo.model.LoginType;
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
 * JWT 認證過濾器 (SSO + 多端互踢)
 *
 * 功能：
 * - 驗證 JWT 是否合法與未過期
 * - 透過 AuthService 檢查 sessionId 是否有效
 * - 支援多端互踢與單端登出
 */
@Component
public class AuthFilter extends OncePerRequestFilter {

    private final AuthService authService;
    private final JwtProvider jwtProvider;

    public AuthFilter(JwtProvider jwtProvider, AuthService authService) {
        this.jwtProvider = jwtProvider;
        this.authService = authService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String auth = request.getHeader("Authorization");

        if (auth != null && auth.startsWith("Bearer ")) {
            try {
                // 解析 JWT，檢查簽名與過期
                Claims claims = jwtProvider.parse(auth.substring(7));
                String userId = claims.getSubject();
                LoginType loginType = LoginType.valueOf(claims.get("loginType", String.class));
                String sessionId = claims.get("sessionId", String.class);

                // 檢查 session 是否仍然有效 (Redis / AuthService)
                if (!authService.isSessionValidBySessionId(userId, loginType, sessionId)) {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    return;
                }

                // 可將 userId / loginType 放入 request attribute，供 controller 使用
                request.setAttribute("userId", userId);
                request.setAttribute("loginType", loginType);

            } catch (Exception e) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }
        }

        filterChain.doFilter(request, response);
    }
}