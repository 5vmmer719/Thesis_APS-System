package com.aps.dto.response.masterdata;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * BOM详情DTO（包含明细列表）
 */
@Data
@Schema(description = "BOM详情")
public class BomDetailDTO {

    @Schema(description = "BOM基本信息")
    private BomDTO bom;

    @Schema(description = "BOM明细列表")
    private List<BomItemDTO> items;
}
