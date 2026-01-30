package com.aps.dto.request.masterdata;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "创建车型请求")
public class ModelCreateRequest {

    @Schema(description = "车型编码", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "车型编码不能为空")
    private String modelCode;

    @Schema(description = "车型名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "车型名称不能为空")
    private String modelName;

    @Schema(description = "平台")
    private String platform;

    @Schema(description = "状态", example = "1")
    private Integer status = 1;

    @Schema(description = "备注")
    private String remark;
}
