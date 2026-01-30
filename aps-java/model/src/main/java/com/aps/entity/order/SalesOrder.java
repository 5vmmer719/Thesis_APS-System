package com.aps.entity.order;


import com.aps.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

/**
 * 销售订单实体
 *
 * @author APS System
 * @since 2024-01-01
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ord_sales")
@Schema(description = "销售订单")
public class SalesOrder extends BaseEntity {

    @Schema(description = "主键ID")
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    @Schema(description = "销售订单号", requiredMode = Schema.RequiredMode.REQUIRED)
    @TableField("sales_no")
    private String salesNo;

    @Schema(description = "客户编码")
    @TableField("customer_code")
    private String customerCode;

    @Schema(description = "车型ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @TableField("model_id")
    private Long modelId;

    @Schema(description = "订单数量", requiredMode = Schema.RequiredMode.REQUIRED)
    @TableField("qty")
    private Integer qty;

    @Schema(description = "交期", requiredMode = Schema.RequiredMode.REQUIRED)
    @TableField("due_date")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dueDate;

    @Schema(description = "优先级(数字越大优先级越高)", example = "0")
    @TableField("priority")
    private Integer priority;

    @Schema(description = "状态: 0-新建, 1-已审核, 2-已转生产, 9-取消")
    @TableField("status")
    private Integer status;

    @Schema(description = "来源数据(JSON)")
    @TableField("source_payload")
    private String sourcePayload;

    @Schema(description = "备注")
    @TableField("remark")
    private String remark;

    // ========== 非数据库字段 ==========

    @Schema(description = "车型编码")
    @TableField(exist = false)
    private String modelCode;

    @Schema(description = "车型名称")
    @TableField(exist = false)
    private String modelName;

    /**
     * 状态枚举
     */
    public enum Status {
        NEW(0, "新建"),
        APPROVED(1, "已审核"),
        CONVERTED(2, "已转生产"),
        CANCELLED(9, "取消");

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

