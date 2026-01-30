package com.aps.interceptor;


import com.aps.exception.BusinessException;
import com.aps.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuthInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;



    // 使用 request attribute 存储用户信息
    private static final String USER_ID_ATTR = "userId";
    private static final String USERNAME_ATTR = "username";
    private static final String ROLE_ATTR = "role";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 从请求头获取 token
        String token = request.getHeader("Authorization");

        if (!StringUtils.hasText(token)) {
            throw new BusinessException(40100, "未登录");
        }

        // 去掉 Bearer 前缀
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        // 验证 token
        if (!jwtUtil.validateToken(token)) {
            throw new BusinessException(40100, "Token无效或已过期");
        }

        // 解析 token
        try {
            Claims claims = jwtUtil.parseToken(token);
            Long userId = Long.parseLong(claims.getSubject());
            String username = claims.get("username", String.class);
            String role = claims.get("role", String.class);

            // 设置到 request attribute
            request.setAttribute(USER_ID_ATTR, userId);
            request.setAttribute(USERNAME_ATTR, username);
            request.setAttribute(ROLE_ATTR, role);

            log.debug("用户认证成功: userId={}, username={}, role={}", userId, username, role);
            return true;
        } catch (Exception e) {
            log.error("Token解析失败", e);
            throw new BusinessException(40100, "Token解析失败");
        }
    }
}

