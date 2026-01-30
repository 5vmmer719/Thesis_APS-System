package com.aps.entity.masterdata;


import com.aps.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * BOM明细实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("md_bom_item")
public class BomItem extends BaseEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * BOM ID
     */
    private Long bomId;

    /**
     * 父物料编码
     */
    private String parentItemCode;

    /**
     * 物料编码
     */
    private String itemCode;

    /**
     * 用量
     */
    private BigDecimal qty;

    /**
     * 损耗率
     */
    private BigDecimal lossRate;

    /**
     * 层级
     */
    private Integer levelNo;

    /**
     * 是否可选：1-是，0-否
     */
    private Integer isOptional;

    /**
     * 选配组
     */
    private String optionGroup;

    /**
     * 替代组
     */
    private String altGroup;

    /**
     * 备注
     */
    private String remark;
}
