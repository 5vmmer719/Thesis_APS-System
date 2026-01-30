package com.aps.dto.request.masterdata;


import com.aps.request.PageRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 工位组查询请求
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "工位组查询请求")
public class StationGroupQueryRequest extends PageRequest {

    @Schema(description = "工位组编码（模糊查询）")
    private String groupCode;

    @Schema(description = "工位组名称（模糊查询）")
    private String groupName;

    @Schema(description = "状态：0-禁用，1-启用")
    private Integer status;
}
