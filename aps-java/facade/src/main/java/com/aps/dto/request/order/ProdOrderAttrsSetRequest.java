package com.aps.dto.request.order;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 生产订单属性设置请求
 */
@Data
@Schema(description = "生产订单属性设置请求")
public class ProdOrderAttrsSetRequest {

    @NotEmpty(message = "属性列表不能为空")
    @Valid
    @Schema(description = "属性列表", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<ProdOrderAttrItem> attrs;

    @Data
    @Schema(description = "订单属性项")
    public static class ProdOrderAttrItem {

        @NotNull(message = "属性键不能为空")
        @Schema(description = "属性键", requiredMode = Schema.RequiredMode.REQUIRED, example = "color")
        private String attrKey;

        @NotNull(message = "属性值不能为空")
        @Schema(description = "属性值", requiredMode = Schema.RequiredMode.REQUIRED, example = "红色")
        private String attrValue;

        @Schema(description = "备注")
        private String remark;
    }
}
