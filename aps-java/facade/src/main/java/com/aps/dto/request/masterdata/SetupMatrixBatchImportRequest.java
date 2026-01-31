package com.aps.dto.request.masterdata;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 批量导入换型矩阵请求
 *
 * @author APS System
 * @since 2024-01-30
 */
@Data
@Schema(description = "批量导入换型矩阵请求")
public class SetupMatrixBatchImportRequest {

    @Schema(description = "工艺类型：1-冲压，2-焊装，3-涂装，4-总装", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "工艺类型不能为空")
    private Integer processType;

    @Schema(description = "导入模式：REPLACE-替换，APPEND-追加", example = "REPLACE")
    private String mode;

    @Schema(description = "换型矩阵项列表", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "换型矩阵项列表不能为空")
    private List<SetupMatrixItem> items;

    @Data
    @Schema(description = "换型矩阵项")
    public static class SetupMatrixItem {
        @Schema(description = "源换型键", requiredMode = Schema.RequiredMode.REQUIRED)
        private String fromKey;

        @Schema(description = "目标换型键", requiredMode = Schema.RequiredMode.REQUIRED)
        private String toKey;

        @Schema(description = "换型时间（分钟）", requiredMode = Schema.RequiredMode.REQUIRED)
        private Integer setupMinutes;

        @Schema(description = "换型成本")
        private BigDecimal setupCost;

        @Schema(description = "备注")
        private String remark;
    }
}

