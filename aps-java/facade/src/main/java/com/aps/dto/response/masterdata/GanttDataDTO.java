package com.aps.dto.response.masterdata;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

/**
 * 甘特图数据DTO
 *
 * @author APS System
 * @since 2024-01-01
 */
@Data
@Schema(description = "甘特图数据")
public class GanttDataDTO {

    @Schema(description = "产线列表")
    private List<LineInfo> lines;

    @Schema(description = "时间轴(日期列表)")
    private List<LocalDate> timeline;

    @Schema(description = "甘特图数据")
    private List<GanttRow> rows;

    /**
     * 产线信息
     */
    @Data
    @Schema(description = "产线信息")
    public static class LineInfo {
        @Schema(description = "产线ID")
        private Long lineId;

        @Schema(description = "产线编码")
        private String lineCode;

        @Schema(description = "产线名称")
        private String lineName;

        @Schema(description = "工艺类型")
        private Integer processType;
    }

    /**
     * 甘特图行
     */
    @Data
    @Schema(description = "甘特图行数据")
    public static class GanttRow {
        @Schema(description = "产线ID")
        private Long lineId;

        @Schema(description = "日期")
        private LocalDate date;

        @Schema(description = "班次编码")
        private String shiftCode;

        @Schema(description = "任务列表")
        private List<GanttTask> tasks;
    }

    /**
     * 甘特图任务
     */
    @Data
    @Schema(description = "甘特图任务")
    public static class GanttTask {
        @Schema(description = "桶ID")
        private Long bucketId;

        @Schema(description = "生产订单ID")
        private Long prodOrderId;

        @Schema(description = "生产订单号")
        private String prodNo;

        @Schema(description = "车型编码")
        private String modelCode;

        @Schema(description = "车型名称")
        private String modelName;

        @Schema(description = "数量")
        private Integer qty;

        @Schema(description = "顺序号")
        private Integer seqNo;

        @Schema(description = "是否换型")
        private Boolean hasSetup;

        @Schema(description = "换型时间(分钟)")
        private Integer setupMinutes;
    }
}

