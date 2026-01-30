package com.aps.entity.order;

import com.aps.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 生产订单属性实体
 *
 * @author APS System
 * @since 2024-01-01
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ord_prod_attr")
@Schema(description = "生产订单属性")
public class ProductionOrderAttr extends BaseEntity {

    @Schema(description = "主键ID")
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    @Schema(description = "生产订单ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @TableField("ord_id")
    private Long ordId;

    @Schema(description = "属性键", requiredMode = Schema.RequiredMode.REQUIRED, example = "color")
    @TableField("attr_key")
    private String attrKey;

    @Schema(description = "属性值", requiredMode = Schema.RequiredMode.REQUIRED, example = "红色")
    @TableField("attr_value")
    private String attrValue;

    @Schema(description = "备注")
    @TableField("remark")
    private String remark;

    /**
     * 常用属性键常量
     */
    public static class AttrKey {
        public static final String COLOR = "color";              // 颜色
        public static final String INTERIOR = "interior";        // 内饰
        public static final String ENGINE = "engine";            // 发动机型号
        public static final String TRANSMISSION = "transmission"; // 变速箱
        public static final String MOLD_CODE = "mold_code";      // 模具编码(冲压)
        public static final String FIXTURE = "fixture";          // 夹具(焊装)
    }
}

