package com.aps.dto.response.masterdata;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalTime;

/**
 * 资源日历（班次）DTO
 */
@Data
@Schema(description = "资源日历（班次）")
public class CalShiftDTO {

    @Schema(description = "班次ID")
    private Long id;

    @Schema(description = "日历日ID")
    private Long dayId;

    @Schema(description = "班次编码")
    private String shiftCode;

    @Schema(description = "开始时间")
    @JsonFormat(pattern = "HH:mm:ss")
    private LocalTime startTime;

    @Schema(description = "结束时间")
    @JsonFormat(pattern = "HH:mm:ss")
    private LocalTime endTime;

    @Schema(description = "班次产能（辆/班）")
    private Integer capacityQty;

    @Schema(description = "状态：0-禁用，1-启用")
    private Integer status;

    @Schema(description = "备注")
    private String remark;
}
