package com.aps.entity.plan;

import com.aps.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

/**
 * MPS主生产计划实体
 *
 * @author APS System
 * @since 2024-01-30
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("plan_mps")
@Schema(description = "MPS主生产计划")
public class Mps extends BaseEntity {

    @Schema(description = "主键ID")
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    @Schema(description = "MPS计划编号", requiredMode = Schema.RequiredMode.REQUIRED)
    @TableField("mps_no")
    private String mpsNo;

    @Schema(description = "计划开始日期", requiredMode = Schema.RequiredMode.REQUIRED)
    @TableField("start_date")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    @Schema(description = "计划结束日期", requiredMode = Schema.RequiredMode.REQUIRED)
    @TableField("end_date")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;

    @Schema(description = "状态: 0-草稿, 1-审批中, 2-已批准, 3-关闭")
    @TableField("status")
    private Integer status;

    @Schema(description = "备注")
    @TableField("remark")
    private String remark;

    /**
     * 状态枚举
     */
    public enum Status {
        DRAFT(0, "草稿"),
        APPROVING(1, "审批中"),
        APPROVED(2, "已批准"),
        CLOSED(3, "已关闭");

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

