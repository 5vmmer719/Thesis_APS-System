package com.aps.entity.plan;

import com.aps.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

/**
 * MPS明细实体
 *
 * @author APS System
 * @since 2024-01-30
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("plan_mps_item")
@Schema(description = "MPS明细")
public class MpsItem extends BaseEntity {

    @Schema(description = "主键ID")
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    @Schema(description = "MPS主表ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @TableField("mps_id")
    private Long mpsId;

    @Schema(description = "计划日期", requiredMode = Schema.RequiredMode.REQUIRED)
    @TableField("biz_date")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate bizDate;

    @Schema(description = "车型ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @TableField("model_id")
    private Long modelId;

    @Schema(description = "计划数量", requiredMode = Schema.RequiredMode.REQUIRED)
    @TableField("qty")
    private Integer qty;

    // ========== 非数据库字段 ==========

    @Schema(description = "车型编码")
    @TableField(exist = false)
    private String modelCode;

    @Schema(description = "车型名称")
    @TableField(exist = false)
    private String modelName;
}

