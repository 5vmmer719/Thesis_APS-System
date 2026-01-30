package com.aps.dto.request.masterdata;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "创建物料请求")
public class ItemCreateRequest {

    @Schema(description = "物料编码", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "物料编码不能为空")
    private String itemCode;

    @Schema(description = "物料名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "物料名称不能为空")
    private String itemName;

    @Schema(description = "物料类型：1-原料，2-半成品，3-成品，4-辅料", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "物料类型不能为空")
    private Integer itemType;

    @Schema(description = "计量单位", example = "个")
    private String uom;

    @Schema(description = "规格")
    private String spec;

    @Schema(description = "状态", example = "1")
    private Integer status = 1;

    @Schema(description = "备注")
    private String remark;
}

