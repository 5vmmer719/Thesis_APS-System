package com.aps.dto.request.masterdata;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

/**
 * 创建节假日请求
 */
@Data
@Schema(description = "创建节假日请求")
public class HolidayCreateRequest {

    @Schema(description = "业务日期", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "日期不能为空")
    private LocalDate bizDate;

    @Schema(description = "节假日名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "节假日名称不能为空")
    private String name;
}
