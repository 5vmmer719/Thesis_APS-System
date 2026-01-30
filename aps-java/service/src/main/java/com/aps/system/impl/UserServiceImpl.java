package com.aps.system.impl;


import com.aps.dto.request.system.*;
import com.aps.dto.response.system.UserDTO;
import com.aps.entity.system.SysUser;
import com.aps.exception.BusinessException;
import com.aps.mapper.system.SysUserMapper;
import com.aps.response.PageResult;
import com.aps.system.UserService;

import com.aps.utils.PasswordUtil;
import com.aps.utils.RequestUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final SysUserMapper userMapper;

    @Override
    public PageResult<UserDTO> listUsers(UserQueryRequest request) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getDeleted, 0);

        if (StringUtils.hasText(request.getUsername())) {
            wrapper.like(SysUser::getUsername, request.getUsername());
        }
        if (StringUtils.hasText(request.getRealName())) {
            wrapper.like(SysUser::getRealName, request.getRealName());
        }
        if (StringUtils.hasText(request.getRole())) {
            wrapper.eq(SysUser::getRole, request.getRole());
        }
        if (request.getStatus() != null) {
            wrapper.eq(SysUser::getStatus, request.getStatus());
        }

        wrapper.orderByDesc(SysUser::getCreatedTime);

        Page<SysUser> page = new Page<>(request.getPageNum(), request.getPageSize());
        Page<SysUser> resultPage = userMapper.selectPage(page, wrapper);

        List<UserDTO> userDTOS = resultPage.getRecords().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return new PageResult<>(
                userDTOS,
                resultPage.getTotal(),
                request.getPageNum(),
                request.getPageSize()
        );
    }

    @Override
    public UserDTO getUserById(Long id) {
        SysUser user = userMapper.selectById(id);
        if (user == null || user.getDeleted() == 1) {
            throw new BusinessException(40401, "用户不存在");
        }
        return convertToDTO(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createUser(UserCreateRequest request) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getUsername, request.getUsername())
                .eq(SysUser::getDeleted, 0);

        if (userMapper.selectCount(wrapper) > 0) {
            throw new BusinessException(40001, "用户名已存在");
        }

        if (StringUtils.hasText(request.getMobile())) {
            wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(SysUser::getMobile, request.getMobile())
                    .eq(SysUser::getDeleted, 0);
            if (userMapper.selectCount(wrapper) > 0) {
                throw new BusinessException(40001, "手机号已被使用");
            }
        }

        if (StringUtils.hasText(request.getEmail())) {
            wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(SysUser::getEmail, request.getEmail())
                    .eq(SysUser::getDeleted, 0);
            if (userMapper.selectCount(wrapper) > 0) {
                throw new BusinessException(40001, "邮箱已被使用");
            }
        }

        SysUser user = new SysUser();
        BeanUtils.copyProperties(request, user);
        user.setPasswordHash(PasswordUtil.encode(request.getPassword()));
        user.setDeleted(0);

        userMapper.insert(user);

        log.info("创建用户成功: id={}, username={}, 操作人={}",
                user.getId(), user.getUsername(), RequestUtil.getUsername());

        return user.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateUser(UserUpdateRequest request) {
        SysUser user = userMapper.selectById(request.getId());
        if (user == null || user.getDeleted() == 1) {
            throw new BusinessException(40401, "用户不存在");
        }

        if ("admin".equals(user.getUsername()) && request.getRole() != null
                && !"admin".equals(request.getRole())) {
            throw new BusinessException(40003, "不能修改超级管理员的角色");
        }

        if (StringUtils.hasText(request.getMobile())
                && !request.getMobile().equals(user.getMobile())) {
            LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(SysUser::getMobile, request.getMobile())
                    .eq(SysUser::getDeleted, 0)
                    .ne(SysUser::getId, request.getId());
            if (userMapper.selectCount(wrapper) > 0) {
                throw new BusinessException(40001, "手机号已被使用");
            }
        }

        if (StringUtils.hasText(request.getEmail())
                && !request.getEmail().equals(user.getEmail())) {
            LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(SysUser::getEmail, request.getEmail())
                    .eq(SysUser::getDeleted, 0)
                    .ne(SysUser::getId, request.getId());
            if (userMapper.selectCount(wrapper) > 0) {
                throw new BusinessException(40001, "邮箱已被使用");
            }
        }

        if (StringUtils.hasText(request.getRole())) {
            user.setRole(request.getRole());
        }
        if (StringUtils.hasText(request.getRealName())) {
            user.setRealName(request.getRealName());
        }
        if (StringUtils.hasText(request.getMobile())) {
            user.setMobile(request.getMobile());
        }
        if (StringUtils.hasText(request.getEmail())) {
            user.setEmail(request.getEmail());
        }
        if (request.getStatus() != null) {
            user.setStatus(request.getStatus());
        }

        userMapper.updateById(user);

        log.info("更新用户成功: id={}, username={}, 操作人={}",
                user.getId(), user.getUsername(), RequestUtil.getUsername());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteUser(Long id) {
        log.info("===== 开始删除用户: id={} =====", id);

        SysUser user = userMapper.selectById(id);
        if (user == null || user.getDeleted() == 1) {
            throw new BusinessException(40401, "用户不存在");
        }

        if ("admin".equals(user.getUsername())) {
            throw new BusinessException(40003, "不能删除超级管理员");
        }

        if (user.getId().equals(RequestUtil.getUserId())) {
            throw new BusinessException(40003, "不能删除自己");
        }

        log.info("构建 UpdateWrapper 进行逻辑删除");

        LambdaUpdateWrapper<SysUser> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(SysUser::getId, id)
                .set(SysUser::getDeleted, 1)
                .set(SysUser::getUpdatedBy, RequestUtil.getUsername())
                .set(SysUser::getUpdatedTime, LocalDateTime.now());

        int rows = userMapper.update(null, wrapper);

        log.info("删除用户成功: id={}, username={}, 影响行数={}, 操作人={}",
                user.getId(), user.getUsername(), rows, RequestUtil.getUsername());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void changePassword(ChangePasswordRequest request) {
        // 检查新密码和确认密码是否一致
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new BusinessException(40000, "两次输入的密码不一致");
        }

        // 检查新旧密码是否相同
        if (request.getOldPassword().equals(request.getNewPassword())) {
            throw new BusinessException(40000, "新密码不能与旧密码相同");
        }

        // 获取当前用户
        Long userId = RequestUtil.getUserId();
        SysUser user = userMapper.selectById(userId);
        if (user == null || user.getDeleted() == 1) {
            throw new BusinessException(40401, "用户不存在");
        }

        // 验证旧密码
        if (!PasswordUtil.matches(request.getOldPassword(), user.getPasswordHash())) {
            throw new BusinessException(40103, "旧密码错误");
        }

        // 更新密码
        user.setPasswordHash(PasswordUtil.encode(request.getNewPassword()));
        userMapper.updateById(user);

        log.info("修改密码成功: userId={}, username={}", userId, user.getUsername());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resetPassword(ResetPasswordRequest request) {
        // 检查目标用户是否存在
        SysUser user = userMapper.selectById(request.getUserId());
        if (user == null || user.getDeleted() == 1) {
            throw new BusinessException(40401, "用户不存在");
        }

        // 检查当前用户角色（只有管理员可以重置密码）
        String currentRole = RequestUtil.getRole();
        if (!"admin".equals(currentRole)) {
            throw new BusinessException(40300, "无权限执行此操作");
        }

        // 不能重置超级管理员的密码
        if ("admin".equals(user.getUsername()) && !user.getId().equals(RequestUtil.getUserId())) {
            throw new BusinessException(40003, "不能重置超级管理员的密码");
        }

        // 更新密码
        user.setPasswordHash(PasswordUtil.encode(request.getNewPassword()));
        userMapper.updateById(user);

        log.info("重置密码成功: userId={}, username={}, 操作人={}",
                user.getId(), user.getUsername(), RequestUtil.getUsername());
    }



    private UserDTO convertToDTO(SysUser user) {
        UserDTO dto = new UserDTO();
        BeanUtils.copyProperties(user, dto);
        return dto;
    }
}
