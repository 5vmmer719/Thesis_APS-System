package com.aps.dto.response.masterdata;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 工位信息DTO
 */
@Data
@Schema(description = "工位信息")
public class StationDTO {

    @Schema(description = "工位ID")
    private Long id;

    @Schema(description = "产线ID")
    private Long lineId;

    @Schema(description = "产线编码")
    private String lineCode;

    @Schema(description = "产线名称")
    private String lineName;

    @Schema(description = "工位编码")
    private String stationCode;

    @Schema(description = "工位名称")
    private String stationName;

    @Schema(description = "班次产能（辆/班）")
    private Integer capacityQtyPerShift;

    @Schema(description = "状态：0-禁用，1-启用")
    private Integer status;

    @Schema(description = "状态文本")
    private String statusText;

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
