package com.aps.entity.masterdata;

import com.aps.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 维护窗口实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("cal_maintenance_window")
@Schema(description = "维护窗口")
public class CalMaintenanceWindow extends BaseEntity {

    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "资源类型：1-产线，2-工位，3-设备，4-模具，5-人力组")
    private Integer resourceType;

    @Schema(description = "资源ID")
    private Long resourceId;

    @Schema(description = "开始时间")
    private LocalDateTime startTime;

    @Schema(description = "结束时间")
    private LocalDateTime endTime;

    @Schema(description = "维护原因")
    private String reason;

    @Schema(description = "状态：0-禁用，1-启用")
    private Integer status;
}
