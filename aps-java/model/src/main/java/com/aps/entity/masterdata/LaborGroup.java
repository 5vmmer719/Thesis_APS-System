package com.aps.entity.masterdata;

import com.aps.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 人力组实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("md_labor_group")
@Schema(description = "人力组")
public class LaborGroup extends BaseEntity {

    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "人力组编码")
    private String laborCode;

    @Schema(description = "人力组名称")
    private String laborName;

    @Schema(description = "人数")
    private Integer headcount;

    @Schema(description = "状态：0-禁用，1-启用")
    private Integer status;

    @Schema(description = "备注")
    private String remark;
}
