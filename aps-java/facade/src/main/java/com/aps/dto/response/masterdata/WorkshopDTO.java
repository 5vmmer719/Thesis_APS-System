package com.aps.dto.response.masterdata;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 车间信息DTO
 */
@Data
@Schema(description = "车间信息")
public class WorkshopDTO {

    @Schema(description = "车间ID")
    private Long id;

    @Schema(description = "车间编码")
    private String workshopCode;

    @Schema(description = "车间名称")
    private String workshopName;

    @Schema(description = "工艺类型：1-冲压，2-焊装，3-涂装，4-总装")
    private Integer processType;

    @Schema(description = "工艺类型文本")
    private String processTypeText;

    @Schema(description = "状态：0-禁用，1-启用")
    private Integer status;

    @Schema(description = "状态文本")
    private String statusText;

    @Schema(description = "产线数量")
    private Integer lineCount;

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
