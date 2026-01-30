package com.aps.entity.system;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("sys_user")
public class SysUser {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private String username;
    private String passwordHash;
    private String role;
    private String realName;
    private String mobile;
    private String email;
    private Integer status;
    private LocalDateTime lastLoginTime;
    private LocalDateTime createdTime;
    private String createdBy;
    private LocalDateTime updatedTime;
    private String updatedBy;
    private Integer deleted;
}
