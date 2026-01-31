package com.aps.dto.request.plan;

import com.aps.request.PageRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * MRP查询请求
 *
 * @author APS System
 * @since 2024-01-30
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "MRP查询请求")
public class MrpQueryRequest extends PageRequest {

    @Schema(description = "MRP计划编号（模糊查询）")
    private String mrpNo;

    @Schema(description = "MPS主表ID")
    private Long mpsId;

    @Schema(description = "状态: 0-生成中, 1-完成, 2-失败")
    private Integer status;
}

