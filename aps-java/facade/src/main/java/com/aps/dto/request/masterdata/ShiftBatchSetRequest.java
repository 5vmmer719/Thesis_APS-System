package com.aps.dto.request.masterdata;

import com.aps.dto.response.masterdata.ShiftConfigDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 批量设置班次请求
 */
@Data
@Schema(description = "批量设置班次请求")
public class ShiftBatchSetRequest {

    @Schema(description = "日历日ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "日历日ID不能为空")
    private Long dayId;

    @Schema(description = "班次配置列表", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "班次配置不能为空")
    private List<ShiftConfigDTO> shifts;
}
