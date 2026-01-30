package com.aps.dto.request.masterdata;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 更新工位请求
 */
@Data
@Schema(description = "更新工位请求")
public class StationUpdateRequest {

    @Schema(description = "工位ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "工位ID不能为空")
    private Long id;

    @Schema(description = "工位名称")
    private String stationName;

    @Schema(description = "班次产能（辆/班）")
    private Integer capacityQtyPerShift;

    @Schema(description = "状态：0-禁用，1-启用")
    private Integer status;

    @Schema(description = "备注")
    private String remark;
}
