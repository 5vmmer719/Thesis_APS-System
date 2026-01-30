package com.aps.entity.schedule;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 排产方案实体
 *
 * @author APS System
 * @since 2024-01-01
 */
@Data
@TableName(value = "sch_plan", autoResultMap = true)
@Schema(description = "排产方案")
public class SchPlan {

    @Schema(description = "主键ID")
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    @Schema(description = "任务ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @TableField("job_id")
    private Long jobId;

    @Schema(description = "方案编号", requiredMode = Schema.RequiredMode.REQUIRED)
    @TableField("plan_no")
    private String planNo;

    @Schema(description = "是否最优方案: 0否 1是")
    @TableField("is_best")
    private Integer isBest;

    @Schema(description = "KPI汇总数据")
    @TableField(value = "kpi_json", typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> kpiJson;

    @Schema(description = "状态: 0草稿 1已发布 2作废")
    @TableField("status")
    private Integer status;

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

    /**
     * 状态枚举
     */
    public enum Status {
        DRAFT(0, "草稿"),
        PUBLISHED(1, "已发布"),
        VOIDED(2, "已作废");

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

