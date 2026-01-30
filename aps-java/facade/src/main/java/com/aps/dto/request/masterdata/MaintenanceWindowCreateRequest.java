package com.aps.dto.request.masterdata;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 创建维护窗口请求
 */
@Data
@Schema(description = "创建维护窗口请求")
public class MaintenanceWindowCreateRequest {

    @Schema(description = "资源类型：1-产线，2-工位，3-设备，4-模具，5-人力组", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "资源类型不能为空")
    private Integer resourceType;

    @Schema(description = "资源ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "资源ID不能为空")
    private Long resourceId;

    @Schema(description = "开始时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "开始时间不能为空")
    private LocalDateTime startTime;

    @Schema(description = "结束时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "结束时间不能为空")
    private LocalDateTime endTime;

    @Schema(description = "维护原因")
    private String reason;

    @Schema(description = "状态：0-禁用，1-启用", example = "1")
    private Integer status = 1;
}
