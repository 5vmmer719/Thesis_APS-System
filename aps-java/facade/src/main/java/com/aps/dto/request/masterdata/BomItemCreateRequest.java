package com.aps.dto.request.masterdata;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 创建BOM明细请求
 */
@Data
@Schema(description = "创建BOM明细请求")
public class BomItemCreateRequest {

    @Schema(description = "父物料编码")
    private String parentItemCode;

    @Schema(description = "物料编码", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "物料编码不能为空")
    private String itemCode;

    @Schema(description = "用量", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "用量不能为空")
    @Positive(message = "用量必须大于0")
    private BigDecimal qty;

    @Schema(description = "损耗率（默认0）")
    private BigDecimal lossRate;

    @Schema(description = "层级（默认1）")
    private Integer levelNo;

    @Schema(description = "是否可选：1-是，0-否（默认0）")
    private Integer isOptional;

    @Schema(description = "选配组")
    private String optionGroup;

    @Schema(description = "替代组")
    private String altGroup;

    @Schema(description = "备注")
    private String remark;
}
