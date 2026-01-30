package com.aps.entity.schedule;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 手动调整日志实体
 *
 * @author APS System
 * @since 2024-01-01
 */
@Data
@TableName(value = "sch_manual_adjust_log", autoResultMap = true)
@Schema(description = "手动调整日志")
public class SchManualAdjustLog {

    @Schema(description = "主键ID")
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    @Schema(description = "方案ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @TableField("plan_id")
    private Long planId;

    @Schema(description = "操作用户ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @TableField("user_id")
    private Long userId;

    @Schema(description = "变更内容JSON", requiredMode = Schema.RequiredMode.REQUIRED)
    @TableField(value = "change_json", typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> changeJson;

    @Schema(description = "备注")
    @TableField("remark")
    private String remark;

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

