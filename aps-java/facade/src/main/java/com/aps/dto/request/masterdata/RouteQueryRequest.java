package com.aps.dto.request.masterdata;


import com.aps.request.PageRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 工艺路线查询请求
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "工艺路线查询请求")
public class RouteQueryRequest extends PageRequest {

    @Schema(description = "路线编码（模糊查询）")
    private String routeCode;

    @Schema(description = "车型ID")
    private Long modelId;

    @Schema(description = "工艺类型：1-冲压，2-焊装，3-涂装，4-总装")
    private Integer processType;

    @Schema(description = "版本号")
    private String version;

    @Schema(description = "状态：0-禁用，1-启用")
    private Integer status;
}
