package com.aps.entity.masterdata;


import com.aps.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 模具/工装实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("md_tooling")
@Schema(description = "模具/工装")
public class Tooling extends BaseEntity {

    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "工装编码")
    private String toolingCode;

    @Schema(description = "工装名称")
    private String toolingName;

    @Schema(description = "工装类型：1-模具，2-夹具，3-检具")
    private Integer toolingType;

    @Schema(description = "状态：0-禁用，1-启用")
    private Integer status;

    @Schema(description = "备注")
    private String remark;
}
