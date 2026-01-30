package com.aps.dto.request.masterdata;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 更新工位组请求
 */
@Data
@Schema(description = "更新工位组请求")
public class StationGroupUpdateRequest {

    @Schema(description = "工位组ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "工位组ID不能为空")
    private Long id;

    @Schema(description = "工位组名称")
    private String groupName;

    @Schema(description = "工位ID列表（如果提供则全量更新）")
    private List<Long> stationIds;

    @Schema(description = "状态：0-禁用，1-启用")
    private Integer status;

    @Schema(description = "备注")
    private String remark;
}
