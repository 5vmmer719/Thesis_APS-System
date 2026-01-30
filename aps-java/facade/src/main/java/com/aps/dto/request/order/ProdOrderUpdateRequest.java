package com.aps.dto.request.order;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

/**
 * 生产订单更新请求
 */
@Data
@Schema(description = "生产订单更新请求")
public class ProdOrderUpdateRequest {

    @Min(value = 1, message = "数量必须大于0")
    @Schema(description = "订单数量", example = "120")
    private Integer qty;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "交期", example = "2026-02-20")
    private LocalDate dueDate;

    @Min(value = 0, message = "优先级不能为负数")
    @Schema(description = "优先级", example = "10")
    private Integer priority;

    @Size(max = 255, message = "备注长度不能超过255")
    @Schema(description = "备注")
    private String remark;
}
