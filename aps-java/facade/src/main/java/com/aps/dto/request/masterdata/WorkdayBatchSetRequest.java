package com.aps.dto.request.masterdata;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

/**
 * 批量设置工作日请求
 */
@Data
@Schema(description = "批量设置工作日请求")
public class WorkdayBatchSetRequest {

    @Schema(description = "资源类型：1-产线，2-工位，3-设备，4-模具，5-人力组", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "资源类型不能为空")
    private Integer resourceType;

    @Schema(description = "资源ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "资源ID不能为空")
    private Long resourceId;

    @Schema(description = "开始日期", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "开始日期不能为空")
    private LocalDate startDate;

    @Schema(description = "结束日期", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "结束日期不能为空")
    private LocalDate endDate;

    @Schema(description = "是否工作日：0-否，1-是", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "是否工作日不能为空")
    private Integer isWorkday;

    @Schema(description = "排除日期列表（如节假日）")
    private List<LocalDate> excludeDates;
}
