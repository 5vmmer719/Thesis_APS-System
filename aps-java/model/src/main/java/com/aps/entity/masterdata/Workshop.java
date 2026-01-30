package com.aps.entity.masterdata;


import com.aps.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 车间实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("md_workshop")
@Schema(description = "车间")
public class Workshop extends BaseEntity {

    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "车间编码")
    private String workshopCode;

    @Schema(description = "车间名称")
    private String workshopName;

    @Schema(description = "工艺类型：1-冲压，2-焊装，3-涂装，4-总装（可选）")
    private Integer processType;

    @Schema(description = "状态：0-禁用，1-启用")
    private Integer status;

    @Schema(description = "备注")
    private String remark;
}
