package com.aps.dto.response.schedule;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 排产任务响应DTO
 *
 * @author APS System
 * @since 2024-01-01
 */
@Data
@Schema(description = "排产任务响应")
public class ScheduleJobDTO {

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "任务编号")
    private String jobNo;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "排产起始日期")
    private LocalDate horizonStart;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "排产结束日期")
    private LocalDate horizonEnd;

    @Schema(description = "范围配置")
    private Map<String, Object> scopeJson;

    @Schema(description = "目标权重配置")
    private Map<String, Object> objectiveJson;

    @Schema(description = "约束规则配置")
    private Map<String, Object> constraintJson;

    @Schema(description = "状态: 0-待运行, 1-运行中, 2-成功, 3-失败, 4-不可行")
    private Integer status;

    @Schema(description = "状态文本")
    private String statusText;

    @Schema(description = "引擎追踪ID")
    private String engineTrace;

    @Schema(description = "错误信息")
    private String errorMsg;

    @Schema(description = "方案数量")
    private Integer planCount;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "创建时间")
    private LocalDateTime createdTime;

    @Schema(description = "创建人")
    private String createdBy;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "更新时间")
    private LocalDateTime updatedTime;

    @Schema(description = "更新人")
    private String updatedBy;
}

