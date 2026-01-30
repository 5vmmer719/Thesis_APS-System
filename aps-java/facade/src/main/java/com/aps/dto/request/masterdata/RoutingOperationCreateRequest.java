package com.aps.dto.request.masterdata;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 创建工序请求
 */
@Data
@Schema(description = "创建工序请求")
public class RoutingOperationCreateRequest {

    @Schema(description = "工序编码", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "工序编码不能为空")
    private String operationCode;

    @Schema(description = "工序名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "工序名称不能为空")
    private String operationName;

    @Schema(description = "工作中心ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "工作中心ID不能为空")
    private Long workCenterId;

    @Schema(description = "序号", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "序号不能为空")
    private Integer seqNo;

    @Schema(description = "标准工时（分钟）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "标准工时不能为空")
    private BigDecimal standardTime;

    @Schema(description = "准备时间（分钟）")
    private BigDecimal setupTime;

    @Schema(description = "排队时间（分钟）")
    private BigDecimal queueTime;

    @Schema(description = "移动时间（分钟）")
    private BigDecimal moveTime;

    @Schema(description = "备注")
    private String remark;
}
