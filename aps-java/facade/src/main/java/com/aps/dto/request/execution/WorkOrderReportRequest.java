// facade/src/main/java/com/aps/dto/request/execution/WorkOrderReportRequest.java
package com.aps.dto.request.execution;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 工单报工请求
 * 对应OpenAPI WorkOrderReportRequest
 */
@Data
@Schema(description = "工单报工请求")
public class WorkOrderReportRequest {

    @NotNull(message = "报工时间不能为空")
    @Schema(description = "报工时间", required = true)
    private LocalDateTime reportTime;

    @Schema(description = "工位ID")
    private Long stationId;

    @NotNull(message = "良品数量不能为空")
    @Schema(description = "良品数量", required = true)
    private Integer qtyGood;

    @NotNull(message = "不良品数量不能为空")
    @Schema(description = "不良品数量", required = true)
    private Integer qtyBad;

    @Schema(description = "详情JSON")
    private Map<String, Object> detailJson;
}
