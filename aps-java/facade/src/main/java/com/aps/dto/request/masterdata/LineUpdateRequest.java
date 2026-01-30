package com.aps.dto.request.masterdata;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 更新产线请求
 */
@Data
@Schema(description = "更新产线请求")
public class LineUpdateRequest {

    @Schema(description = "产线ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "产线ID不能为空")
    private Long id;

    @Schema(description = "产线名称")
    private String lineName;

    @Schema(description = "工艺类型：1-冲压，2-焊装，3-涂装，4-总装")
    private Integer processType;

    @Schema(description = "状态：0-禁用，1-启用")
    private Integer status;

    @Schema(description = "备注")
    private String remark;
}
