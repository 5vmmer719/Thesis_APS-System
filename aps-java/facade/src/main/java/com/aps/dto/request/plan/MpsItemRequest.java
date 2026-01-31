package com.aps.dto.request.plan;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

/**
 * MPS明细项
 *
 * @author APS System
 * @since 2024-01-30
 */
@Data
@Schema(description = "MPS明细项")
public class MpsItemRequest {

    @Schema(description = "计划日期", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "计划日期不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate bizDate;

    @Schema(description = "车型ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "车型ID不能为空")
    private Long modelId;

    @Schema(description = "计划数量", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "计划数量不能为空")
    private Integer qty;
}

