package com.aps.dto.request.masterdata;


import com.aps.request.PageRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 人力组查询请求
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "人力组查询请求")
public class LaborGroupQueryRequest extends PageRequest {

    @Schema(description = "人力组编码（模糊查询）")
    private String laborCode;

    @Schema(description = "人力组名称（模糊查询）")
    private String laborName;

    @Schema(description = "状态：0-禁用，1-启用")
    private Integer status;
}
