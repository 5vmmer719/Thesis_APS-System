package com.aps.dto.response.masterdata;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 工艺路线信息DTO
 */
@Data
@Schema(description = "工艺路线信息")
public class RouteDTO {

    @Schema(description = "工艺路线ID")
    private Long id;

    @Schema(description = "路线编码")
    private String routeCode;

    @Schema(description = "车型ID")
    private Long modelId;

    @Schema(description = "车型编码")
    private String modelCode;

    @Schema(description = "车型名称")
    private String modelName;

    @Schema(description = "工艺类型：1-冲压，2-焊装，3-涂装，4-总装")
    private Integer processType;

    @Schema(description = "工艺类型文本")
    private String processTypeText;

    @Schema(description = "版本号")
    private String version;

    @Schema(description = "状态：0-禁用，1-启用")
    private Integer status;

    @Schema(description = "状态文本")
    private String statusText;

    @Schema(description = "工序数量")
    private Integer operationCount;

    @Schema(description = "总标准工时（分钟）")
    private Integer totalStdMinutes;

    @Schema(description = "总准备时间（分钟）")
    private Integer totalSetupMinutes;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdTime;

    @Schema(description = "创建人")
    private String createdBy;

    @Schema(description = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedTime;

    @Schema(description = "更新人")
    private String updatedBy;
}
