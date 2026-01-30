
package com.aps.dto.request.system;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
@Schema(description = "创建用户请求")
public class UserCreateRequest {

    @Schema(description = "用户名", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "用户名不能为空")
    @Pattern(regexp = "^[a-zA-Z0-9_]{4,20}$", message = "用户名只能包含字母、数字、下划线，长度4-20位")
    private String username;

    @Schema(description = "密码", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "密码不能为空")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[a-zA-Z\\d@$!%*?&]{8,20}$",
            message = "密码必须包含大小写字母和数字，长度8-20位")
    private String password;

    @Schema(description = "角色", requiredMode = Schema.RequiredMode.REQUIRED, example = "user")
    @NotBlank(message = "角色不能为空")
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

    @Schema(description = "状态：0-禁用，1-启用", example = "1")
    private Integer status = 1;
}
