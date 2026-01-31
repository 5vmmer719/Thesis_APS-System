package com.aps.dto.request.masterdata;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 更新换型矩阵请求
 *
 * @author APS System
 * @since 2024-01-30
 */
@Data
@Schema(description = "更新换型矩阵请求")
public class SetupMatrixUpdateRequest {

    @Schema(description = "换型矩阵ID", hidden = true)
    private Long id;

    @Schema(description = "换型时间（分钟）")
    private Integer setupMinutes;

    @Schema(description = "换型成本")
    private BigDecimal setupCost;

    @Schema(description = "状态：1-启用，0-停用")
    private Integer status;

    @Schema(description = "备注")
    private String remark;
}

