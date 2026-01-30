package com.aps.dto.request.masterdata;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "更新车型请求")
public class ModelUpdateRequest {

    @Schema(description = "车型ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "车型ID不能为空")
    private Long id;

    @Schema(description = "车型名称")
    private String modelName;

    @Schema(description = "平台")
    private String platform;

    @Schema(description = "状态")
    private Integer status;

    @Schema(description = "备注")
    private String remark;
}
