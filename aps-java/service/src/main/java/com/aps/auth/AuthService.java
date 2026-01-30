package com.aps.auth;


import com.aps.dto.request.auth.LoginRequest;
import com.aps.dto.response.auth.LoginResponse;

public interface AuthService {
    LoginResponse login(LoginRequest request);
}

