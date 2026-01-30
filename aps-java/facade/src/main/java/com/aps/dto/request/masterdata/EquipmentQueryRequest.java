package com.aps.dto.request.masterdata;


import com.aps.request.PageRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 设备查询请求
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "设备查询请求")
public class EquipmentQueryRequest extends PageRequest {

    @Schema(description = "工位ID")
    private Long stationId;

    @Schema(description = "设备编码（模糊查询）")
    private String equipCode;

    @Schema(description = "设备名称（模糊查询）")
    private String equipName;

    @Schema(description = "状态：0-禁用，1-启用")
    private Integer status;
}
