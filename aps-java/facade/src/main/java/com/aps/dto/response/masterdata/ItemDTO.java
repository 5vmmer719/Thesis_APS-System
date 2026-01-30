package com.aps.dto.response.masterdata;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "物料信息")
public class ItemDTO {

    @Schema(description = "物料ID")
    private Long id;

    @Schema(description = "物料编码")
    private String itemCode;

    @Schema(description = "物料名称")
    private String itemName;

    @Schema(description = "物料类型：1-原料，2-半成品，3-成品，4-辅料")
    private Integer itemType;

    @Schema(description = "物料类型文本")
    private String itemTypeText;

    @Schema(description = "计量单位")
    private String uom;

    @Schema(description = "规格")
    private String spec;

    @Schema(description = "状态")
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

