package com.aps.dto.request.masterdata;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 创建工位请求
 */
@Data
@Schema(description = "创建工位请求")
public class StationCreateRequest {

    @Schema(description = "产线ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "产线ID不能为空")
    private Long lineId;

    @Schema(description = "工位编码", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "工位编码不能为空")
    private String stationCode;

    @Schema(description = "工位名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "工位名称不能为空")
    private String stationName;

    @Schema(description = "班次产能（辆/班）", example = "50")
    private Integer capacityQtyPerShift = 0;

    @Schema(description = "状态：0-禁用，1-启用", example = "1")
    private Integer status = 1;

    @Schema(description = "备注")
    private String remark;
}
