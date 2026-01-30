package com.aps.dto.response.masterdata;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 工装信息DTO
 */
@Data
@Schema(description = "工装信息")
public class ToolingDTO {

    @Schema(description = "工装ID")
    private Long id;

    @Schema(description = "工装编码")
    private String toolingCode;

    @Schema(description = "工装名称")
    private String toolingName;

    @Schema(description = "工装类型：1-模具，2-夹具，3-检具")
    private Integer toolingType;

    @Schema(description = "工装类型文本")
    private String toolingTypeText;

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
