package com.aps.dto.request.auth;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;  // 改这里

@Data
public class LoginRequest {
    @NotBlank(message = "用户名不能为空")
    private String username;

    @NotBlank(message = "密码不能为空")
    private String password;
}
