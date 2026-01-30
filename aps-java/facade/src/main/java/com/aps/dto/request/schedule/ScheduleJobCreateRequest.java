package com.aps.dto.request.schedule;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 排产任务创建请求
 *
 * @author APS System
 * @since 2024-01-01
 */
@Data
@Schema(description = "排产任务创建请求")
public class ScheduleJobCreateRequest {

    @NotNull(message = "排产起始日期不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "排产起始日期", requiredMode = Schema.RequiredMode.REQUIRED, example = "2026-02-01")
    private LocalDate horizonStart;

    @NotNull(message = "排产结束日期不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "排产结束日期", requiredMode = Schema.RequiredMode.REQUIRED, example = "2026-02-28")
    private LocalDate horizonEnd;

    @Schema(description = "订单ID列表", example = "[1001, 1002, 1003]")
    private List<Long> orderIds;

    @Schema(description = "工艺类型列表: 1-冲压, 2-焊装, 3-涂装, 4-总装", example = "[1, 2, 3, 4]")
    private List<Integer> processTypes;

    @Schema(description = "产线范围配置")
    private List<LineScopeDTO> lineScopes;

    @Schema(description = "排产目标权重配置")
    private ScheduleObjectiveDTO objective;

    @Schema(description = "约束规则配置")
    private ScheduleConstraintsDTO constraints;

    /**
     * 产线范围DTO
     */
    @Data
    @Schema(description = "产线范围配置")
    public static class LineScopeDTO {
        @Schema(description = "工艺类型: 1-冲压, 2-焊装, 3-涂装, 4-总装", example = "1")
        private Integer processType;

        @Schema(description = "产线ID列表", example = "[101, 102]")
        @JsonDeserialize(using = LongListDeserializer.class)
        private List<Long> lineIds;
    }

    /**
     * 排产目标权重DTO
     */
    @Data
    @Schema(description = "排产目标权重配置")
    public static class ScheduleObjectiveDTO {
        @Schema(description = "准时交付权重(0-100)", example = "40")
        private Integer otdWeight;

        @Schema(description = "换型最小化权重(0-100)", example = "30")
        private Integer setupWeight;

        @Schema(description = "产线负荷均衡权重(0-100)", example = "30")
        private Integer loadBalanceWeight;
    }

    /**
     * 约束规则DTO
     */
    @Data
    @Schema(description = "约束规则配置")
    public static class ScheduleConstraintsDTO {
        @Schema(description = "是否允许跨班次", example = "false")
        private Boolean allowCrossShift;

        @Schema(description = "是否考虑维护窗口", example = "true")
        private Boolean considerMaintenance;

        @Schema(description = "最大提前天数", example = "3")
        private Integer maxEarlyDays;

        @Schema(description = "最大延迟天数", example = "0")
        private Integer maxLateDays;

        @Schema(description = "其他约束条件")
        private Map<String, Object> others;
    }

    /**
     * Long列表自定义反序列化器
     * 支持多种格式：
     * 1. 数组格式: [101, 102, 103]
     * 2. 字符串格式: "101,102,103"
     * 3. 字符串数组: ["101", "102", "103"]
     * 4. 嵌套数组（扁平化）: [[101, 102], [103]]
     */
    public static class LongListDeserializer extends JsonDeserializer<List<Long>> {
        @Override
        public List<Long> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            if (p.currentToken().isNumeric()) {
                // 单个数字
                return List.of(p.getLongValue());
            } else if (p.currentToken().isScalarValue()) {
                // 字符串格式: "101,102,103"
                String text = p.getText();
                if (text == null || text.trim().isEmpty()) {
                    return new ArrayList<>();
                }
                return Arrays.stream(text.split(","))
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .map(Long::parseLong)
                        .collect(Collectors.toList());
            } else {
                // 数组格式: [101, 102, 103] 或嵌套数组
                return parseArray(p);
            }
        }

        private List<Long> parseArray(JsonParser p) throws IOException {
            List<Long> result = new ArrayList<>();
            while (p.nextToken() != null && !p.currentToken().isStructEnd()) {
                if (p.currentToken().isNumeric()) {
                    // 数字元素
                    result.add(p.getLongValue());
                } else if (p.currentToken().isScalarValue()) {
                    // 字符串元素: "101"
                    String text = p.getText();
                    if (text != null && !text.trim().isEmpty()) {
                        result.add(Long.parseLong(text.trim()));
                    }
                } else if (p.currentToken().isStructStart()) {
                    // 嵌套数组: [[101, 102]]，递归解析并扁平化
                    result.addAll(parseArray(p));
                }
            }
            return result;
        }
    }
}

