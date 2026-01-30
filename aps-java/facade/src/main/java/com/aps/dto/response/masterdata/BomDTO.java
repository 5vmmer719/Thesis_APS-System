package com.aps.dto.response.masterdata;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * BOM信息DTO
 */
@Data
@Schema(description = "BOM信息")
public class BomDTO {

    @Schema(description = "BOM ID")
    private Long id;

    @Schema(description = "BOM编码")
    private String bomCode;

    // bomName 字段不存在于数据库，已从实体类中标记为 @TableField(exist = false)
    // @Schema(description = "BOM名称")
    // private String bomName;

    @Schema(description = "车型ID")
    private Long modelId;

    @Schema(description = "车型编码")
    private String modelCode;

    @Schema(description = "车型名称")
    private String modelName;

    @Schema(description = "版本号")
    private String version;

    @Schema(description = "生效开始时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime effectiveFrom;

    @Schema(description = "生效结束时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime effectiveTo;

    @Schema(description = "状态")
    private Integer status;

    @Schema(description = "状态文本")
    private String statusText;

    @Schema(description = "物料数量")
    private Integer itemCount;

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
