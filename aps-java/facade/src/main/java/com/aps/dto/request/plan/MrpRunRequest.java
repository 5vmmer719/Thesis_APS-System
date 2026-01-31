package com.aps.dto.request.plan;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotNull;

/**
 * MRP运行请求
 *
 * @author APS System
 * @since 2024-01-30
 */
@Data
@Schema(description = "MRP运行请求")
public class MrpRunRequest {

    @Schema(description = "MPS主表ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "MPS主表ID不能为空")
    private Long mpsId;
}

