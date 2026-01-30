package com.aps.entity.schedule;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 方案统计实体
 *
 * @author APS System
 * @since 2024-01-01
 */
@Data
@TableName("sch_plan_stat")
@Schema(description = "方案统计")
public class SchPlanStat {

    @Schema(description = "主键ID")
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    @Schema(description = "方案ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @TableField("plan_id")
    private Long planId;

    @Schema(description = "OTD准时交付率")
    @TableField("otd_rate")
    private BigDecimal otdRate;

    @Schema(description = "换型次数")
    @TableField("setup_times")
    private Integer setupTimes;

    @Schema(description = "平均产线负荷率")
    @TableField("avg_line_load")
    private BigDecimal avgLineLoad;

    @Schema(description = "创建时间")
    @TableField(value = "created_time", fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdTime;

    @Schema(description = "创建人")
    @TableField(value = "created_by", fill = FieldFill.INSERT)
    private String createdBy;

    @Schema(description = "更新时间")
    @TableField(value = "updated_time", fill = FieldFill.INSERT_UPDATE)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedTime;

    @Schema(description = "更新人")
    @TableField(value = "updated_by", fill = FieldFill.INSERT_UPDATE)
    private String updatedBy;

    @Schema(description = "逻辑删除")
    @TableField("deleted")
    @TableLogic
    private Integer deleted;
}

