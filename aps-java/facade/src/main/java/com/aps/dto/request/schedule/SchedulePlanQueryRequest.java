package com.aps.dto.request.schedule;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 排产方案查询请求
 *
 * @author APS System
 * @since 2024-01-01
 */
@Data
@Schema(description = "排产方案查询请求")
public class SchedulePlanQueryRequest {

    @Schema(description = "任务ID", example = "1001")
    private Long jobId;

    @Schema(description = "方案编号(模糊查询)", example = "PLAN")
    private String planNo;

    @Schema(description = "是否最优方案: 0-否, 1-是", example = "1")
    private Integer isBest;

    @Schema(description = "状态: 0-草稿, 1-已发布, 2-已作废", example = "1")
    private Integer status;

    @Schema(description = "页码", example = "1")
    private Integer pageNum = 1;

    @Schema(description = "每页大小", example = "10")
    private Integer pageSize = 10;
}

