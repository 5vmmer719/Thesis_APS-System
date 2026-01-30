package com.aps.dto.request.masterdata;


import com.aps.request.PageRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "车型查询请求")
public class ModelQueryRequest extends PageRequest {

    @Schema(description = "关键字（编码或名称）")
    private String keyword;

    @Schema(description = "平台")
    private String platform;

    @Schema(description = "状态：1-启用，0-停用")
    private Integer status;
}
