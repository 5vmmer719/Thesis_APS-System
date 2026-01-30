package com.aps.dto.response.masterdata;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Map;

/**
 * 工序信息DTO
 */
@Data
@Schema(description = "工序信息")
public class OperationDTO {

    @Schema(description = "工序ID")
    private Long id;

    @Schema(description = "工艺路线ID")
    private Long routeId;

    @Schema(description = "工序编码")
    private String opCode;

    @Schema(description = "工序名称")
    private String opName;

    @Schema(description = "序号")
    private Integer seqNo;

    @Schema(description = "标准工时（分钟/辆）")
    private Integer stdMinutesPerUnit;

    @Schema(description = "准备/换型时间（分钟）")
    private Integer setupMinutes;

    @Schema(description = "可选工位组")
    private String stationGroup;

    @Schema(description = "工艺约束参数（JSON）")
    private Map<String, Object> constraintJson;

    @Schema(description = "状态：0-禁用，1-启用")
    private Integer status;

    @Schema(description = "状态文本")
    private String statusText;

    @Schema(description = "备注")
    private String remark;
}
