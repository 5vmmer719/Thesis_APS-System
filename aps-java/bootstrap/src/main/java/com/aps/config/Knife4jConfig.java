package com.aps.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Knife4jConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("APS排产系统 API")
                        .description("汽车制造APS排产系统接口文档")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("APS Team")
                                .email("aps@example.com")))
                // 配置 JWT 认证
                .components(new Components()
                        .addSecuritySchemes("JWT",
                                new SecurityScheme()
                                        .name("Authorization")
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .in(SecurityScheme.In.HEADER)
                                        .description("请输入 JWT Token（无需添加 Bearer 前缀）")))
                // 全局应用 JWT 认证
                .addSecurityItem(new SecurityRequirement().addList("JWT"));
    }
}
