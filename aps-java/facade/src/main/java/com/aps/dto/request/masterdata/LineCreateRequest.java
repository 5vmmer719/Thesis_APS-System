package com.aps.dto.request.masterdata;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 创建产线请求
 */
@Data
@Schema(description = "创建产线请求")
public class LineCreateRequest {

    @Schema(description = "车间ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "车间ID不能为空")
    private Long workshopId;

    @Schema(description = "产线编码", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "产线编码不能为空")
    private String lineCode;

    @Schema(description = "产线名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "产线名称不能为空")
    private String lineName;

    @Schema(description = "工艺类型：1-冲压，2-焊装，3-涂装，4-总装", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "工艺类型不能为空")
    private Integer processType;

    @Schema(description = "状态：0-禁用，1-启用", example = "1")
    private Integer status = 1;

    @Schema(description = "备注")
    private String remark;
}
