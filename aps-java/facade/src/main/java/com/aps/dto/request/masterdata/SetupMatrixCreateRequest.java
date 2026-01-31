package com.aps.dto.request.masterdata;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 创建换型矩阵请求
 *
 * @author APS System
 * @since 2024-01-30
 */
@Data
@Schema(description = "创建换型矩阵请求")
public class SetupMatrixCreateRequest {

    @Schema(description = "工艺类型：1-冲压，2-焊装，3-涂装，4-总装", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "工艺类型不能为空")
    private Integer processType;

    @Schema(description = "源换型键（如：模具编码、颜色编码）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "源换型键不能为空")
    private String fromKey;

    @Schema(description = "目标换型键", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "目标换型键不能为空")
    private String toKey;

    @Schema(description = "换型时间（分钟）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "换型时间不能为空")
    private Integer setupMinutes;

    @Schema(description = "换型成本", example = "100.00")
    private BigDecimal setupCost;

    @Schema(description = "状态：1-启用，0-停用", example = "1")
    private Integer status;

    @Schema(description = "备注")
    private String remark;
}

