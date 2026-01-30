package com.aps.dto.response.schedule;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 排产方案响应DTO
 *
 * @author APS System
 * @since 2024-01-01
 */
@Data
@Schema(description = "排产方案响应")
public class SchedulePlanDTO {

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "任务ID")
    private Long jobId;

    @Schema(description = "方案编号")
    private String planNo;

    @Schema(description = "是否最优方案: 0-否, 1-是")
    private Integer isBest;

    @Schema(description = "KPI汇总数据")
    private Map<String, Object> kpiJson;

    @Schema(description = "状态: 0-草稿, 1-已发布, 2-已作废")
    private Integer status;

    @Schema(description = "状态文本")
    private String statusText;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "方案统计信息")
    private PlanStatDTO stat;

    @Schema(description = "冲突列表")
    private List<PlanConflictDTO> conflicts;

    @Schema(description = "明细数量")
    private Integer bucketCount;

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

    /**
     * 方案统计DTO
     */
    @Data
    @Schema(description = "方案统计信息")
    public static class PlanStatDTO {
        @Schema(description = "OTD准时交付率(%)", example = "95.5")
        private Double otdRate;

        @Schema(description = "换型次数", example = "12")
        private Integer setupTimes;

        @Schema(description = "平均产线负荷率(%)", example = "85.3")
        private Double avgLineLoad;
    }

    /**
     * 冲突DTO
     */
    @Data
    @Schema(description = "冲突信息")
    public static class PlanConflictDTO {
        @Schema(description = "冲突类型")
        private String conflictType;

        @Schema(description = "级别: 1-提示, 2-警告, 3-致命")
        private Integer level;

        @Schema(description = "级别文本")
        private String levelText;

        @Schema(description = "冲突消息")
        private String message;

        @Schema(description = "对象类型")
        private String objectType;

        @Schema(description = "对象ID")
        private Long objectId;
    }
}

