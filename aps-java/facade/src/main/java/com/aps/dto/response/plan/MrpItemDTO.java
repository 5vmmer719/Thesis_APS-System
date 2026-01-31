package com.aps.dto.response.plan;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * MRP明细响应DTO
 *
 * @author APS System
 * @since 2024-01-30
 */
@Data
@Schema(description = "MRP明细响应")
public class MrpItemDTO {

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "MRP主表ID")
    private Long mrpId;

    @Schema(description = "物料编码")
    private String itemCode;

    @Schema(description = "物料名称")
    private String itemName;

    @Schema(description = "物料类型")
    private Integer itemType;

    @Schema(description = "单位")
    private String uom;

    @Schema(description = "需求日期")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate reqDate;

    @Schema(description = "需求数量")
    private BigDecimal reqQty;

    @Schema(description = "供应数量")
    private BigDecimal supplyQty;

    @Schema(description = "缺口数量")
    private BigDecimal shortageQty;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdTime;

    @Schema(description = "创建人")
    private String createdBy;
}

