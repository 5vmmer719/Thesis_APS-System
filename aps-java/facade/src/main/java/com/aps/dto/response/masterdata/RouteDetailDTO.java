package com.aps.dto.response.masterdata;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 工艺路线详情DTO（包含工序列表）
 */
@Data
@Schema(description = "工艺路线详情")
public class RouteDetailDTO {

    @Schema(description = "工艺路线基本信息")
    private RouteDTO route;

    @Schema(description = "工序列表")
    private List<OperationDTO> operations;
}
