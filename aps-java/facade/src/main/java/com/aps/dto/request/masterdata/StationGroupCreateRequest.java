package com.aps.dto.request.masterdata;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

/**
 * 创建工位组请求
 */
@Data
@Schema(description = "创建工位组请求")
public class StationGroupCreateRequest {

    @Schema(description = "工位组编码", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "工位组编码不能为空")
    private String groupCode;

    @Schema(description = "工位组名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "工位组名称不能为空")
    private String groupName;

    @Schema(description = "工位ID列表")
    private List<Long> stationIds;

    @Schema(description = "状态：0-禁用，1-启用", example = "1")
    private Integer status = 1;

    @Schema(description = "备注")
    private String remark;
}
