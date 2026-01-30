package com.aps.dto.request.order;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

/**
 * 生产订单创建请求
 */
@Data
@Schema(description = "生产订单创建请求")
public class ProdOrderCreateRequest {

    @NotBlank(message = "生产订单号不能为空")
    @Size(max = 64, message = "生产订单号长度不能超过64")
    @Schema(description = "生产订单号", requiredMode = Schema.RequiredMode.REQUIRED, example = "MO20260129001")
    private String prodNo;

    @Schema(description = "销售订单ID", example = "1001")
    private Long salesId;

    @NotNull(message = "订单类型不能为空")
    @Min(value = 1, message = "订单类型必须在1-5之间")
    @Max(value = 5, message = "订单类型必须在1-5之间")
    @Schema(description = "订单类型: 1-常规, 2-紧急, 3-定制, 4-插单, 5-返工", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer orderKind;

    @NotNull(message = "车型ID不能为空")
    @Schema(description = "车型ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1001")
    private Long modelId;

    @NotNull(message = "BOM ID不能为空")
    @Schema(description = "BOM ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "2001")
    private Long bomId;

    @NotBlank(message = "工艺路线版本不能为空")
    @Size(max = 32, message = "工艺路线版本长度不能超过32")
    @Schema(description = "工艺路线版本", requiredMode = Schema.RequiredMode.REQUIRED, example = "V1.0")
    private String routeVersion;

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

    @Size(max = 255, message = "备注长度不能超过255")
    @Schema(description = "备注")
    private String remark;
}
