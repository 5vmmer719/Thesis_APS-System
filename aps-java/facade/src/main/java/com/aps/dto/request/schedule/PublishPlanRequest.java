package com.aps.dto.request.schedule;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 方案发布请求
 *
 * @author APS System
 * @since 2024-01-01
 */
@Data
@Schema(description = "方案发布请求")
public class PublishPlanRequest {

    @NotNull(message = "方案ID不能为空")
    @Schema(description = "方案ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1001")
    private Long planId;

    @Schema(description = "是否生成工单", example = "true")
    private Boolean generateWorkOrder;

    @Schema(description = "备注")
    private String remark;
}

