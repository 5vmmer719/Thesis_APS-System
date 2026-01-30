package com.aps.entity.masterdata;


import com.aps.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("md_model")
public class Model extends BaseEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 车型编码
     */
    private String modelCode;

    /**
     * 车型名称
     */
    private String modelName;

    /**
     * 平台
     */
    private String platform;

    /**
     * 状态：1-启用，0-停用
     */
    private Integer status;

    /**
     * 备注
     */
    private String remark;
}
