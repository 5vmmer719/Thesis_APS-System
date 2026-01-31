package com.aps.dto.response.masterdata;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 换型矩阵DTO
 *
 * @author APS System
 * @since 2024-01-30
 */
@Data
@Schema(description = "换型矩阵DTO")
public class SetupMatrixDTO {

    @Schema(description = "换型矩阵ID")
    private Long id;

    @Schema(description = "工艺类型：1-冲压，2-焊装，3-涂装，4-总装")
    private Integer processType;

    @Schema(description = "工艺类型名称")
    private String processTypeName;

    @Schema(description = "源换型键")
    private String fromKey;

    @Schema(description = "目标换型键")
    private String toKey;

    @Schema(description = "换型时间（分钟）")
    private Integer setupMinutes;

    @Schema(description = "换型成本")
    private BigDecimal setupCost;

    @Schema(description = "状态：1-启用，0-停用")
    private Integer status;

    @Schema(description = "状态文本")
    private String statusText;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建时间")
    private LocalDateTime createdTime;

    @Schema(description = "创建人")
    private String createdBy;

    @Schema(description = "更新时间")
    private LocalDateTime updatedTime;

    @Schema(description = "更新人")
    private String updatedBy;
}

