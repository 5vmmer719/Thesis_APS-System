package com.aps.dto.request.plan;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

/**
 * MPS创建请求
 *
 * @author APS System
 * @since 2024-01-30
 */
@Data
@Schema(description = "MPS创建请求")
public class MpsCreateRequest {

    @Schema(description = "MPS计划编号", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "MPS计划编号不能为空")
    private String mpsNo;

    @Schema(description = "计划开始日期", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "计划开始日期不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    @Schema(description = "计划结束日期", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "计划结束日期不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;

    @Schema(description = "状态: 0-草稿, 1-审批中, 2-已批准, 3-关闭")
    private Integer status;

    @Schema(description = "备注")
    private String remark;
}

