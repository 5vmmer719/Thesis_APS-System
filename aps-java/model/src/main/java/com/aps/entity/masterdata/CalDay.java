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
 * 资源日历（日）实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("cal_day")
@Schema(description = "资源日历（日）")
public class CalDay extends BaseEntity {

    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "资源类型：1-产线，2-工位，3-设备，4-模具，5-人力组")
    private Integer resourceType;

    @Schema(description = "资源ID")
    private Long resourceId;

    @Schema(description = "业务日期")
    private LocalDate bizDate;

    @Schema(description = "是否工作日：0-否，1-是")
    private Integer isWorkday;

    @Schema(description = "备注")
    private String remark;
}
