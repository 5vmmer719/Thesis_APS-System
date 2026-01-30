package com.aps.entity.schedule;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 排产任务实体
 *
 * @author APS System
 * @since 2024-01-01
 */
@Data
@TableName(value = "sch_job", autoResultMap = true)
@Schema(description = "排产任务")
public class SchJob {

    @Schema(description = "主键ID")
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    @Schema(description = "任务编号", requiredMode = Schema.RequiredMode.REQUIRED)
    @TableField("job_no")
    private String jobNo;

    @Schema(description = "排产起始日期", requiredMode = Schema.RequiredMode.REQUIRED)
    @TableField("horizon_start")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate horizonStart;

    @Schema(description = "排产结束日期", requiredMode = Schema.RequiredMode.REQUIRED)
    @TableField("horizon_end")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate horizonEnd;

    @Schema(description = "范围配置(订单ID列表、工艺、产线等)")
    @TableField(value = "scope_json", typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> scopeJson;

    @Schema(description = "目标权重配置")
    @TableField(value = "objective_json", typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> objectiveJson;

    @Schema(description = "约束规则配置")
    @TableField(value = "constraint_json", typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> constraintJson;

    @Schema(description = "状态: 0待运行 1运行中 2成功 3失败 4不可行")
    @TableField("status")
    private Integer status;

    @Schema(description = "引擎追踪ID")
    @TableField("engine_trace")
    private String engineTrace;

    @Schema(description = "错误信息")
    @TableField("error_msg")
    private String errorMsg;

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
     * 状态枚举
     */
    public enum Status {
        PENDING(0, "待运行"),
        RUNNING(1, "运行中"),
        SUCCESS(2, "成功"),
        FAILED(3, "失败"),
        INFEASIBLE(4, "不可行");

        private final Integer code;
        private final String desc;

        Status(Integer code, String desc) {
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
            for (Status status : values()) {
                if (status.code.equals(code)) {
                    return status.desc;
                }
            }
            return "未知";
        }
    }
}

