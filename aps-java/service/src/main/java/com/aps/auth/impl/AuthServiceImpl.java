package com.aps.auth.impl;




import com.aps.auth.AuthService;
import com.aps.dto.request.auth.LoginRequest;
import com.aps.dto.response.auth.LoginResponse;
import com.aps.entity.system.SysUser;
import com.aps.exception.BusinessException;
import com.aps.mapper.system.SysUserMapper;
import com.aps.utils.JwtUtil;
import com.aps.utils.PasswordUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final SysUserMapper userMapper;
    private final JwtUtil jwtUtil;

    @Override
    public LoginResponse login(LoginRequest request) {
        // 查询用户
        SysUser user = userMapper.selectOne(
                new LambdaQueryWrapper<SysUser>()
                        .eq(SysUser::getUsername, request.getUsername())
                        .eq(SysUser::getDeleted, 0)
        );

        if (user == null) {
            throw new BusinessException(40101, "用户名或密码错误");
        }

        if (user.getStatus() != 1) {
            throw new BusinessException(40102, "用户已被禁用");
        }

        // 验证密码
        if (!PasswordUtil.matches(request.getPassword(), user.getPasswordHash())) {
            throw new BusinessException(40101, "用户名或密码错误");
        }

        // 更新登录时间
        user.setLastLoginTime(LocalDateTime.now());
        userMapper.updateById(user);

        // 生成Token
        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());

        LoginResponse response = new LoginResponse();
        response.setToken(token);
        response.setExpiresIn(7200);
        response.setRole(user.getRole());
        response.setUserId(user.getId());
        return response;
    }
}

