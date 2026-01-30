package com.aps.entity.masterdata;


import com.aps.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("md_item")
public class Item extends BaseEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 物料编码
     */
    private String itemCode;

    /**
     * 物料名称
     */
    private String itemName;

    /**
     * 物料类型：1-原料，2-半成品，3-成品，4-辅料
     */
    private Integer itemType;

    /**
     * 计量单位
     */
    private String uom;

    /**
     * 规格
     */
    private String spec;

    /**
     * 状态：1-启用，0-停用
     */
    private Integer status;

    /**
     * 备注
     */
    private String remark;
}
