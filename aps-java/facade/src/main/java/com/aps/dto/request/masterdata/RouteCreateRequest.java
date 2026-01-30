package com.aps.dto.request.masterdata;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 创建工艺路线请求
 */
@Data
@Schema(description = "创建工艺路线请求")
public class RouteCreateRequest {

    @Schema(description = "路线编码", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "路线编码不能为空")
    private String routeCode;

    @Schema(description = "车型ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "车型ID不能为空")
    private Long modelId;

    @Schema(description = "工艺类型：1-冲压，2-焊装，3-涂装，4-总装", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "工艺类型不能为空")
    private Integer processType;

    @Schema(description = "版本号", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "版本号不能为空")
    private String version;

    @Schema(description = "状态：0-禁用，1-启用", example = "1")
    private Integer status = 1;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "工序列表", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "工序列表不能为空")
    @Valid
    private List<OperationCreateRequest> operations;
}
