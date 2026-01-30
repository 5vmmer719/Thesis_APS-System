package com.aps.dto.request.masterdata;


import com.aps.request.PageRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * BOM查询请求
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "BOM查询请求")
public class BomQueryRequest extends PageRequest {

    @Schema(description = "关键字（BOM编码或名称）")
    private String keyword;

    @Schema(description = "车型ID")
    private Long modelId;

    @Schema(description = "版本号")
    private String version;

    @Schema(description = "状态：1-启用，0-停用")
    private Integer status;
}
