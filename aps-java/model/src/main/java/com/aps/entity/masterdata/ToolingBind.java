package com.aps.entity.masterdata;


import com.aps.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 工装绑定关系实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("md_tooling_bind")
@Schema(description = "工装绑定关系")
public class ToolingBind extends BaseEntity {

    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "工装ID")
    private Long toolingId;

    @Schema(description = "资源类型：2-工位，3-设备")
    private Integer resourceType;

    @Schema(description = "资源ID")
    private Long resourceId;

    @Schema(description = "状态：0-禁用，1-启用")
    private Integer status;

    @Schema(description = "备注")
    private String remark;
}
