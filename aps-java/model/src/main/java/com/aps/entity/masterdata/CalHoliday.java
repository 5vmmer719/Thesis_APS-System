package com.aps.entity.masterdata;

import com.aps.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

/**
 * 节假日实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("cal_holiday")
@Schema(description = "节假日")
public class CalHoliday extends BaseEntity {

    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "业务日期")
    private LocalDate bizDate;

    @Schema(description = "节假日名称")
    private String name;
}
