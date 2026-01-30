package com.aps.controller.system;


import com.aps.dto.request.system.ResetPasswordRequest;
import com.aps.dto.request.system.UserCreateRequest;
import com.aps.dto.request.system.UserQueryRequest;
import com.aps.dto.request.system.UserUpdateRequest;
import com.aps.dto.response.system.UserDTO;
import com.aps.response.ApiResponse;
import com.aps.response.PageResult;
import com.aps.system.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(name = "用户管理")
@RestController
@RequestMapping("/system/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "分页查询用户列表")
    @GetMapping("/list")
    public ApiResponse<PageResult<UserDTO>> listUsers(UserQueryRequest request) {
        PageResult<UserDTO> result = userService.listUsers(request);
        return ApiResponse.success(result);
    }

    @Operation(summary = "根据ID获取用户详情")
    @GetMapping("/{id}")
    public ApiResponse<UserDTO> getUserById(@PathVariable Long id) {
        UserDTO user = userService.getUserById(id);
        return ApiResponse.success(user);
    }

    @Operation(summary = "创建用户")
    @PostMapping
    public ApiResponse<Long> createUser(@Validated @RequestBody UserCreateRequest request) {
        Long userId = userService.createUser(request);
        return ApiResponse.success(userId);
    }

    @Operation(summary = "更新用户")
    @PutMapping
    public ApiResponse<Void> updateUser(@Validated @RequestBody UserUpdateRequest request) {
        userService.updateUser(request);
        return ApiResponse.success();
    }

    @Operation(summary = "删除用户")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ApiResponse.success();
    }

    @Operation(summary = "重置密码（管理员）")
    @PostMapping("/reset-password")
    public ApiResponse<Void> resetPassword(@Validated @RequestBody ResetPasswordRequest request) {
        userService.resetPassword(request);
        return ApiResponse.success();
    }


}
