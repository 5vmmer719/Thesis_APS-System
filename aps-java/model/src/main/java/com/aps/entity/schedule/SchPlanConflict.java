package com.aps.entity.schedule;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 方案冲突实体
 *
 * @author APS System
 * @since 2024-01-01
 */
@Data
@TableName(value = "sch_plan_conflict", autoResultMap = true)
@Schema(description = "方案冲突/不可行原因")
public class SchPlanConflict {

    @Schema(description = "主键ID")
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    @Schema(description = "方案ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @TableField("plan_id")
    private Long planId;

    @Schema(description = "冲突类型", requiredMode = Schema.RequiredMode.REQUIRED)
    @TableField("conflict_type")
    private String conflictType;

    @Schema(description = "级别: 1提示 2警告 3致命", requiredMode = Schema.RequiredMode.REQUIRED)
    @TableField("level")
    private Integer level;

    @Schema(description = "对象类型")
    @TableField("object_type")
    private String objectType;

    @Schema(description = "对象ID")
    @TableField("object_id")
    private Long objectId;

    @Schema(description = "冲突消息", requiredMode = Schema.RequiredMode.REQUIRED)
    @TableField("message")
    private String message;

    @Schema(description = "详细数据")
    @TableField(value = "payload", typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> payload;

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

    /**
     * 冲突级别枚举
     */
    public enum Level {
        INFO(1, "提示"),
        WARNING(2, "警告"),
        FATAL(3, "致命");

        private final Integer code;
        private final String desc;

        Level(Integer code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        public Integer getCode() {
            return code;
        }

        public String getDesc() {
            return desc;
        }

        public static String getDesc(Integer code) {
            for (Level level : values()) {
                if (level.code.equals(code)) {
                    return level.desc;
                }
            }
            return "未知";
        }
    }

    /**
     * 冲突类型常量
     */
    public static class ConflictType {
        public static final String CAPACITY = "CAPACITY";           // 容量冲突
        public static final String MAINTENANCE = "MAINTENANCE";     // 维护窗口冲突
        public static final String ROUTE = "ROUTE";                 // 路线冲突
        public static final String MATERIAL = "MATERIAL";           // 物料冲突
        public static final String SETUP = "SETUP";                 // 换型冲突
        public static final String OTHER = "OTHER";                 // 其他
    }
}

