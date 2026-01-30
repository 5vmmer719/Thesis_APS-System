package com.aps.dto.response.auth;

import lombok.Data;

@Data
public class LoginResponse {
    private String token;
    private Integer expiresIn;
    private String role;
    private Long userId;
}

