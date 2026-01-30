package com.aps.entity.masterdata;

import com.aps.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * BOM主数据实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("md_bom")
public class Bom extends BaseEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * BOM编码
     */
    private String bomCode;

    /**
     * BOM名称
     */
    @TableField(exist = false)
    private String bomName;

    /**
     * 车型ID
     */
    private Long modelId;

    /**
     * 版本号
     */
    private String version;

    /**
     * 生效开始时间
     */
    private LocalDateTime effectiveFrom;

    /**
     * 生效结束时间
     */
    private LocalDateTime effectiveTo;

    /**
     * 状态：1-启用，0-停用
     */
    private Integer status;

    /**
     * 备注
     */
    private String remark;
}
