package com.aps.controller.auth;


import com.aps.auth.AuthService;
import com.aps.dto.request.auth.LoginRequest;
import com.aps.dto.request.system.ChangePasswordRequest;
import com.aps.dto.response.auth.LoginResponse;
import com.aps.dto.response.system.UserDTO;
import com.aps.response.ApiResponse;
import com.aps.system.UserService;
import com.aps.utils.RequestUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(name = "认证管理")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final UserService userService;

    @Operation(summary = "用户登录")
    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Validated @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ApiResponse.success(response);
    }

    @Operation(summary = "获取当前用户信息")
    @GetMapping("/current")
    public ApiResponse<UserDTO> getCurrentUser() {
        Long userId = RequestUtil.getUserId();
        UserDTO user = userService.getUserById(userId);
        return ApiResponse.success(user);
    }

    @Operation(summary = "修改密码")
    @PostMapping("/change-password")
    public ApiResponse<Void> changePassword(@Validated @RequestBody ChangePasswordRequest request) {
        userService.changePassword(request);
        return ApiResponse.success();
    }

    @Operation(summary = "登出")
    @PostMapping("/logout")
    public ApiResponse<Void> logout() {
        // Token 无状态，客户端直接删除即可
        return ApiResponse.success();
    }
}
