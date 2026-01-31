package com.aps.dto.request.plan;

import com.aps.request.PageRequest;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

/**
 * MPS查询请求
 *
 * @author APS System
 * @since 2024-01-30
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "MPS查询请求")
public class MpsQueryRequest extends PageRequest {

    @Schema(description = "MPS计划编号（模糊查询）")
    private String mpsNo;

    @Schema(description = "状态: 0-草稿, 1-审批中, 2-已批准, 3-关闭")
    private Integer status;

    @Schema(description = "开始日期（起）")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDateFrom;

    @Schema(description = "开始日期（止）")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDateTo;

    @Schema(description = "结束日期（起）")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDateFrom;

    @Schema(description = "结束日期（止）")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDateTo;
}

