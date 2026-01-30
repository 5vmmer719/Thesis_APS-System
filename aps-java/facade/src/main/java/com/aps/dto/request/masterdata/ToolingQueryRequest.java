package com.aps.dto.request.masterdata;


import com.aps.request.PageRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 工装查询请求
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "工装查询请求")
public class ToolingQueryRequest extends PageRequest {

    @Schema(description = "工装编码（模糊查询）")
    private String toolingCode;

    @Schema(description = "工装名称（模糊查询）")
    private String toolingName;

    @Schema(description = "工装类型：1-模具，2-夹具，3-检具")
    private Integer toolingType;

    @Schema(description = "状态：0-禁用，1-启用")
    private Integer status;
}
