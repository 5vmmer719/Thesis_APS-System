package com.aps.dto.request.plan;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;

/**
 * MPS更新请求
 *
 * @author APS System
 * @since 2024-01-30
 */
@Data
@Schema(description = "MPS更新请求")
public class MpsUpdateRequest {

    @Schema(description = "MPS计划编号")
    private String mpsNo;

    @Schema(description = "计划开始日期")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    @Schema(description = "计划结束日期")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;

    @Schema(description = "状态: 0-草稿, 1-审批中, 2-已批准, 3-关闭")
    private Integer status;

    @Schema(description = "备注")
    private String remark;
}

