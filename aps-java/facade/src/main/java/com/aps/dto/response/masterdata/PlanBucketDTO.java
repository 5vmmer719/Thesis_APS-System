package com.aps.dto.response.masterdata;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 排产方案明细(班次桶)响应DTO
 *
 * @author APS System
 * @since 2024-01-01
 */
@Data
@Schema(description = "排产方案明细(班次桶)")
public class PlanBucketDTO {

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "方案ID")
    private Long planId;

    @Schema(description = "工艺类型: 1-冲压, 2-焊装, 3-涂装, 4-总装")
    private Integer processType;

    @Schema(description = "工艺类型文本")
    private String processTypeText;

    @Schema(description = "产线ID")
    private Long lineId;

    @Schema(description = "产线编码")
    private String lineCode;

    @Schema(description = "产线名称")
    private String lineName;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "业务日期")
    private LocalDate bizDate;

    @Schema(description = "班次编码")
    private String shiftCode;

    @Schema(description = "班次名称")
    private String shiftName;

    @Schema(description = "生产订单ID")
    private Long prodOrderId;

    @Schema(description = "生产订单号")
    private String prodNo;

    @Schema(description = "车型编码")
    private String modelCode;

    @Schema(description = "车型名称")
    private String modelName;

    @Schema(description = "班次内顺序号")
    private Integer seqNo;

    @Schema(description = "分配数量(辆)")
    private Integer qty;

    @Schema(description = "源换型键")
    private String fromSetupKey;

    @Schema(description = "目标换型键")
    private String toSetupKey;

    @Schema(description = "换型时间(分钟)")
    private Integer setupMinutes;

    @Schema(description = "换型成本")
    private BigDecimal setupCost;

    @Schema(description = "备注")
    private String remark;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "创建时间")
    private LocalDateTime createdTime;

    @Schema(description = "创建人")
    private String createdBy;
}

