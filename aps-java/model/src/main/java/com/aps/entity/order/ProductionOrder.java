
package com.aps.entity.order;


import com.aps.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.util.List;

/**
 * 生产订单实体
 *
 * @author APS System
 * @since 2024-01-01
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ord_prod")
@Schema(description = "生产订单")
public class ProductionOrder extends BaseEntity {

    @Schema(description = "主键ID")
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    @Schema(description = "生产订单号", requiredMode = Schema.RequiredMode.REQUIRED)
    @TableField("prod_no")
    private String prodNo;

    @Schema(description = "销售订单ID")
    @TableField("sales_id")
    private Long salesId;

    @Schema(description = "订单类型: 1-常规, 2-紧急, 3-定制, 4-插单, 5-返工")
    @TableField("order_kind")
    private Integer orderKind;

    @Schema(description = "车型ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @TableField("model_id")
    private Long modelId;

    @Schema(description = "BOM ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @TableField("bom_id")
    private Long bomId;

    @Schema(description = "工艺路线版本", requiredMode = Schema.RequiredMode.REQUIRED)
    @TableField("route_version")
    private String routeVersion;

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

    @Schema(description = "状态: 0-新建, 1-待审批, 2-已审批, 3-已排产, 4-执行中, 5-完成, 9-取消")
    @TableField("status")
    private Integer status;

    @Schema(description = "备注")
    @TableField("remark")
    private String remark;

    // ========== 非数据库字段 ==========

    @Schema(description = "销售订单号")
    @TableField(exist = false)
    private String salesNo;

    @Schema(description = "车型编码")
    @TableField(exist = false)
    private String modelCode;

    @Schema(description = "车型名称")
    @TableField(exist = false)
    private String modelName;

    @Schema(description = "BOM编码")
    @TableField(exist = false)
    private String bomCode;

    // 🔧 添加此字段
    @Schema(description = "订单属性列表")
    @TableField(exist = false)
    private List<ProductionOrderAttr> attrs;


    /**
     * 订单类型枚举
     */
    public enum OrderKind {
        NORMAL(1, "常规"),
        URGENT(2, "紧急"),
        CUSTOM(3, "定制"),
        INSERT(4, "插单"),
        REWORK(5, "返工");

        private final Integer code;
        private final String desc;

        OrderKind(Integer code, String desc) {
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
            for (OrderKind kind : values()) {
                if (kind.code.equals(code)) {
                    return kind.desc;
                }
            }
            return "未知";
        }
    }

    /**
     * 状态枚举
     */
    public enum Status {
        NEW(0, "新建"),
        PENDING_APPROVAL(1, "待审批"),
        APPROVED(2, "已审批"),
        SCHEDULED(3, "已排产"),
        IN_PROGRESS(4, "执行中"),
        COMPLETED(5, "完成"),
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
