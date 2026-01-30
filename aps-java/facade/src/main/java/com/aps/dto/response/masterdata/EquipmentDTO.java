package com.aps.dto.response.masterdata;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 设备信息DTO
 */
@Data
@Schema(description = "设备信息")
public class EquipmentDTO {

    @Schema(description = "设备ID")
    private Long id;

    @Schema(description = "工位ID")
    private Long stationId;

    @Schema(description = "工位编码")
    private String stationCode;

    @Schema(description = "工位名称")
    private String stationName;

    @Schema(description = "设备编码")
    private String equipCode;

    @Schema(description = "设备名称")
    private String equipName;

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
