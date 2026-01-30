package com.aps.entity.schedule;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 四大工艺串联绑定实体
 *
 * @author APS System
 * @since 2024-01-01
 */
@Data
@TableName("sch_wip_link")
@Schema(description = "四大工艺串联绑定")
public class SchWipLink {

    @Schema(description = "主键ID")
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    @Schema(description = "生产订单ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @TableField("prod_order_id")
    private Long prodOrderId;

    @Schema(description = "工艺类型: 1冲压 2焊装 3涂装 4总装", requiredMode = Schema.RequiredMode.REQUIRED)
    @TableField("process_type")
    private Integer processType;

    @Schema(description = "方案桶ID")
    @TableField("plan_bucket_id")
    private Long planBucketId;

    @Schema(description = "工单ID")
    @TableField("wo_id")
    private Long woId;

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

