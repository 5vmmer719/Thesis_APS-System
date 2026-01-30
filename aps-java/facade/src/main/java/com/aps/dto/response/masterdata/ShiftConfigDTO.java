package com.aps.dto.response.masterdata;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalTime;

/**
 * 班次配置DTO
 */
@Data
@Schema(description = "班次配置")
public class ShiftConfigDTO {

    @Schema(description = "班次编码", example = "SHIFT_1")
    private String shiftCode;

    @Schema(description = "开始时间", example = "08:00:00")
    private LocalTime startTime;

    @Schema(description = "结束时间", example = "16:00:00")
    private LocalTime endTime;

    @Schema(description = "班次产能（辆/班）", example = "50")
    private Integer capacityQty;
}
