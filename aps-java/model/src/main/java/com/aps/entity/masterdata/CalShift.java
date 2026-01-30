package com.aps.entity.masterdata;

import com.aps.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalTime;

/**
 * 资源日历（班次）实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("cal_shift")
@Schema(description = "资源日历（班次）")
public class CalShift extends BaseEntity {

    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "日历ID（cal_day.id）")
    private Long dayId;

    @Schema(description = "班次编码")
    private String shiftCode;

    @Schema(description = "开始时间")
    private LocalTime startTime;

    @Schema(description = "结束时间")
    private LocalTime endTime;

    @Schema(description = "班次产能（辆/班）")
    private Integer capacityQty;

    @Schema(description = "状态：0-禁用，1-启用")
    private Integer status;

    @Schema(description = "备注")
    private String remark;
}
