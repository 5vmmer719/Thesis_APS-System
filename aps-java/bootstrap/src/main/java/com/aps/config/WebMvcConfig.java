package com.aps.config;

import com.aps.interceptor.AuthInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final AuthInterceptor authInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/auth/login",           // 登录接口
                        "/auth/register",        // 注册接口
                        "/grpc-test/**",         // gRPC 测试接口（开发测试用）
                        "/doc.html",             // Swagger UI
                        "/swagger-resources/**", // Swagger 资源
                        "/v3/api-docs/**",       // Swagger API 文档
                        "/webjars/**",           // Swagger 静态资源
                        "/favicon.ico",          // 网站图标
                        "/error"                 // 错误页面
                );
    }
}

