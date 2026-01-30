package com.aps.dto.request.order;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

/**
 * 销售订单创建请求
 */
@Data
@Schema(description = "销售订单创建请求")
public class SalesOrderCreateRequest {

    @NotBlank(message = "销售订单号不能为空")
    @Size(max = 64, message = "销售订单号长度不能超过64")
    @Schema(description = "销售订单号", requiredMode = Schema.RequiredMode.REQUIRED, example = "SO20260129001")
    private String salesNo;

    @Size(max = 64, message = "客户编码长度不能超过64")
    @Schema(description = "客户编码", example = "CUST001")
    private String customerCode;

    @NotNull(message = "车型ID不能为空")
    @Schema(description = "车型ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1001")
    private Long modelId;

    @NotNull(message = "数量不能为空")
    @Min(value = 1, message = "数量必须大于0")
    @Schema(description = "订单数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "100")
    private Integer qty;

    @NotNull(message = "交期不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "交期", requiredMode = Schema.RequiredMode.REQUIRED, example = "2026-02-15")
    private LocalDate dueDate;

    @Min(value = 0, message = "优先级不能为负数")
    @Schema(description = "优先级(数字越大优先级越高)", example = "5")
    private Integer priority;

    @Schema(description = "来源数据(JSON)")
    private String sourcePayload;

    @Size(max = 255, message = "备注长度不能超过255")
    @Schema(description = "备注")
    private String remark;
}
