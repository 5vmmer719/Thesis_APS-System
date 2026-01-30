package com.aps.system;


import com.aps.dto.request.system.*;
import com.aps.dto.response.system.UserDTO;
import com.aps.response.PageResult;

public interface UserService {

    /**
     * 分页查询用户列表
     */
    PageResult<UserDTO> listUsers(UserQueryRequest request);

    /**
     * 根据ID获取用户详情
     */
    UserDTO getUserById(Long id);

    /**
     * 创建用户
     */
    Long createUser(UserCreateRequest request);

    /**
     * 更新用户
     */
    void updateUser(UserUpdateRequest request);

    /**
     * 删除用户（逻辑删除）
     */
    void deleteUser(Long id);

    /**
     * 修改密码（用户自己）
     */
    void changePassword(ChangePasswordRequest request);

    /**
     * 重置密码（管理员）
     */
    void resetPassword(ResetPasswordRequest request);

}
