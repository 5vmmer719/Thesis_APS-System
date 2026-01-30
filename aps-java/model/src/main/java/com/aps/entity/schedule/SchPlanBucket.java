package com.aps.entity.schedule;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 排产方案明细(班次桶)实体
 *
 * @author APS System
 * @since 2024-01-01
 */
@Data
@TableName("sch_plan_bucket")
@Schema(description = "排产方案明细(班次桶)")
public class SchPlanBucket {

    @Schema(description = "主键ID")
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    @Schema(description = "方案ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @TableField("plan_id")
    private Long planId;

    @Schema(description = "工艺类型: 1冲压 2焊装 3涂装 4总装", requiredMode = Schema.RequiredMode.REQUIRED)
    @TableField("process_type")
    private Integer processType;

    @Schema(description = "产线ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @TableField("line_id")
    private Long lineId;

    @Schema(description = "业务日期", requiredMode = Schema.RequiredMode.REQUIRED)
    @TableField("biz_date")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate bizDate;

    @Schema(description = "班次编码", requiredMode = Schema.RequiredMode.REQUIRED)
    @TableField("shift_code")
    private String shiftCode;

    @Schema(description = "生产订单ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @TableField("prod_order_id")
    private Long prodOrderId;

    @Schema(description = "班次内顺序号", requiredMode = Schema.RequiredMode.REQUIRED)
    @TableField("seq_no")
    private Integer seqNo;

    @Schema(description = "分配数量(辆)", requiredMode = Schema.RequiredMode.REQUIRED)
    @TableField("qty")
    private Integer qty;

    @Schema(description = "源换型键(如模具/颜色)")
    @TableField("from_setup_key")
    private String fromSetupKey;

    @Schema(description = "目标换型键")
    @TableField("to_setup_key")
    private String toSetupKey;

    @Schema(description = "换型时间(分钟)")
    @TableField("setup_minutes")
    private Integer setupMinutes;

    @Schema(description = "换型成本")
    @TableField("setup_cost")
    private BigDecimal setupCost;

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
     * 工艺类型枚举
     */
    public enum ProcessType {
        STAMPING(1, "冲压"),
        WELDING(2, "焊装"),
        PAINTING(3, "涂装"),
        ASSEMBLY(4, "总装");

        private final Integer code;
        private final String desc;

        ProcessType(Integer code, String desc) {
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
            for (ProcessType type : values()) {
                if (type.code.equals(code)) {
                    return type.desc;
                }
            }
            return "未知";
        }
    }
}

