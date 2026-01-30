package com.aps.dto.request.schedule;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 排产任务查询请求
 *
 * @author APS System
 * @since 2024-01-01
 */
@Data
@Schema(description = "排产任务查询请求")
public class ScheduleJobQueryRequest {

    @Schema(description = "任务编号(模糊查询)", example = "JOB")
    private String jobNo;

    @Schema(description = "状态: 0-待运行, 1-运行中, 2-成功, 3-失败, 4-不可行", example = "2")
    private Integer status;

    @Schema(description = "开始日期(起)", example = "2026-02-01")
    private String horizonStartFrom;

    @Schema(description = "开始日期(止)", example = "2026-02-28")
    private String horizonStartTo;

    @Schema(description = "创建人", example = "admin")
    private String createdBy;

    @Schema(description = "页码", example = "1")
    private Integer pageNum = 1;

    @Schema(description = "每页大小", example = "10")
    private Integer pageSize = 10;
}

