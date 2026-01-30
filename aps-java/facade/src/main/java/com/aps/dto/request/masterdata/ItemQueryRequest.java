package com.aps.dto.request.masterdata;


import com.aps.request.PageRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "物料查询请求")
public class ItemQueryRequest extends PageRequest {

    @Schema(description = "关键字（编码或名称）")
    private String keyword;

    @Schema(description = "物料类型：1-原料，2-半成品，3-成品，4-辅料")
    private Integer itemType;

    @Schema(description = "状态：1-启用，0-停用")
    private Integer status;
}
