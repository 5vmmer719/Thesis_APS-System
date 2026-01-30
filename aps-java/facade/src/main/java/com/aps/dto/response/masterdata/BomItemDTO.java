package com.aps.dto.response.masterdata;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * BOM明细DTO
 */
@Data
@Schema(description = "BOM明细信息")
public class BomItemDTO {

    @Schema(description = "明细ID")
    private Long id;

    @Schema(description = "BOM ID")
    private Long bomId;

    @Schema(description = "父物料编码")
    private String parentItemCode;

    @Schema(description = "物料编码")
    private String itemCode;

    @Schema(description = "物料名称")
    private String itemName;

    @Schema(description = "物料类型：1-原材料，2-半成品，3-成品")
    private Integer itemType;

    @Schema(description = "物料类型文本")
    private String itemTypeText;

    @Schema(description = "用量")
    private BigDecimal qty;

    @Schema(description = "损耗率")
    private BigDecimal lossRate;

    @Schema(description = "层级")
    private Integer levelNo;

    @Schema(description = "是否可选：1-是，0-否")
    private Integer isOptional;

    @Schema(description = "选配组")
    private String optionGroup;

    @Schema(description = "替代组")
    private String altGroup;

    @Schema(description = "备注")
    private String remark;
}
