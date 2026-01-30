package com.aps.dto.request.masterdata;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 创建工装请求
 */
@Data
@Schema(description = "创建工装请求")
public class ToolingCreateRequest {

    @Schema(description = "工装编码", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "工装编码不能为空")
    private String toolingCode;

    @Schema(description = "工装名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "工装名称不能为空")
    private String toolingName;

    @Schema(description = "工装类型：1-模具，2-夹具，3-检具", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "工装类型不能为空")
    private Integer toolingType;

    @Schema(description = "状态：0-禁用，1-启用", example = "1")
    private Integer status = 1;

    @Schema(description = "备注")
    private String remark;
}
