package com.aps.entity.masterdata;


import com.aps.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 工位实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("md_station")
@Schema(description = "工位")
public class Station extends BaseEntity {

    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "产线ID")
    private Long lineId;

    @Schema(description = "工位编码")
    private String stationCode;

    @Schema(description = "工位名称")
    private String stationName;

    @Schema(description = "班次产能（辆/班）")
    private Integer capacityQtyPerShift;

    @Schema(description = "状态：0-禁用，1-启用")
    private Integer status;

    @Schema(description = "备注")
    private String remark;
}
