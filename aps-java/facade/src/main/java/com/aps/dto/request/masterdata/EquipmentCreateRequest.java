package com.aps.dto.request.masterdata;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 创建设备请求
 */
@Data
@Schema(description = "创建设备请求")
public class EquipmentCreateRequest {

    @Schema(description = "工位ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "工位ID不能为空")
    private Long stationId;

    @Schema(description = "设备编码", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "设备编码不能为空")
    private String equipCode;

    @Schema(description = "设备名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "设备名称不能为空")
    private String equipName;

    @Schema(description = "状态：0-禁用，1-启用", example = "1")
    private Integer status = 1;

    @Schema(description = "备注")
    private String remark;
}
