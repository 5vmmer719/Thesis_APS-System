package com.aps.dto.request.masterdata;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 更新工艺路线请求
 */
@Data
@Schema(description = "更新工艺路线请求")
public class RouteUpdateRequest {

    @Schema(description = "工艺路线ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "工艺路线ID不能为空")
    private Long id;

    @Schema(description = "版本号")
    private String version;

    @Schema(description = "状态：0-禁用，1-启用")
    private Integer status;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "工序列表（如果提供则全量更新）")
    @Valid
    private List<OperationCreateRequest> operations;
}
