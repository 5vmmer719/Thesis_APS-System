package com.aps.dto.request.masterdata;


import com.aps.request.PageRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 产线查询请求
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "产线查询请求")
public class LineQueryRequest extends PageRequest {

    @Schema(description = "车间ID")
    private Long workshopId;

    @Schema(description = "产线编码（模糊查询）")
    private String lineCode;

    @Schema(description = "产线名称（模糊查询）")
    private String lineName;

    @Schema(description = "工艺类型：1-冲压，2-焊装，3-涂装，4-总装")
    private Integer processType;

    @Schema(description = "状态：0-禁用，1-启用")
    private Integer status;
}
