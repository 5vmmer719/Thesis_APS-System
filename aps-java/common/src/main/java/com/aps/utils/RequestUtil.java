
package com.aps.utils;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * 请求工具类
 */
public class RequestUtil {

    private static final String USER_ID_ATTR = "userId";
    private static final String USERNAME_ATTR = "username";
    private static final String ROLE_ATTR = "role";

    /**
     * 获取当前请求
     */
    public static HttpServletRequest getRequest() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attributes != null ? attributes.getRequest() : null;
    }

    /**
     * 获取当前用户ID
     */
    public static Long getUserId() {
        HttpServletRequest request = getRequest();
        if (request == null) {
            return null;
        }
        Object userId = request.getAttribute(USER_ID_ATTR);
        return userId != null ? (Long) userId : null;
    }

    /**
     * 获取当前用户名
     */
    public static String getUsername() {
        HttpServletRequest request = getRequest();
        if (request == null) {
            return null;
        }
        Object username = request.getAttribute(USERNAME_ATTR);
        return username != null ? (String) username : null;
    }

    /**
     * 获取当前用户角色
     */
    public static String getRole() {
        HttpServletRequest request = getRequest();
        if (request == null) {
            return null;
        }
        Object role = request.getAttribute(ROLE_ATTR);
        return role != null ? (String) role : null;
    }

    /**
     * 获取客户端IP
     */
    public static String getClientIp() {
        HttpServletRequest request = getRequest();
        if (request == null) {
            return null;
        }

        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }

        // 多级代理情况，取第一个IP
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }

        return ip;
    }
}
