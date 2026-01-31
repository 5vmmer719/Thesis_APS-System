package com.aps.entity.plan;

import com.aps.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * MRP明细实体
 *
 * @author APS System
 * @since 2024-01-30
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("plan_mrp_item")
@Schema(description = "MRP明细")
public class MrpItem extends BaseEntity {

    @Schema(description = "主键ID")
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    @Schema(description = "MRP主表ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @TableField("mrp_id")
    private Long mrpId;

    @Schema(description = "物料编码", requiredMode = Schema.RequiredMode.REQUIRED)
    @TableField("item_code")
    private String itemCode;

    @Schema(description = "需求日期", requiredMode = Schema.RequiredMode.REQUIRED)
    @TableField("req_date")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate reqDate;

    @Schema(description = "需求数量")
    @TableField("req_qty")
    private BigDecimal reqQty;

    @Schema(description = "供应数量")
    @TableField("supply_qty")
    private BigDecimal supplyQty;

    @Schema(description = "缺口数量")
    @TableField("shortage_qty")
    private BigDecimal shortageQty;

    // ========== 非数据库字段 ==========

    @Schema(description = "物料名称")
    @TableField(exist = false)
    private String itemName;

    @Schema(description = "物料类型")
    @TableField(exist = false)
    private Integer itemType;

    @Schema(description = "单位")
    @TableField(exist = false)
    private String uom;
}

