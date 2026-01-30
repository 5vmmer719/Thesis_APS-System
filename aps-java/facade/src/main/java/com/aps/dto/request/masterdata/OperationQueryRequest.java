package com.aps.dto.request.masterdata;


import com.aps.request.PageRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 工序查询请求
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "工序查询请求")
public class OperationQueryRequest extends PageRequest {

    @Schema(description = "工艺路线ID")
    private Long routeId;

    @Schema(description = "工序编码（模糊查询）")
    private String opCode;

    @Schema(description = "工序名称（模糊查询）")
    private String opName;

    @Schema(description = "工位组")
    private String stationGroup;

    @Schema(description = "状态：0-禁用，1-启用")
    private Integer status;
}
