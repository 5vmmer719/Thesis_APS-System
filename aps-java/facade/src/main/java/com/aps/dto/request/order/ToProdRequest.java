package com.aps.dto.request.order;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

/**
 * 销售订单转生产订单请求
 */
@Data
@Schema(description = "销售订单转生产订单请求")
public class ToProdRequest {

    @NotNull(message = "订单类型不能为空")
    @Min(value = 1, message = "订单类型必须在1-5之间")
    @Max(value = 5, message = "订单类型必须在1-5之间")
    @Schema(description = "订单类型: 1-常规, 2-紧急, 3-定制, 4-插单, 5-返工", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer orderKind;

    @NotNull(message = "BOM ID不能为空")
    @Schema(description = "BOM ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "2001")
    private Long bomId;

    @NotBlank(message = "工艺路线版本不能为空")
    @Size(max = 32, message = "工艺路线版本长度不能超过32")
    @Schema(description = "工艺路线版本", requiredMode = Schema.RequiredMode.REQUIRED, example = "V1.0")
    private String routeVersion;
}

