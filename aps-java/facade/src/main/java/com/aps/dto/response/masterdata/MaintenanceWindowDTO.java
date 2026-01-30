package com.aps.dto.response.masterdata;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 维护窗口DTO
 */
@Data
@Schema(description = "维护窗口")
public class MaintenanceWindowDTO {

    @Schema(description = "维护窗口ID")
    private Long id;

    @Schema(description = "资源类型：1-产线，2-工位，3-设备，4-模具，5-人力组")
    private Integer resourceType;

    @Schema(description = "资源ID")
    private Long resourceId;

    @Schema(description = "开始时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    @Schema(description = "结束时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

    @Schema(description = "维护原因")
    private String reason;

    @Schema(description = "状态：0-禁用，1-启用")
    private Integer status;
}
