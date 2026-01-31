package com.aps.entity.plan;

import com.aps.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Map;

/**
 * MRP物料需求计划实体
 *
 * @author APS System
 * @since 2024-01-30
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "plan_mrp", autoResultMap = true)
@Schema(description = "MRP物料需求计划")
public class Mrp extends BaseEntity {

    @Schema(description = "主键ID")
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    @Schema(description = "MRP计划编号", requiredMode = Schema.RequiredMode.REQUIRED)
    @TableField("mrp_no")
    private String mrpNo;

    @Schema(description = "MPS主表ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @TableField("mps_id")
    private Long mpsId;

    @Schema(description = "状态: 0-生成中, 1-完成, 2-失败")
    @TableField("status")
    private Integer status;

    @Schema(description = "运算结果数据")
    @TableField(value = "result_payload", typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> resultPayload;

    @Schema(description = "备注")
    @TableField("remark")
    private String remark;

    // ========== 非数据库字段 ==========

    @Schema(description = "MPS编号")
    @TableField(exist = false)
    private String mpsNo;

    /**
     * 状态枚举
     */
    public enum Status {
        GENERATING(0, "生成中"),
        COMPLETED(1, "完成"),
        FAILED(2, "失败");

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

