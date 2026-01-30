package com.aps.dto.request.masterdata;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "更新物料请求")
public class ItemUpdateRequest {

    @Schema(description = "物料ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "物料ID不能为空")
    private Long id;

    @Schema(description = "物料名称")
    private String itemName;

    @Schema(description = "物料类型：1-原料，2-半成品，3-成品，4-辅料")
    private Integer itemType;

    @Schema(description = "计量单位")
    private String uom;

    @Schema(description = "规格")
    private String spec;

    @Schema(description = "状态")
    private Integer status;

    @Schema(description = "备注")
    private String remark;
}
