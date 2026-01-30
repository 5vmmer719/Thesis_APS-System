package com.aps.dto.request.masterdata;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Map;

/**
 * 创建工序请求
 */
@Data
@Schema(description = "创建工序请求")
public class OperationCreateRequest {

    @Schema(description = "工序编码", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "工序编码不能为空")
    private String opCode;

    @Schema(description = "工序名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "工序名称不能为空")
    private String opName;

    @Schema(description = "序号", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "序号不能为空")
    private Integer seqNo;

    @Schema(description = "标准工时（分钟/辆）", example = "60")
    private Integer stdMinutesPerUnit = 0;

    @Schema(description = "准备/换型时间（分钟）", example = "30")
    private Integer setupMinutes = 0;

    @Schema(description = "可选工位组")
    private String stationGroup;

    @Schema(description = "工艺约束参数（JSON）")
    private Map<String, Object> constraintJson;

    @Schema(description = "状态：0-禁用，1-启用", example = "1")
    private Integer status = 1;

    @Schema(description = "备注")
    private String remark;
}
