package com.aps.dto.request.masterdata;

import com.aps.request.PageRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 换型矩阵查询请求
 *
 * @author APS System
 * @since 2024-01-30
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "换型矩阵查询请求")
public class SetupMatrixQueryRequest extends PageRequest {

    @Schema(description = "关键字（源键或目标键）")
    private String keyword;

    @Schema(description = "工艺类型：1-冲压，2-焊装，3-涂装，4-总装")
    private Integer processType;

    @Schema(description = "状态：1-启用，0-停用")
    private Integer status;
}

