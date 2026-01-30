
package com.aps.dto.request.system;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
@Schema(description = "更新用户请求")
public class UserUpdateRequest {

    @Schema(description = "用户ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "用户ID不能为空")
    private Long id;

    @Schema(description = "角色")
    @Pattern(regexp = "^(admin|scheduler|operator|user)$", message = "角色必须是：admin/scheduler/operator/user")
    private String role;

    @Schema(description = "真实姓名")
    private String realName;

    @Schema(description = "手机号")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String mobile;

    @Schema(description = "邮箱")
    @Email(message = "邮箱格式不正确")
    private String email;

    @Schema(description = "状态：0-禁用，1-启用")
    private Integer status;
}
