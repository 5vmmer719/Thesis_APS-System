package com.aps.dto.request.masterdata;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 创建车间请求
 */
@Data
@Schema(description = "创建车间请求")
public class WorkshopCreateRequest {

    @Schema(description = "车间编码", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "车间编码不能为空")
    private String workshopCode;

    @Schema(description = "车间名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "车间名称不能为空")
    private String workshopName;

    @Schema(description = "工艺类型：1-冲压，2-焊装，3-涂装，4-总装")
    private Integer processType;

    @Schema(description = "状态：0-禁用，1-启用", example = "1")
    private Integer status = 1;

    @Schema(description = "备注")
    private String remark;
}
