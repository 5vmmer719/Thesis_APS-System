package com.aps.dto.request.masterdata;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

/**
 * 创建人力组请求
 */
@Data
@Schema(description = "创建人力组请求")
public class LaborGroupCreateRequest {

    @Schema(description = "人力组编码", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "人力组编码不能为空")
    private String laborCode;

    @Schema(description = "人力组名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "人力组名称不能为空")
    private String laborName;

    @Schema(description = "人数", example = "10")
    private Integer headcount = 0;

    @Schema(description = "关联工位ID列表")
    private List<Long> stationIds;

    @Schema(description = "状态：0-禁用，1-启用", example = "1")
    private Integer status = 1;

    @Schema(description = "备注")
    private String remark;
}
